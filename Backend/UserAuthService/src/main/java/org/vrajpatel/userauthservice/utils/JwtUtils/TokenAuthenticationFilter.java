package org.vrajpatel.userauthservice.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.utils.UserPrincipal;

import java.io.IOException;
import java.util.UUID;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final TokenProvider tokenProvider;

    public TokenAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/userauth/api/auth") ||
                requestURI.startsWith("/oauth2") ||
                requestURI.startsWith("/v3") ||
                requestURI.startsWith("/actuator") ||
                requestURI.startsWith("/user/api");
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws IOException {
        try {
            if (isPublicEndpoint(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
            String jwt = getJWTFromRequest(request);
            if (StringUtils.hasText(jwt) ) {
                Claims claims=tokenProvider.validateandExtractToken(jwt);
                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                if(StringUtils.hasText(email)) {
                    UserDetails userDetails = UserPrincipal.create(userId, email);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                else{
                    throw new UnAuthorizedException("Not Authorized");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token missing or expired\"}");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"UnAuthorized, try again\"}");
        }
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
