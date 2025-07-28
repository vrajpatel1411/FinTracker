package org.vrajpatel.userauthservice.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;

import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.RefreshToken;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RefreshToken refreshToken;

    private final StringRedisTemplate stringRedisTemplate;

    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    AuthService(UserRepository userRepository, RefreshToken refreshToken, StringRedisTemplate stringRedisTemplate,
                TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshToken = refreshToken;
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public LoginResponseDTO loginService(@Valid UserDTO userDTO) throws UserNotFound, UnAuthorizedException {
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail().toLowerCase());
        if (user.isPresent()) {
            if (passwordEncoder.matches(userDTO.getPassword(), user.get().getPassword())) {

                String accessToken = tokenProvider.generateToken('A', user.get().getUserId().toString());
                String refreshTokenKey = tokenProvider.generateToken('R', user.get().getUserId().toString());
                refreshToken.setRefreshToken(refreshTokenKey, user.get().getEmail());
                return new LoginResponseDTO(accessToken, refreshTokenKey);
            } else {
                throw new UnAuthorizedException("Unauthorized Access, Password is wrong");
            }
        } else {
            throw new UserNotFound("User Not Yet Registered");
        }

    }

    @Transactional
    public LoginResponseDTO registerService(RegisterUserDto userDTO) throws UserExistException {

        boolean isUser = userRepository.existsByEmail(userDTO.getEmail().toLowerCase());
        if (isUser) {
            throw new UserExistException("Email Is Already Registered");
        } else {
            User user = new User();
            user.setEmail(userDTO.getEmail().toLowerCase());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setFirstName(userDTO.getFirstName());

            user.setCreatedAt(new Date());
            user.setAuthProvider(AuthProvider.usernamepassword);
            userRepository.save(user);
            String accessToken = tokenProvider.generateToken('A', user.getUserId().toString());
            String refreshTokenKey = tokenProvider.generateToken('R', user.getUserId().toString());
            refreshToken.setRefreshToken(refreshTokenKey, user.getEmail());
            return new LoginResponseDTO(accessToken, refreshTokenKey);
        }
    }

    public String getNewAccessToken(String refreshToken) throws UserNotFound, UnAuthorizedException {
            if (StringUtils.hasText(refreshToken) && stringRedisTemplate.hasKey("refresh_token : " + refreshToken) && tokenProvider.validateRefreshToken(refreshToken)) {
                String email = stringRedisTemplate.opsForValue().get("refresh_token : " + refreshToken);
                if (email != null) {
                    Optional<User> user = userRepository.findByEmail(email.toLowerCase());
                    if (user.isPresent()) {

                        return tokenProvider.generateToken('A', user.get().getUserId().toString());
                    } else {
                        throw new UserNotFound("User Not Found");
                    }
                } else {
                   throw new UnAuthorizedException("Not Authorized. Email Not found");
                }
            } else {
                throw new UnAuthorizedException("Not Authorized. Refresh Token Empty or not Valid");
            }
    }

//    }

    public Boolean validate(String accessToken) throws UnAuthorizedException, ExpiredJwtException {
        if (StringUtils.hasText(accessToken)) {
            return tokenProvider.validateToken(accessToken);
        }
        else{
            throw new UnAuthorizedException("Unauthorized Access");
        }
    }
}





