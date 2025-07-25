package org.vrajpatel.userauthservice.utils.JwtUtils;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.utils.UserPrincipal;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private final AppProperties appProperties;
    private final Key accesskey;
    private final Key refreshKey;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;

        // Convert the secret key string to a proper Key object using UTF-8 encoding
        this.accesskey = Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshKey=Keys.hmacShaKeyFor(appProperties.getAuth().getRefreshTokenSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createJWT(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal == null) {
            return null;
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .claim("id", userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(accesskey, SignatureAlgorithm.HS256) // 🔐 No deprecation warning
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null) {
            return null;
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .claim("id", userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID getUserIdFromJWT(String jwt) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(accesskey) // ✅ Use cached Key object
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateRefreshToken(String refreshToken) {
        try{
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken);
            return true;
        }
        catch (Exception e){
            throw new BadRequestException("Invalid refresh token");
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(accesskey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
            throw new BadRequestException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
            throw new BadRequestException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
            throw new BadRequestException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
            throw new BadRequestException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
            throw new BadRequestException("JWT claims string is empty");
        }
    }
}
