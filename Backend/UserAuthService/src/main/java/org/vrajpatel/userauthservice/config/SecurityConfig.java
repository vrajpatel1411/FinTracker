package org.vrajpatel.userauthservice.config;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.vrajpatel.userauthservice.utils.CustomUserService;
import org.vrajpatel.userauthservice.utils.HttpCookieOauth2;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenAuthenticationFilter;
import org.vrajpatel.userauthservice.utils.Oauth2Handler.OAuth2FailureHandler;
import org.vrajpatel.userauthservice.utils.Oauth2Handler.Oauth2SuccessHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserService customUserService;
    private final Oauth2SuccessHandler customSuccessHandler;
    private final OAuth2FailureHandler customFailureHandler;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Value("${authorizedUrl}")
    private String authorizedUrl;

    public SecurityConfig(CustomUserService customUserService,
                          Oauth2SuccessHandler customSuccessHandler,
                          OAuth2FailureHandler customFailureHandler,
                          TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.customUserService = customUserService;
        this.customSuccessHandler = customSuccessHandler;
        this.customFailureHandler = customFailureHandler;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http){
        http
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/userauth/api/auth/**", "/oauth2/**", "/user/api/**", "/v3/**", "/actuator/**","/error/**").permitAll()
                                .requestMatchers("/userauth/api/user/**").authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .oauth2Login(oauth ->
                        oauth
                                .authorizationEndpoint(configure ->
                                        configure
                                                .baseUri("/oauth2/authorize")
                                                .authorizationRequestRepository(new HttpCookieOauth2())
                                )
                                .redirectionEndpoint(redirect ->
                                        redirect.baseUri("/oauth2/callback/*")
                                )
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(customUserService)
                                )
                                .successHandler(customSuccessHandler)
                                .failureHandler(customFailureHandler)
                ).exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Unauthorized - token missing or invalid\"}");
                                },
                                request -> true  // matches all requests
                        )
                );

        return http.build();
    }
}
