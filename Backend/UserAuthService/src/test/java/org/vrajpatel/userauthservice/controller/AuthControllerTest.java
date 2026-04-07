package org.vrajpatel.userauthservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.Exception.GlobalExceptionHandler;
import org.vrajpatel.userauthservice.Exception.OTPException;
import org.vrajpatel.userauthservice.Exception.TooManyAttemptException;
import org.vrajpatel.userauthservice.Exception.TooManyRequestException;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.SetCookies;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer unit tests using standalone MockMvc (no Spring context).
 * Security filters are intentionally excluded; they are covered by AuthIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @Mock TokenProvider tokenProvider;
    @Mock SetCookies setCookies;
    @InjectMocks AuthController authController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.add(HttpHeaders.SET_COOKIE, "accessToken=tok; HttpOnly; Path=/");
        cookieHeaders.add(HttpHeaders.SET_COOKIE, "refreshToken=rtok; HttpOnly; Path=/");
        lenient().when(setCookies.setCookies(any(HttpHeaders.class), anyString(), anyString()))
                .thenReturn(cookieHeaders);
        lenient().when(setCookies.getAccessCookie(anyString()))
                .thenReturn("accessToken=new; HttpOnly; Path=/");
        lenient().when(setCookies.clearAccessCookie())
                .thenReturn("accessToken=; Max-Age=0; Path=/");
        lenient().when(setCookies.clearRefreshCookie())
                .thenReturn("refreshToken=; Max-Age=0; Path=/");
    }

    // ── POST /login ──────────────────────────────────────────────────────────

    @Test
    void login_emailVerified_returns200WithCookiesAndStatusTrue() throws Exception {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEmailVerified(true);
        dto.setAccessToken("access-tok");
        dto.setRefreshToken("refresh-tok");
        when(authService.loginService(any())).thenReturn(dto);

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Successfully logged in"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void login_emailNotVerified_returns200WithNeedEmailVerification() throws Exception {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEmailVerified(false);
        dto.setEmail("test@example.com");
        when(authService.loginService(any())).thenReturn(dto);

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.needEmailVerification").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_userNotFound_returns404() throws Exception {
        when(authService.loginService(any())).thenThrow(new UserNotFound("not found"));

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "missing@example.com", "password", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(authService.loginService(any())).thenThrow(new UnAuthorizedException("bad pw"));

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized, Wrong Password"));
    }

    @Test
    void login_tooManyOtpRequests_returns429() throws Exception {
        when(authService.loginService(any())).thenThrow(new TooManyRequestException("Too many requests"));

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com", "password", "x"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.needEmailVerification").value(true));
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email", "password", "x"))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /register ───────────────────────────────────────────────────────

    @Test
    void register_newUser_returns200WithEmailVerificationRequired() throws Exception {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEmailVerified(false);
        dto.setEmail("new@example.com");
        when(authService.registerService(any())).thenReturn(dto);

        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "new@example.com", "password", "password1", "firstName", "New"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needEmailVerification").value(true))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void register_existingEmail_returns409() throws Exception {
        when(authService.registerService(any())).thenThrow(new UserExistException("exists"));

        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@example.com", "password", "password1", "firstName", "T"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void register_missingFirstName_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "a@b.com", "password", "password1"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "a@b.com", "password", "short", "firstName", "X"))))
                .andExpect(status().isBadRequest());
    }

    // ── GET /validate (cookie-based) ─────────────────────────────────────────

    @Test
    void validateGet_validAccessToken_returns200Valid() throws Exception {
        when(tokenProvider.validateandExtractToken("good-token")).thenReturn(mock(Claims.class));

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("accessToken", "good-token"))
                        .cookie(new Cookie("refreshToken", "r-tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateGet_noAccessToken_usesRefreshTokenAndReturnsCookie() throws Exception {
        when(authService.getNewAccessToken("r-tok")).thenReturn("new-access");

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("refreshToken", "r-tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void validateGet_noAccessToken_noCookieEither_returns401() throws Exception {
        mockMvc.perform(get("/userauth/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validateGet_noAccessToken_invalidRefresh_returns401() throws Exception {
        when(authService.getNewAccessToken("bad-r")).thenThrow(new UnAuthorizedException("invalid"));

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("refreshToken", "bad-r")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validateGet_refreshTokenLeadsToUserNotFound_returns404() throws Exception {
        when(authService.getNewAccessToken("r-tok")).thenThrow(new UserNotFound("gone"));

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("refreshToken", "r-tok")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ── POST /validate (body JWT) ────────────────────────────────────────────

    @Test
    void validatePost_validJwt_returns200WithUserInfo() throws Exception {
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn(userId.toString());
        when(mockClaims.get("email", String.class)).thenReturn("test@example.com");
        when(authService.validate("good-jwt")).thenReturn(mockClaims);

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"good-jwt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void validatePost_claimsHaveNoEmail_returns401() throws Exception {
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn(userId.toString());
        when(mockClaims.get("email", String.class)).thenReturn(null);
        when(authService.validate("jwt-no-email")).thenReturn(mockClaims);

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"jwt-no-email\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validatePost_invalidJwt_returns401() throws Exception {
        when(authService.validate("bad-jwt")).thenThrow(new BadRequestException("invalid token"));

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"bad-jwt\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ── POST /getNewAccessToken ──────────────────────────────────────────────

    @Test
    void getNewAccessToken_validRefresh_returns200WithTokenAndUserInfo() throws Exception {
        when(authService.getNewAccessToken("r-tok")).thenReturn("new-a-tok");
        when(tokenProvider.getUserIdFromJWT("new-a-tok")).thenReturn(userId);
        when(tokenProvider.getEmailFromJWT("new-a-tok")).thenReturn("test@example.com");

        mockMvc.perform(post("/userauth/api/auth/getNewAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"r-tok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-a-tok"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void getNewAccessToken_serviceThrows_propagates() throws Exception {
        when(authService.getNewAccessToken(anyString())).thenThrow(new UnAuthorizedException("expired"));

        mockMvc.perform(post("/userauth/api/auth/getNewAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"bad-r\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /verifyOtp ──────────────────────────────────────────────────────

    @Test
    void verifyOtp_correctOtp_returns200WithCookies() throws Exception {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEmailVerified(true);
        dto.setAccessToken("a-tok");
        dto.setRefreshToken("r-tok");
        when(authService.verifyOTP(any())).thenReturn(dto);

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("otp", "123456", "userEmail", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void verifyOtp_expiredOtp_returns400WithMessage() throws Exception {
        when(authService.verifyOTP(any())).thenThrow(new OTPException("OTP has expired. Please request a new one."));

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("otp", "123456", "userEmail", "test@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("OTP has expired. Please request a new one."));
    }

    @Test
    void verifyOtp_wrongOtp_returns400WithMessage() throws Exception {
        when(authService.verifyOTP(any())).thenThrow(new OTPException("Invalid OTP."));

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("otp", "000000", "userEmail", "test@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP."));
    }

    @Test
    void verifyOtp_tooManyAttempts_returns429() throws Exception {
        when(authService.verifyOTP(any())).thenThrow(new TooManyAttemptException("Too many attempts, Request New OTP"));

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("otp", "000000", "userEmail", "test@example.com"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void verifyOtp_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("otp", "123456", "userEmail", "not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /logout ─────────────────────────────────────────────────────────

    @Test
    void logout_withRefreshCookie_clearsSessionAndReturns200() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "r-tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout("r-tok");
    }

    @Test
    void logout_withNoRefreshCookie_stillReturns200() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(authService).logout(null);
    }

    // ── POST /resendOtp ──────────────────────────────────────────────────────

    @Test
    void resendOtp_success_returns200WithStatusTrue() throws Exception {
        when(authService.sendOTP("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void resendOtp_tooManyRequests_returns429() throws Exception {
        when(authService.sendOTP("test@example.com"))
                .thenThrow(new TooManyRequestException("Too many resend attempts. Try again later."));

        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void resendOtp_smtpError_returns500() throws Exception {
        when(authService.sendOTP(anyString())).thenThrow(new RuntimeException("SMTP down"));

        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void resendOtp_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-valid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendOtp_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
