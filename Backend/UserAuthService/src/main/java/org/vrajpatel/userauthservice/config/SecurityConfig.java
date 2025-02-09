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
                .cors(corsConfigurer -> corsConfigurer.configurationSource(
                        new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration cors=new CorsConfiguration();
                                cors.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                                cors.setAllowedMethods(Collections.singletonList("*"));
                                cors.setAllowCredentials(true);
                                cors.setAllowedHeaders(Collections.singletonList("*"));
                                cors.setExposedHeaders(Arrays.asList("Authorization"));
                                cors.setMaxAge(3600L);
                                return cors;
                            }
                        }
                ))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests.requestMatchers("/api/user/*").authenticated().anyRequest().permitAll())
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

        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Collections.singletonList("*"));
//        configuration.setAllowedMethods(Collections.singletonList("*"));
//        configuration.setAllowedHeaders(Collections.singletonList("*"));
//        configuration.setAllowCredentials(true);
//        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
//        configuration.setMaxAge(3600L);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
}
