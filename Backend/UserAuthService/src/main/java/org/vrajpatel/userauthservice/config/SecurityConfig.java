package org.vrajpatel.userauthservice.config;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.vrajpatel.userauthservice.utils.CustomUserService;
import org.vrajpatel.userauthservice.utils.HttpCookieOauth2;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenAuthenticationFilter;
import org.vrajpatel.userauthservice.utils.Oauth2Handler.OAuth2FailureHandler;
import org.vrajpatel.userauthservice.utils.Oauth2Handler.Oauth2SuccessHandler;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserService customUserService;

    @Value("${authorizedUrl}")
    private String authorizedUrl;

    @Autowired
    private Oauth2SuccessHandler customSuccessHandler;

    @Autowired
    private OAuth2FailureHandler customFailureHandler;

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers("/userauthservice/api/auth/**","/oauth2/**","/swagger-ui/**","/v3/**","/actuator/**").permitAll()
                                .requestMatchers("/userauthservice/api/user/**").authenticated()
                                .anyRequest().permitAll()
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
                                .redirectionEndpoint(redirectionEndpointConfig ->
                                        redirectionEndpointConfig.baseUri("/oauth2/callback/*"))
                                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(customUserService))
                                .successHandler(customSuccessHandler)
                                .failureHandler(customFailureHandler));


        return http.build();
    }
}
