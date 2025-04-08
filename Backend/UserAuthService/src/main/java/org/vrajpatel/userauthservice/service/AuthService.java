package org.vrajpatel.userauthservice.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.UserPrincipal;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<AuthResponse> loginService(@Valid UserDTO userDTO) {


        Optional<User> user=userRepository.findByEmail(userDTO.getEmail().toLowerCase());
        
        if(user.isPresent()){
            if(passwordEncoder.matches(userDTO.getPassword(), user.get().getPassword())){
                UserPrincipal userPrincipal=UserPrincipal.create(user.get());
                Authentication authentication=new UsernamePasswordAuthenticationToken(userPrincipal,new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt= tokenProvider.createJWT(authentication);
                ResponseCookie cookie=ResponseCookie.from("jwttoken",jwt)
                        .httpOnly(true)
                        .maxAge(3600)
                        .sameSite("None")
                        .domain("localhost")
                        .secure(true)
                        .path("/")
                        .build();

                AuthResponse authResponse=new AuthResponse();
                authResponse.setStatus(true);
                authResponse.setMessage("Login successful");

//                HttpHeaders httpHeaders=new HttpHeaders();
//                httpHeaders.set(HttpHeaders.SET_COOKIE,cookie.toString());
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString()).body(authResponse);
            }
            else{
                AuthResponse authResponse=new AuthResponse();
                authResponse.setStatus(false);
                authResponse.setMessage("Sorry, wrong password");
                return new ResponseEntity<>(authResponse, HttpStatus.UNAUTHORIZED);
            }
        }
        else{
            AuthResponse authResponse=new AuthResponse();
            authResponse.setStatus(false);
            authResponse.setMessage("Please create a account");
            return new ResponseEntity<>(authResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<AuthResponse> registerService(@Valid RegisterUserDto userDTO) {

        boolean isUser=userRepository.existsByEmail(userDTO.getEmail().toLowerCase());

        if(isUser){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setStatus(false);
            authResponse.setMessage("Email is already registered");
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST);
        }
        else{
            User user=new User();
            user.setEmail(userDTO.getEmail().toLowerCase());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setCreatedAt(new Date());
            user.setAuthProvider(AuthProvider.usernamepassword);
            userRepository.save(user);

            UserPrincipal userPrincipal=UserPrincipal.create(user);
            Authentication authentication=new UsernamePasswordAuthenticationToken(userPrincipal,new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt= tokenProvider.createJWT(authentication);
            AuthResponse authResponse=new AuthResponse();
            authResponse.setStatus(true);
            authResponse.setMessage("User Registration successful");
            ResponseCookie cookie=ResponseCookie.from("jwttoken",jwt)
                    .httpOnly(true)
                    .maxAge(3600)
                    .sameSite("None")
                    .domain("localhost")
                    .secure(true)
                    .path("/")
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString()).body(authResponse);

        }
    }
}
