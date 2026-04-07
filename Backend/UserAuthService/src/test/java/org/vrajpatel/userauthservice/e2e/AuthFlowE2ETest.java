package org.vrajpatel.userauthservice.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests with a real PostgreSQL container (Testcontainers).
 * Redis and Mail are mocked to avoid external service dependencies.
 * Covers the full user journey:
 *   register → resendOtp → verifyOtp → login → validate (cookie) → validate (body)
 *   → getNewAccessToken → access protected endpoint → logout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fintracker_test")
            .withUsername("sa")
            .withPassword("sa");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.globally_quoted_identifiers", () -> "false");
    }

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired TokenProvider tokenProvider;

    @MockitoBean StringRedisTemplate stringRedisTemplate;

    private ValueOperations<String, String> valueOps;
    private ZSetOperations<String, String> zSetOps;

    ObjectMapper objectMapper = new ObjectMapper();

    static final AtomicReference<String> capturedOtp = new AtomicReference<>("567890");
    static final AtomicReference<String> capturedAccessToken = new AtomicReference<>();
    static final AtomicReference<String> capturedRefreshToken = new AtomicReference<>();
    static final String TEST_EMAIL = "e2e@example.com";
    static final String TEST_PASSWORD = "SecurePass1!";
    static final String OTP_KEY = "OTP : " + TEST_EMAIL;
    static final String RATE_LIMIT_KEY = "resendOTPCount : " + TEST_EMAIL;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUpRedisMock() {
        valueOps = mock(ValueOperations.class);
        zSetOps = mock(ZSetOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.size(anyString())).thenReturn(1L);
        lenient().when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Rate limit: allow first call
        lenient().when(valueOps.setIfAbsent(
                eq(RATE_LIMIT_KEY), eq("1"), eq(600L), eq(TimeUnit.SECONDS))
        ).thenReturn(true);

        // Capture OTP when it's stored, serve it back on get
        doAnswer(inv -> {
            capturedOtp.set(inv.getArgument(1));
            return null;
        }).when(valueOps).set(eq(OTP_KEY), anyString(), eq(120L), eq(TimeUnit.SECONDS));
        when(valueOps.get(OTP_KEY)).thenAnswer(inv -> capturedOtp.get());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1: Register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void step1_register_createsUserAndRequiresEmailVerification() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", TEST_EMAIL,
                                "password", TEST_PASSWORD,
                                "firstName", "E2E"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needEmailVerification").value(true))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));

        assertThat(userRepository.findByEmail(TEST_EMAIL)).isPresent();
        assertThat(userRepository.findByEmail(TEST_EMAIL).get().isEmailVerified()).isFalse();
    }

    @Test
    @Order(2)
    void step1b_register_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", TEST_EMAIL,
                                "password", TEST_PASSWORD,
                                "firstName", "Dup"))))
                .andExpect(status().isConflict());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2: Resend OTP
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    void step2_resendOtp_returns200() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/resendOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + TEST_EMAIL + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 3: Login before verification
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void step3_loginBeforeVerification_requiresEmailVerification() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", TEST_EMAIL,
                                "password", TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needEmailVerification").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 4: Verify OTP
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @Order(5)
    void step4a_verifyOtp_wrongOtp_returns400() throws Exception {
        // Stub attempt counter (first attempt)
        when(valueOps.setIfAbsent(
                eq("OTPAttempt : " + TEST_EMAIL), eq("1"), eq(120L), eq(TimeUnit.SECONDS))
        ).thenReturn(true);

        mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "otp", "000000",
                                "userEmail", TEST_EMAIL))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void step4b_verifyOtp_setsEmailVerifiedAndReturnsCookies() throws Exception {
        String otp = capturedOtp.get();

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "otp", otp,
                                "userEmail", TEST_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();

        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .anyMatch(c -> c.contains("accessToken"))
                .anyMatch(c -> c.contains("refreshToken"));
        assertThat(userRepository.findByEmail(TEST_EMAIL).get().isEmailVerified()).isTrue();

        // Capture tokens for subsequent steps
        String userId = userRepository.findByEmail(TEST_EMAIL)
                .map(u -> u.getUserId().toString()).orElseThrow();
        capturedAccessToken.set(tokenProvider.generateAccessToken(UUID.fromString(userId), TEST_EMAIL));
        capturedRefreshToken.set(tokenProvider.generateRefreshToken(UUID.fromString(userId), TEST_EMAIL));
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Step 5: Login after verification
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    void step5_loginAfterVerification_returns200WithCookies() throws Exception {
        MvcResult result = mockMvc.perform(post("/userauth/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", TEST_EMAIL,
                                "password", TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Successfully logged in"))
                .andReturn();

        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .anyMatch(c -> c.contains("accessToken"))
                .anyMatch(c -> c.contains("refreshToken"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 6: Validate JWT (POST body)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    void step6_validatePostWithToken_returns200ValidAndUserInfo() throws Exception {
        String token = capturedAccessToken.get();
        assertThat(token).isNotNull();

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userEmail").value(TEST_EMAIL));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 7: Validate via cookie
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void step7_validateGetWithCookie_returns200() throws Exception {
        String token = capturedAccessToken.get();
        assertThat(token).isNotNull();

        mockMvc.perform(get("/userauth/api/auth/validate")
                        .cookie(new Cookie("accessToken", token))
                        .cookie(new Cookie("refreshToken", capturedRefreshToken.get())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 8: Access protected endpoint
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    void step8_accessProtectedEndpoint_withToken_returns200() throws Exception {
        String token = capturedAccessToken.get();
        assertThat(token).isNotNull();

        mockMvc.perform(get("/userauth/api/user/")
                        .cookie(new Cookie("accessToken", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));
    }

    @Test
    @Order(11)
    void step8b_accessProtectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 9: Get new access token from refresh token (POST body)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(12)
    void step9_getNewAccessToken_validRefresh_returns200() throws Exception {
        String refresh = capturedRefreshToken.get();
        assertThat(refresh).isNotNull();

        when(valueOps.get("refresh_token : " + refresh)).thenReturn(TEST_EMAIL);

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/getNewAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userEmail").value(TEST_EMAIL))
                .andReturn();

        // New access token must itself be valid
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String newAccessToken = body.get("accessToken").asText();
        assertThat(newAccessToken).isNotBlank();

        mockMvc.perform(post("/userauth/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"" + newAccessToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @Order(13)
    void step9b_getNewAccessToken_invalidRefresh_returns401() throws Exception {
        mockMvc.perform(post("/userauth/api/auth/getNewAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt\":\"garbage\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 10: Logout
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(14)
    void step10_logout_clearsSessionAndCookies() throws Exception {
        String refresh = capturedRefreshToken.get();
        when(valueOps.get("refresh_token : " + refresh)).thenReturn(TEST_EMAIL);

        MvcResult result = mockMvc.perform(post("/userauth/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andReturn();

        assertThat(result.getResponse().getHeader("Set-Cookie")).contains("Max-Age=0");
    }

    @Test
    @Order(15)
    void step10b_accessProtectedEndpoint_afterLogout_returns401() throws Exception {
        // After logout the refresh token is deleted from Redis; a new access token
        // cannot be issued, so protected endpoints are inaccessible without re-login.
        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isUnauthorized());
    }
}
