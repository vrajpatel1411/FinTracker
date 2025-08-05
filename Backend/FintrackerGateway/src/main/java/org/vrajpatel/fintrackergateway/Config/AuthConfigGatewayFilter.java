package org.vrajpatel.fintrackergateway.Config;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.vrajpatel.fintrackergateway.Config.Exception.BadException;
import org.vrajpatel.fintrackergateway.ResponseDto.TokenDTO;
import org.vrajpatel.fintrackergateway.ResponseDto.ValidationResponseDto;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Component
public class AuthConfigGatewayFilter extends AbstractGatewayFilterFactory<AuthConfigGatewayFilter.Config> {

    Logger logger = LoggerFactory.getLogger(AuthConfigGatewayFilter.class);

    public static class Config{

    }

    @Value("${domain}")
    private String domain;

    @Value("${validationUrl}")
    private String validationUrl;

    public AuthConfigGatewayFilter() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> {

            logger.info("Inside AuthConfigGatewayFilter");


            String authHeader = null;
            try {
                if (exchange.getRequest().getCookies().getFirst("accessToken") != null) {
                    authHeader = Objects.requireNonNull(exchange.getRequest().getCookies().getFirst("accessToken")).getValue();
                    logger.info("JWT Token found from client: {}", authHeader);
                }
            } catch (Exception e) {
                logger.error("Error extracting JWT token: {}", e.getMessage());
            }
            WebClient webClient = WebClient.builder().baseUrl(validationUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            if (authHeader == null || authHeader.isEmpty()) {
                logger.info("Auth header is empty");
                HttpCookie cookie = exchange.getRequest().getCookies().getFirst("refreshToken");

                if (cookie == null || cookie.getValue().isEmpty()) {
                    return Mono.error(new BadException("Refresh Token is empty"));
                }

                String refreshToken = cookie.getValue();
                return webClient
                        .post()
                        .uri("/getNewAccessToken")
                        .bodyValue(String.format("{\"jwt\": \"%s\"}", refreshToken))
                        .retrieve().bodyToMono(TokenDTO.class)
                        .flatMap(response ->
                                {
                                    if (response.getAccessToken() != null) {
                                        logger.info("JWT Token found: {}", response.getAccessToken());
                                        logger.info("User Email {}", response.getUserEmail());
                                        logger.info("User Id {}", response.getUserId());
                                        ServerHttpRequest mutatedRequest = exchange.getRequest()
                                                .mutate()
                                                .header("userEmail", response.getUserEmail())
                                                .header("userId", response.getUserId())
                                                .build();
                                        return chain.filter(exchange.mutate().request(mutatedRequest).build()).then(
                                                Mono.fromRunnable(() -> exchange.getResponse().addCookie(
                                                        ResponseCookie.from("accessToken", response.getAccessToken())
                                                                .httpOnly(true)
                                                                .secure(true)
                                                                .path("/")
                                                                .sameSite("None")
                                                                .domain(domain)
                                                                .maxAge(300)
                                                                .build()
                                                ))
                                        );
                                    } else {
                                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT validation failed"));
                                    }
                                }
                        )
                        .onErrorResume(Exception.class, e -> {
                            logger.error("Error validating JWT By Service: {}", e.getMessage());
                            return Mono.error(new BadException(

                                    "Error validating JWT: " + e.getMessage()
                            ));
                        }).then();


            }

            String requestBody = String.format("{\"jwt\": \"%s\"}", authHeader);


            return webClient.post()
                    .uri("/validate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ValidationResponseDto.class)
                    .flatMap(response -> {
                        if (response.isValid()) {
                            ServerHttpRequest mutatedRequest = exchange.getRequest()
                                    .mutate()
                                    .header("userEmail", response.getUserEmail())
                                    .header("userId", response.getUserId())
                                    .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            logger.info(response.getMessage());
                            logger.warn("JWT validation failed");
                            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT validation failed"));
                        }
                    })
                    .onErrorResume(Exception.class, e -> {
                        logger.error("Error validating JWT: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "Error validating JWT: " + e.getMessage()
                        ));
                    });
        };
    }

}
