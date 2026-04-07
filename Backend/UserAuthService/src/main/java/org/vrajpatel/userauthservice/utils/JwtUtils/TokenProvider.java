package org.vrajpatel.userauthservice.utils.JwtUtils;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private final AppProperties appProperties;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.accessKey = Keys.hmacShaKeyFor(appProperties.getAuth().getAccessTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(appProperties.getAuth().getRefreshTokenSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String email) {
        return buildToken(userId, email, appProperties.getAuth().getAccessTokenExpirationMsec(), accessKey);
    }

    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, appProperties.getAuth().getRefreshTokenExpirationMsec(), refreshKey);
    }

    private String buildToken(UUID userId, String email, long expirationMsec, SecretKey key) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMsec);
        return Jwts.builder()
                .subject(userId.toString())
                .claims().add("userId", userId).add("email", email).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromJWT(String jwt) {
        return UUID.fromString(parseClaims(jwt, accessKey).getSubject());
    }

    public String getEmailFromJWT(String jwt) {
        return parseClaims(jwt,accessKey).get("email", String.class);
    }

    public UUID getUserIdfromRefreshToken(String refreshToken) {
        return UUID.fromString(parseClaims(refreshToken,refreshKey).getSubject());
    }

    public String getEmailFromRefreshToken(String refreshToken) {
        return parseClaims(refreshToken,refreshKey).get("email", String.class);
    }

    private Claims parseClaims(String jwt,SecretKey key ) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }


    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(refreshToken);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.error("Refresh token expired");
        } catch (JwtException ex) {
            logger.error("Invalid refresh token: {}", ex.getMessage());
        }
        return false;
    }

    public Claims validateandExtractToken(String authToken) {
        try {
            return Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(authToken).getPayload();
        } catch (SignatureException ex) {
            throw new BadRequestException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            throw new BadRequestException("Invalid JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new BadRequestException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("JWT claims string is empty");
        }
    }
}
