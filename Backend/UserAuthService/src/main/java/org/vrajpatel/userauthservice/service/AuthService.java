package org.vrajpatel.userauthservice.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;

import org.vrajpatel.userauthservice.Exception.OTPException;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.OtpDto;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.OTPService.EmailService;
import org.vrajpatel.userauthservice.utils.OTPService.OTP;
import org.vrajpatel.userauthservice.utils.RefreshToken;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RefreshToken refreshToken;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final OTP otpService;

    private final EmailService emailService;



    AuthService(UserRepository userRepository, RefreshToken refreshToken, StringRedisTemplate stringRedisTemplate,
                TokenProvider tokenProvider, PasswordEncoder passwordEncoder, OTP otpService, EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshToken = refreshToken;
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
    }


    @Transactional
    public LoginResponseDTO loginService(@Valid UserDTO userDTO) throws UserNotFound, UnAuthorizedException {
        User user=userRepository.findByEmail(userDTO.getEmail().toLowerCase()).orElseThrow(()-> new UserNotFound("User Not Yet Registered"));
        if (passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            if(user.isEmailVerified()){
                return getLogin(user);
            }else{
                sendOTP(userDTO.getEmail().toLowerCase());
                LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
                loginResponseDTO.setEmail(userDTO.getEmail().toLowerCase());
                loginResponseDTO.setEmailVerified(false);
                return loginResponseDTO;
            }
        } else {
            throw new UnAuthorizedException("Unauthorized Access, Password is wrong");
        }
    }

    private LoginResponseDTO getLogin(User user) {
        logger.info("Logging to user");
        String accessToken = tokenProvider.generateToken('A', user.getUserId().toString());
        String refreshTokenKey = tokenProvider.generateToken('R', user.getUserId().toString());
        refreshToken.setRefreshToken(refreshTokenKey, user.getEmail());
        LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        loginResponseDTO.setRefreshToken(refreshTokenKey);
        loginResponseDTO.setEmailVerified(true);
        return loginResponseDTO;
    }

    @Transactional
    public LoginResponseDTO registerService(RegisterUserDto userDTO) throws UserExistException, Exception {
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
            user=userRepository.save(user);
            boolean otpStatus=sendOTP(userDTO.getEmail().toLowerCase());
            if(!otpStatus){
                logger.warn("Issue Sending OTP");
                throw new Exception("Issue sending OTP");
            }
            LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
            loginResponseDTO.setEmail(user.getEmail().toLowerCase());
            loginResponseDTO.setEmailVerified(false);
            return loginResponseDTO;
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

    public Boolean validate(String accessToken) throws UnAuthorizedException, ExpiredJwtException {
        if (StringUtils.hasText(accessToken)) {
            return tokenProvider.validateToken(accessToken);
        }
        else{
            throw new UnAuthorizedException("Unauthorized Access");
        }
    }

    public boolean sendOTP(String email) {
        try {
            String otp ;
            if(stringRedisTemplate.hasKey("OTP:" + email.toLowerCase())){
                otp=stringRedisTemplate.opsForValue().get("OTP:" + email.toLowerCase());
                emailService.sendEmail(email, "OTP Verification", "Your OTP is: " + otp);
            }
            else{
                otp=otpService.generateOTP();
                stringRedisTemplate.opsForValue().set("OTP:" + email.toLowerCase(), otp,120, TimeUnit.SECONDS);
                emailService.sendEmail(email, "OTP Verification", "Your OTP is: " + otp);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public LoginResponseDTO verifyOTP(OtpDto otp) throws OTPException,UserNotFound{
            String savedOtp=stringRedisTemplate.opsForValue().get("OTP:"+otp.getUserEmail().toLowerCase());
            String userOTP=otp.getOtp();
            if(userOTP.equals(savedOtp)){
                User user = userRepository.findByEmail(otp.getUserEmail().toLowerCase()).orElseThrow(()->new UserNotFound("User not found with email "));
                user.setEmailVerified(true);
                userRepository.save(user);
                return getLogin(user);
            }
            else{
                LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
                loginResponseDTO.setEmailVerified(false);
                return loginResponseDTO;
            }
    }
}





