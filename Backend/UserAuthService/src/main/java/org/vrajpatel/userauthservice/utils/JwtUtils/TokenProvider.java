package org.vrajpatel.userauthservice.utils.JwtUtils;

import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.utils.UserPrincipal;
import org.vrajpatel.userauthservice.utils.config.AppProperties;
import io.jsonwebtoken.*;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenProvider {

    private static final Logger logger= LoggerFactory.getLogger(TokenProvider.class);


    private final AppProperties appProperties;

    TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }


    public String createJWT(Authentication authentication) {
//        System.out.println(authentication.getPrincipal().toString() + " TokenProvider.java");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal == null) {
            return null;
        }
        Date now = new Date();
        Date expiryDate=new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        return Jwts.builder().setSubject(userPrincipal.getId().toString()).claim("id",userPrincipal.getId().toString()).setIssuedAt(now).setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512,appProperties.getAuth().getTokenSecret()).compact();
    }

    public UUID getUserIdFromJWT(String jwt) {
        Claims claims=Jwts.parserBuilder().setSigningKey(appProperties.getAuth().getTokenSecret().getBytes()).build().parseClaimsJws(jwt).getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String authToken){
        try{
            Jwts.parserBuilder().setSigningKey(appProperties.getAuth().getTokenSecret().getBytes()).build().parseClaimsJws(authToken);
            return true;
        }
        catch(SignatureException ex){
            logger.error("Invalid JWT SIGNATURE");
            throw new BadRequestException("Invalid JWT SIGNATURE");
        }
        catch(MalformedJwtException ex){
            logger.error("Invalid JWT Token");
            throw new BadRequestException("Invalid JWT Token");
        }
        catch(ExpiredJwtException ex){
            logger.error("Expired JWT Token");
            throw new BadRequestException("Expired JWT Token");

        }
        catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT Token");
            throw new BadRequestException("Unsupported JWT Token");
        }
        catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
            throw new BadRequestException("JWT claims string is empty");
        }

    }
}
