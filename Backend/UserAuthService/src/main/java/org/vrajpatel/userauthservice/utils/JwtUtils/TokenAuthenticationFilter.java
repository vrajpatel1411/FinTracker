package org.vrajpatel.userauthservice.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        return requestURI.startsWith("/userauthservice/api/auth/") ||
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/") || requestURI.startsWith("/actuator/");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, AuthenticationException {
        try{
            String requestURI = request.getRequestURI();

            if (isPublicEndpoint(requestURI)) {
                filterChain.doFilter(request, response); // Skip the filter
                return;
            }
            else{
                System.out.println(requestURI);
            }
            String jwt=getJWTFromRequest(request);
            logger.info("jwt token is {}", jwt);


            if(StringUtils.hasText(jwt) &&  tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromJWT(jwt);
                Optional<User> user=userRepository.findByUserId(userId);

                if(user.isPresent()) {
                    UserDetails userDetails = UserPrincipal.create(user.get());

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
                else{
                    throw new CustomAuthenticationError("User Not Found Using JWT Token");
                }
            }
            else{
                logger.error("Error with validating the token");
                throw new BadRequestException("Sorry Something wrong with the token");
            }
        }
        catch(Exception e){
            logger.error("Could not set user authentication in security context", e);
            throw new BadRequestException(e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    private String getJWTFromRequest(HttpServletRequest request) {
        logger.info(request.getHeader("Authorization"));
        logger.info(request.getHeaderNames().toString());
        String token=request.getHeader("Authorization");
        if(StringUtils.hasText(token) && token.startsWith("Bearer ")){
            return token.substring(7);
        }
        return null;
    }
}
