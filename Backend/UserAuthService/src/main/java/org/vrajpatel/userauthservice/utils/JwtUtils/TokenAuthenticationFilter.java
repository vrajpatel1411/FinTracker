package org.vrajpatel.userauthservice.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.Exception.CustomAuthenticationError;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.utils.UserPrincipal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final TokenProvider tokenProvider;


    private final UserRepository userRepository;

    public TokenAuthenticationFilter( TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    private boolean isPublicEndpoint(String requestURI) {

        return requestURI.startsWith("/userauth/api/auth/") ||
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/") || requestURI.startsWith("/actuator/");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, AuthenticationException {
        try{
            String requestURI = request.getRequestURI();
            logger.info(">>> Incoming Request: {} {}", request.getMethod(), request.getRequestURI());

            if (isPublicEndpoint(requestURI)) {
                filterChain.doFilter(request, response); // Skip the filter
                return;
            }
            else{
                logger.info("URI -> "+requestURI);
            }
            String jwt=getJWTFromRequest(request);
            logger.info("jwt token is {}", jwt);


            if(StringUtils.hasText(jwt) &&  tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromJWT(jwt);
                Optional<User> user=userRepository.findById(userId);

                if(user.isPresent()) {
                    UserDetails userDetails = UserPrincipal.create(user.get());

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
                else{
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("User Not Found Using JWT Token");
//                    throw new RuntimeException("User Not Found Using JWT Token");
                }
            }
            else{
                logger.error("Error with validating the token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(" Error: Sorry either token is null or has expired");
//                throw new RuntimeException("Sorry Something wrong with the token");
            }
            filterChain.doFilter(request, response);
        }
        catch(Exception e){
            logger.error("Could not set user authentication in security context", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
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
