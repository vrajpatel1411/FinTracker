package org.vrajpatel.userauthservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
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
import org.vrajpatel.userauthservice.Exception.TooManyAttemptException;
import org.vrajpatel.userauthservice.Exception.TooManyRequestException;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.OtpDto;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.LoginUserDto;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.OTPService.EmailService;
import org.vrajpatel.userauthservice.utils.OTPService.OTP;
import org.vrajpatel.userauthservice.utils.RefreshToken;

import java.time.Instant;
import java.util.UUID;
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
    public LoginResponseDTO loginService(LoginUserDto loginUserDto) throws UserNotFound, UnAuthorizedException {
        User user=userRepository.findByEmail(loginUserDto.getEmail().toLowerCase()).orElseThrow(()-> new UserNotFound("User Not Yet Registered"));
        if(user.getAuthProvider()!=AuthProvider.usernamepassword){
            throw new IllegalArgumentException("Try login with an Auth Provider");
        }
        if (passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword())) {
            if(user.isEmailVerified()){
                return getLogin(user);
            }else{
                sendOTP(loginUserDto.getEmail().toLowerCase());
                LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
                loginResponseDTO.setEmail(loginUserDto.getEmail().toLowerCase());
                loginResponseDTO.setEmailVerified(false);
                return loginResponseDTO;
            }
        } else {
            throw new UnAuthorizedException("Unauthorized Access, Password is wrong");
        }
    }

    private LoginResponseDTO getLogin(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getUserId(), user.getEmail());
        String refreshTokenKey = tokenProvider.generateRefreshToken(user.getUserId(), user.getEmail());
        refreshToken.setRefreshToken(refreshTokenKey, user.getEmail());
        LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        loginResponseDTO.setRefreshToken(refreshTokenKey);
        loginResponseDTO.setEmailVerified(true);
        return loginResponseDTO;
    }

    @Transactional
    public LoginResponseDTO registerService(RegisterUserDto userDTO) throws Exception {
        boolean isUser = userRepository.existsByEmail(userDTO.getEmail().toLowerCase());
        if (isUser) {
            throw new UserExistException("Email Is Already Registered");
        } else {
            User user = new User();
            user.setEmail(userDTO.getEmail().toLowerCase());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setFirstName(userDTO.getFirstName());
            user.setCreatedAt(Instant.now());
            user.setAuthProvider(AuthProvider.usernamepassword);
            user=userRepository.save(user);
            boolean otpStatus=sendOTP(userDTO.getEmail().toLowerCase());
            if(!otpStatus){
                logger.warn("Issue Sending OTP");
                throw new RuntimeException("Issue sending OTP");
            }
            LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
            loginResponseDTO.setEmail(user.getEmail().toLowerCase());
            loginResponseDTO.setEmailVerified(false);
            return loginResponseDTO;
        }
    }

    public String getNewAccessToken(String refreshToken) throws UserNotFound, UnAuthorizedException {
            if (StringUtils.hasText(refreshToken) && tokenProvider.validateRefreshToken(refreshToken)) {
                String email = stringRedisTemplate.opsForValue().get("refresh_token : " + refreshToken);
                if (email != null) {
                    UUID userId= tokenProvider.getUserIdfromRefreshToken(refreshToken);
                    if (userId!=null) {
                        return tokenProvider.generateAccessToken(userId, email);
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

    public Claims validate(String accessToken) throws UnAuthorizedException, ExpiredJwtException {
        if (StringUtils.hasText(accessToken)) {
            return tokenProvider.validateandExtractToken(accessToken);
        }
        else{
            throw new UnAuthorizedException("Unauthorized Access");
        }
    }

    public boolean sendOTP(String email) throws TooManyRequestException {
        String key = "resendOTPCount : " + email.toLowerCase();
        Boolean isNew= stringRedisTemplate.opsForValue().setIfAbsent(key,"1",600, TimeUnit.SECONDS);
        long count = isNew ? 1L: stringRedisTemplate.opsForValue().increment(key);
        if (count > 5) {
            throw new TooManyRequestException("Too many resend attempts. Try again later.");
        }
        try {
            String otp = otpService.generateOTP();
            stringRedisTemplate.opsForValue().set("OTP : " + email.toLowerCase(), otp, 120, TimeUnit.SECONDS);
            emailService.sendEmail(email, "OTP Verification", "Your OTP is: " + otp);
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}", email);
            throw new RuntimeException("Issue sending OTP", e);
        }
        return true;
    }

    public void logout(String refreshTokenValue) {
        if (StringUtils.hasText(refreshTokenValue)) {
            refreshToken.deleteRefreshToken(refreshTokenValue);
        }
    }

    public LoginResponseDTO verifyOTP(OtpDto otp) throws OTPException, UserNotFound, TooManyAttemptException {
        String savedOtp = stringRedisTemplate.opsForValue().get("OTP : " + otp.getUserEmail().toLowerCase());
        if (savedOtp == null) {
            throw new OTPException("OTP has expired. Please request a new one.");
        }
        if (!otp.getOtp().equals(savedOtp)) {
            String key = "OTPAttempt : " + otp.getUserEmail().toLowerCase();
            Boolean isNew= stringRedisTemplate.opsForValue().setIfAbsent(key,"1",120, TimeUnit.SECONDS);
            long attempt = isNew ? 1L: stringRedisTemplate.opsForValue().increment(key);
            if(attempt >5){
                stringRedisTemplate.delete(key);
                stringRedisTemplate.delete("OTP : "+otp.getUserEmail().toLowerCase());
                throw new TooManyAttemptException("Too many attempts, Request New OTP");
            }
            throw new OTPException("Invalid OTP.");
        }
        User user = userRepository.findByEmail(otp.getUserEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFound("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        stringRedisTemplate.delete("OTP : " + otp.getUserEmail().toLowerCase());
        return getLogin(user);
    }
}





