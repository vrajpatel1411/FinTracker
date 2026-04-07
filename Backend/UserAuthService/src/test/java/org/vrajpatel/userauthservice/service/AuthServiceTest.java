package org.vrajpatel.userauthservice.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshToken refreshToken;
    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock TokenProvider tokenProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock OTP otpService;
    @Mock EmailService emailService;

    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks AuthService authService;

    private User verifiedUser;
    private User unverifiedUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        verifiedUser = new User();
        verifiedUser.setUserId(userId);
        verifiedUser.setEmail("test@example.com");
        verifiedUser.setPassword("encodedPassword");
        verifiedUser.setFirstName("Test");
        verifiedUser.setEmailVerified(true);
        verifiedUser.setAuthProvider(AuthProvider.usernamepassword);
        verifiedUser.setCreatedAt(new Date().toInstant());

        unverifiedUser = new User();
        unverifiedUser.setUserId(userId);
        unverifiedUser.setEmail("test@example.com");
        unverifiedUser.setPassword("encodedPassword");
        unverifiedUser.setFirstName("Test");
        unverifiedUser.setEmailVerified(false);
        unverifiedUser.setAuthProvider(AuthProvider.usernamepassword);
        unverifiedUser.setCreatedAt(new Date().toInstant());

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // Convenience: stub the rate-limit setIfAbsent so sendOTP doesn't NPE
    private void stubSendOtpRateLimit(String email) {
        lenient().when(valueOps.setIfAbsent(
                eq("resendOTPCount : " + email), eq("1"), eq(600L), eq(TimeUnit.SECONDS))
        ).thenReturn(true);
    }

    // ── loginService ─────────────────────────────────────────────────────────

    @Test
    void loginService_emailVerifiedCorrectPassword_returnsTokens() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("access-tok");
        when(tokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("refresh-tok");

        LoginResponseDTO result = authService.loginService(new LoginUserDto("test@example.com", "secret"));

        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo("access-tok");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-tok");
        verify(refreshToken).setRefreshToken("refresh-tok", "test@example.com");
    }

    @Test
    void loginService_emailNotVerified_sendsOtpAndReturnsFalse() throws Exception {
        stubSendOtpRateLimit("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);
        when(otpService.generateOTP()).thenReturn("123456");

        LoginResponseDTO result = authService.loginService(new LoginUserDto("test@example.com", "secret"));

        assertThat(result.isEmailVerified()).isFalse();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(valueOps).set(eq("OTP : test@example.com"), eq("123456"), eq(120L), eq(TimeUnit.SECONDS));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), contains("123456"));
    }

    @Test
    void loginService_userNotFound_throwsUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginService(new LoginUserDto("missing@example.com", "pw")))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    void loginService_wrongPassword_throwsUnAuthorizedException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginService(new LoginUserDto("test@example.com", "wrong")))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    void loginService_oauthProvider_throwsIllegalArgumentException() {
        verifiedUser.setAuthProvider(AuthProvider.google);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(verifiedUser));

        assertThatThrownBy(() -> authService.loginService(new LoginUserDto("test@example.com", "pw")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── registerService ──────────────────────────────────────────────────────

    @Test
    void registerService_newUser_savesUserAndSendsOtp() throws Exception {
        stubSendOtpRateLimit("new@example.com");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(UUID.randomUUID());
            return u;
        });
        when(otpService.generateOTP()).thenReturn("654321");

        RegisterUserDto dto = new RegisterUserDto();
        dto.setEmail("new@example.com");
        dto.setPassword("pass1234");
        dto.setFirstName("New");

        LoginResponseDTO result = authService.registerService(dto);

        assertThat(result.isEmailVerified()).isFalse();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(eq("new@example.com"), anyString(), contains("654321"));
    }

    @Test
    void registerService_existingEmail_throwsUserExistException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        RegisterUserDto dto = new RegisterUserDto();
        dto.setEmail("test@example.com");
        dto.setPassword("pass1234");
        dto.setFirstName("Dup");

        assertThatThrownBy(() -> authService.registerService(dto))
                .isInstanceOf(UserExistException.class);
    }

    // ── getNewAccessToken ────────────────────────────────────────────────────

    @Test
    void getNewAccessToken_validRefreshToken_returnsNewAccessToken() throws Exception {
        String refreshTok = "valid-refresh";
        when(tokenProvider.validateRefreshToken(refreshTok)).thenReturn(true);
        when(valueOps.get("refresh_token : " + refreshTok)).thenReturn("test@example.com");
        when(tokenProvider.getUserIdfromRefreshToken(refreshTok)).thenReturn(userId);
        when(tokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("new-access-tok");

        String result = authService.getNewAccessToken(refreshTok);

        assertThat(result).isEqualTo("new-access-tok");
    }

    @Test
    void getNewAccessToken_emptyToken_throwsUnAuthorizedException() {
        assertThatThrownBy(() -> authService.getNewAccessToken(""))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    void getNewAccessToken_invalidSignature_throwsUnAuthorizedException() {
        when(tokenProvider.validateRefreshToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.getNewAccessToken("bad-token"))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    void getNewAccessToken_emailNullInRedis_throwsUnAuthorizedException() {
        String refreshTok = "refresh-no-email";
        when(tokenProvider.validateRefreshToken(refreshTok)).thenReturn(true);
        when(valueOps.get("refresh_token : " + refreshTok)).thenReturn(null);

        assertThatThrownBy(() -> authService.getNewAccessToken(refreshTok))
                .isInstanceOf(UnAuthorizedException.class);
    }

    // ── validate ─────────────────────────────────────────────────────────────

    @Test
    void validate_validToken_returnsClaims() throws Exception {
        Claims mockClaims = mock(Claims.class);
        when(tokenProvider.validateandExtractToken("valid-token")).thenReturn(mockClaims);

        Claims result = authService.validate("valid-token");

        assertThat(result).isEqualTo(mockClaims);
    }

    @Test
    void validate_emptyToken_throwsUnAuthorizedException() {
        assertThatThrownBy(() -> authService.validate(""))
                .isInstanceOf(UnAuthorizedException.class);
        assertThatThrownBy(() -> authService.validate(null))
                .isInstanceOf(UnAuthorizedException.class);
    }

    // ── sendOTP ──────────────────────────────────────────────────────────────

    @Test
    void sendOTP_firstCall_generatesAndCachesOtp() {
        when(valueOps.setIfAbsent(eq("resendOTPCount : test@example.com"), eq("1"), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(otpService.generateOTP()).thenReturn("111222");

        boolean result = authService.sendOTP("test@example.com");

        assertThat(result).isTrue();
        verify(valueOps).set(eq("OTP : test@example.com"), eq("111222"), eq(120L), eq(TimeUnit.SECONDS));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), contains("111222"));
    }

    @Test
    void sendOTP_subsequentCall_incrementsCounterAndSendsOtp() {
        when(valueOps.setIfAbsent(eq("resendOTPCount : test@example.com"), eq("1"), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);
        when(valueOps.increment("resendOTPCount : test@example.com")).thenReturn(2L);
        when(otpService.generateOTP()).thenReturn("999888");

        boolean result = authService.sendOTP("test@example.com");

        assertThat(result).isTrue();
        verify(valueOps).set(eq("OTP : test@example.com"), eq("999888"), eq(120L), eq(TimeUnit.SECONDS));
    }

    @Test
    void sendOTP_rateLimitExceeded_throwsTooManyRequestException() {
        when(valueOps.setIfAbsent(eq("resendOTPCount : test@example.com"), eq("1"), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);
        when(valueOps.increment("resendOTPCount : test@example.com")).thenReturn(6L);

        assertThatThrownBy(() -> authService.sendOTP("test@example.com"))
                .isInstanceOf(TooManyRequestException.class);
        verify(otpService, never()).generateOTP();
    }

    @Test
    void sendOTP_emailServiceThrows_propagatesRuntimeExceptionWithCause() {
        when(valueOps.setIfAbsent(eq("resendOTPCount : test@example.com"), eq("1"), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(otpService.generateOTP()).thenReturn("111222");
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> authService.sendOTP("test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Issue sending OTP")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ── verifyOTP ────────────────────────────────────────────────────────────

    @Test
    void verifyOTP_correctOtp_setsEmailVerifiedAndReturnsTokens() throws Exception {
        OtpDto dto = new OtpDto();
        dto.setOtp("123456");
        dto.setUserEmail("test@example.com");

        when(valueOps.get("OTP : test@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("a-tok");
        when(tokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("r-tok");

        LoginResponseDTO result = authService.verifyOTP(dto);

        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo("a-tok");
        verify(userRepository).save(argThat(User::isEmailVerified));
        verify(stringRedisTemplate).delete("OTP : test@example.com");
    }

    @Test
    void verifyOTP_expiredOtp_throwsOTPException() {
        OtpDto dto = new OtpDto();
        dto.setOtp("123456");
        dto.setUserEmail("test@example.com");

        when(valueOps.get("OTP : test@example.com")).thenReturn(null);

        assertThatThrownBy(() -> authService.verifyOTP(dto))
                .isInstanceOf(OTPException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyOTP_wrongOtp_throwsOTPException() {
        OtpDto dto = new OtpDto();
        dto.setOtp("000000");
        dto.setUserEmail("test@example.com");

        when(valueOps.get("OTP : test@example.com")).thenReturn("123456");
        when(valueOps.setIfAbsent(eq("OTPAttempt : test@example.com"), eq("1"), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(true); // first attempt

        assertThatThrownBy(() -> authService.verifyOTP(dto))
                .isInstanceOf(OTPException.class)
                .hasMessageContaining("Invalid OTP");
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOTP_tooManyFailedAttempts_throwsTooManyAttemptException() {
        OtpDto dto = new OtpDto();
        dto.setOtp("000000");
        dto.setUserEmail("test@example.com");

        when(valueOps.get("OTP : test@example.com")).thenReturn("123456");
        when(valueOps.setIfAbsent(eq("OTPAttempt : test@example.com"), eq("1"), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(false); // key already exists
        when(valueOps.increment("OTPAttempt : test@example.com")).thenReturn(6L);

        assertThatThrownBy(() -> authService.verifyOTP(dto))
                .isInstanceOf(TooManyAttemptException.class);
        verify(stringRedisTemplate).delete("OTPAttempt : test@example.com");
        verify(stringRedisTemplate).delete("OTP : test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOTP_userNotFound_throwsUserNotFound() {
        OtpDto dto = new OtpDto();
        dto.setOtp("123456");
        dto.setUserEmail("ghost@example.com");

        when(valueOps.get("OTP : ghost@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyOTP(dto))
                .isInstanceOf(UserNotFound.class);
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    void logout_withToken_deletesRefreshToken() {
        authService.logout("some-refresh-token");

        verify(refreshToken).deleteRefreshToken("some-refresh-token");
    }

    @Test
    void logout_withBlankToken_doesNothing() {
        authService.logout("");
        authService.logout(null);

        verify(refreshToken, never()).deleteRefreshToken(any());
    }
}
