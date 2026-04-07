package org.vrajpatel.userauthservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests: full Spring context (H2 DB) + mocked Redis and Mail.
 * Verifies that controllers, services, and repositories wire together correctly,
 * and that security filter behaviour is correct for protected/public endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired TokenProvider tokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper = new ObjectMapper();

    ValueOperations<String, String> valueOps;
    ZSetOperations<String, String> zSetOps;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        valueOps = mock(ValueOperations.class);
        zSetOps = mock(ZSetOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.size(anyString())).thenReturn(1L);
        lenient().when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Default: sendOTP rate limit not exceeded (first call)
        lenient().when(valueOps.setIfAbsent(
                startsWith("resendOTPCount : "), eq("1"), eq(600L), eq(TimeUnit.SECONDS))
        ).thenReturn(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registration flow
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_newUser_createsUserInDbAndSendsOtp() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "john@example.com", "password", "pass1234", "firstName", "John"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needEmailVerification").value(true))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        createSavedUser("dup@example.com", true);

        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "dup@example.com", "password", "password1", "firstName", "D"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login flow
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void login_verifiedUser_returns200WithJwtCookies() throws Exception {
        createSavedUser("alice@example.com", true);

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "alice@example.com",
                                "password", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();

        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .anyMatch(c -> c.contains("accessToken"))
                .anyMatch(c -> c.contains("refreshToken"));
    }

    @Test
    void login_unverifiedUser_returnsNeedEmailVerification() throws Exception {
        createSavedUser("bob@example.com", false);
        when(valueOps.get("OTP : bob@example.com")).thenReturn("999999");

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "bob@example.com",
                                "password", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needEmailVerification").value(true));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        createSavedUser("carol@example.com", true);

        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "carol@example.com",
                                "password", "WRONG"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns404() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "nobody@example.com",
                                "password", "pw"))))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OTP flow
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void resendOtp_success_returns200() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"otp@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void resendOtp_rateLimitExceeded_returns429() throws Exception {
        when(valueOps.setIfAbsent(
                eq("resendOTPCount : ratelimit@example.com"), eq("1"), eq(600L), eq(TimeUnit.SECONDS))
        ).thenReturn(false);
        when(valueOps.increment("resendOTPCount : ratelimit@example.com")).thenReturn(6L);

        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ratelimit@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void verifyOtp_correctOtp_setsEmailVerifiedAndReturnsCookies() throws Exception {
        createSavedUser("verify@example.com", false);
        when(valueOps.get("OTP : verify@example.com")).thenReturn("123456");

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "otp", "123456",
                                "userEmail", "verify@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();

        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .anyMatch(c -> c.contains("accessToken"))
                .anyMatch(c -> c.contains("refreshToken"));
        assertThat(userRepository.findByEmail("verify@example.com").orElseThrow().isEmailVerified()).isTrue();
    }

    @Test
    void verifyOtp_wrongOtp_returns400() throws Exception {
        createSavedUser("wrongotp@example.com", false);
        when(valueOps.get("OTP : wrongotp@example.com")).thenReturn("123456");
        when(valueOps.setIfAbsent(
                eq("OTPAttempt : wrongotp@example.com"), eq("1"), eq(120L), eq(TimeUnit.SECONDS))
        ).thenReturn(true);

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "otp", "000000",
                                "userEmail", "wrongotp@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void verifyOtp_tooManyAttempts_returns429() throws Exception {
        createSavedUser("locked@example.com", false);
        when(valueOps.get("OTP : locked@example.com")).thenReturn("123456");
        when(valueOps.setIfAbsent(
                eq("OTPAttempt : locked@example.com"), eq("1"), eq(120L), eq(TimeUnit.SECONDS))
        ).thenReturn(false);
        when(valueOps.increment("OTPAttempt : locked@example.com")).thenReturn(6L);

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "otp", "000000",
                                "userEmail", "locked@example.com"))))
                .andExpect(status().isTooManyRequests());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /validate (body JWT)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void validatePost_validToken_returns200WithEmailAndId() throws Exception {
        User user = createSavedUser("valid@example.com", true);
        String token = tokenProvider.generateAccessToken(user.getUserId(), user.getEmail());

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userEmail").value("valid@example.com"));
    }

    @Test
    void validatePost_invalidToken_returns401() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"this.is.garbage\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /validate (cookie-based with auto-refresh)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void validateGet_withValidAccessCookie_returns200() throws Exception {
        User user = createSavedUser("cookie@example.com", true);
        String accessToken = tokenProvider.generateAccessToken(user.getUserId(), user.getEmail());

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("accessToken", accessToken))
                        .cookie(new Cookie("refreshToken", "r-tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateGet_expiredAccess_validRefresh_issuesNewAccessCookie() throws Exception {
        User user = createSavedUser("refresh@example.com", true);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUserId(), user.getEmail());

        when(valueOps.get("refresh_token : " + refreshToken)).thenReturn("refresh@example.com");

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(header().exists("Set-Cookie"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Security filter
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        User user = createSavedUser("me@example.com", true);
        String token = tokenProvider.generateAccessToken(user.getUserId(), user.getEmail());

        mockMvc.perform(get("/userauth/api/user/")
                        .cookie(new Cookie("accessToken", token)))
                .andExpect(status().isOk());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /getNewAccessToken
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getNewAccessToken_validRefresh_returns200WithNewToken() throws Exception {
        User user = createSavedUser("refresh2@example.com", true);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUserId(), user.getEmail());

        when(valueOps.get("refresh_token : " + refreshToken)).thenReturn("refresh2@example.com");

        mockMvc.perform(post("/userauth/api/auth/getNewAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userEmail").value("refresh2@example.com"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /logout
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void logout_withRefreshCookie_returns200AndClearsCookies() throws Exception {
        User user = createSavedUser("logout@example.com", true);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUserId(), user.getEmail());
        when(valueOps.get("refresh_token : " + refreshToken)).thenReturn("logout@example.com");

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();


        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull().contains("Max-Age=0");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private User createSavedUser(String email, boolean emailVerified) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password1!"));
        user.setFirstName("Test");
        user.setEmailVerified(emailVerified);
        user.setAuthProvider(AuthProvider.usernamepassword);
        user.setCreatedAt(new Date().toInstant());
        return userRepository.save(user);
    }
}
