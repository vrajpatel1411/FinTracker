package org.vrajpatel.userauthservice.utils.JwtUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.utils.UserPrincipal;
import org.vrajpatel.userauthservice.utils.config.AppProperties;
import io.jsonwebtoken.*;
import java.util.Date;

@Service
public class TokenProvider {
    private static final Logger logger= LoggerFactory.getLogger(TokenProvider.class);
    @Autowired
    private AppProperties appProperties;

    public String createJWT(Authentication authentication) {
//        System.out.println(authentication.getPrincipal().toString() + " TokenProvider.java");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal == null) {
            return null;
        }
        Date now = new Date();
        Date expiryDate=new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        return Jwts.builder().setSubject(String.valueOf(userPrincipal.getId())).claim("id",userPrincipal.getId()).setIssuedAt(now).setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512,appProperties.getAuth().getTokenSecret()).compact();
    }

    public long getUserIdFromJWT(String jwt) {
        Claims claims=Jwts.parserBuilder().setSigningKey(appProperties.getAuth().getTokenSecret()).build().parseClaimsJws(jwt).getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken){
        try{
            Jwts.parserBuilder().setSigningKey(appProperties.getAuth().getTokenSecret()).build().parse(authToken);
            return true;
        }
        catch(SignatureException ex){
            logger.error("Invalid JWT SIGNATURE");
        }
        catch(MalformedJwtException ex){
            logger.error("Invalid JWT Token");
        }
        catch(ExpiredJwtException ex){
            logger.error("Expired JWT Token");

        }
        catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT Token");
        }
        catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
