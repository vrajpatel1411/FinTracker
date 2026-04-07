package org.vrajpatel.userauthservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TokenProviderTest {

    private static final String ACCESS_SECRET =
            "04ca023b39512e46d0c2cf4b48d5aac61d34302994c87ed4eff225dcf3b0a218739f3897051a057f9b846a69ea2927a587044164b7bae5e1306219d50b588cb1";
    private static final String REFRESH_SECRET =
            "913b81c967484371b627eb50711352eaa33ca042478dcb155f20ad093855d09e";

    private TokenProvider tokenProvider;
    private TokenProvider expiredTokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = buildProvider(300_000L, 864_000_000L);
        expiredTokenProvider = buildProvider(-1L, -1L);
    }

    private TokenProvider buildProvider(long accessExpiry, long refreshExpiry) {
        AppProperties props = new AppProperties();
        props.getAuth().setAccessTokenSecret(ACCESS_SECRET);
        props.getAuth().setAccessTokenExpirationMsec(accessExpiry);
        props.getAuth().setRefreshTokenSecret(REFRESH_SECRET);
        props.getAuth().setRefreshTokenExpirationMsec(refreshExpiry);
        return new TokenProvider(props);
    }

    // ── generateToken ────────────────────────────────────────────────────────

    @Test
    void generateAccessToken_returnsNonBlankJwt() {
        String token = tokenProvider.generateAccessToken(UUID.randomUUID(), "test@example.com");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateRefreshToken_returnsNonBlankJwt() {
        String token = tokenProvider.generateRefreshToken(UUID.randomUUID(), "test@example.com");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ── validateandExtractToken ──────────────────────────────────────────────

    @Test
    void validateandExtractToken_validToken_returnsClaims() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@example.com");

        Claims claims = tokenProvider.validateandExtractToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
    }

    @Test
    void validateandExtractToken_expiredToken_throwsExpiredJwtException() {
        String token = expiredTokenProvider.generateAccessToken(UUID.randomUUID(), "test@example.com");

        assertThatThrownBy(() -> tokenProvider.validateandExtractToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void validateandExtractToken_tamperedSignature_throwsBadRequestException() {
        String token = tokenProvider.generateAccessToken(UUID.randomUUID(), "test@example.com");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "."
                + parts[2].substring(0, parts[2].length() - 4) + "XXXX";

        assertThatThrownBy(() -> tokenProvider.validateandExtractToken(tampered))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateandExtractToken_malformedString_throwsBadRequestException() {
        assertThatThrownBy(() -> tokenProvider.validateandExtractToken("not.a.jwt"))
                .isInstanceOf(BadRequestException.class);
    }

    // ── claim extraction ─────────────────────────────────────────────────────

    @Test
    void getUserIdFromJWT_returnsCorrectUUID() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@example.com");

        assertThat(tokenProvider.getUserIdFromJWT(token)).isEqualTo(userId);
    }

    @Test
    void getEmailFromJWT_returnsCorrectEmail() {
        String token = tokenProvider.generateAccessToken(UUID.randomUUID(), "alice@example.com");

        assertThat(tokenProvider.getEmailFromJWT(token)).isEqualTo("alice@example.com");
    }

    @Test
    void getUserIdfromRefreshToken_returnsCorrectUUID() {
        UUID userId = UUID.randomUUID();
        String refresh = tokenProvider.generateRefreshToken(userId, "test@example.com");

        assertThat(tokenProvider.getUserIdfromRefreshToken(refresh)).isEqualTo(userId);
    }

    @Test
    void getEmailFromRefreshToken_returnsCorrectEmail() {
        String refresh = tokenProvider.generateRefreshToken(UUID.randomUUID(), "bob@example.com");

        assertThat(tokenProvider.getEmailFromRefreshToken(refresh)).isEqualTo("bob@example.com");
    }

    // ── validateRefreshToken ─────────────────────────────────────────────────

    @Test
    void validateRefreshToken_valid_returnsTrue() {
        String refresh = tokenProvider.generateRefreshToken(UUID.randomUUID(), "test@example.com");
        assertThat(tokenProvider.validateRefreshToken(refresh)).isTrue();
    }

    @Test
    void validateRefreshToken_expired_returnsFalse() {
        String expired = expiredTokenProvider.generateRefreshToken(UUID.randomUUID(), "test@example.com");
        assertThat(tokenProvider.validateRefreshToken(expired)).isFalse();
    }

    @Test
    void validateRefreshToken_tampered_returnsFalse() {
        String refresh = tokenProvider.generateRefreshToken(UUID.randomUUID(), "test@example.com");
        String[] parts = refresh.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "."
                + parts[2].substring(0, parts[2].length() - 4) + "XXXX";

        assertThat(tokenProvider.validateRefreshToken(tampered)).isFalse();
    }
}
