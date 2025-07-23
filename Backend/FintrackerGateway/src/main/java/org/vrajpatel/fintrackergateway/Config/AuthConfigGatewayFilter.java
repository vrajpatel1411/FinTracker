package org.vrajpatel.fintrackergateway.Config;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.vrajpatel.fintrackergateway.Config.Exception.BadException;
import org.vrajpatel.fintrackergateway.Config.RoutesValidator.AuthServiceRouteValidator;
import org.vrajpatel.fintrackergateway.ResponseDto.ValidationResponseDto;
import reactor.core.publisher.Mono;

@Component
public class AuthConfigGatewayFilter extends AbstractGatewayFilterFactory<AuthConfigGatewayFilter.Config> {

    Logger logger = LoggerFactory.getLogger(AuthConfigGatewayFilter.class);

    public static class Config{

    }


    @Value("${validationUrl}")
    private String validationUrl;

    public AuthConfigGatewayFilter() {
        super(Config.class);
    }

    @Autowired
    private AuthServiceRouteValidator authServiceRouteValidator;

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> {

            logger.info("Inside AuthConfigGatewayFilter");


            String authHeader = null;
            try {
                if (exchange.getRequest().getCookies().getFirst("jwttoken") != null) {
                    authHeader = exchange.getRequest().getCookies().getFirst("jwttoken").getValue();
                    logger.info("JWT Token found: " + authHeader);
                }
            } catch (Exception e) {
                logger.error("Error extracting JWT token: " + e.getMessage());
            }

            if (authHeader == null || authHeader.isEmpty()) {
                logger.warn("JWT Token is missing");
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token missing"));
            }

            String requestBody = String.format("{\"jwt\": \"%s\"}", authHeader);
            WebClient webClient = WebClient.builder().baseUrl(validationUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            return webClient.post()
                    .uri("/validate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ValidationResponseDto.class)
                    .flatMap(response -> {
                        if (response.isValid()) {
                            logger.info("JWT is valid. User ID: {}, Email: {}", response.getUserId(), response.getUserEmail());
                            ServerHttpRequest mutatedRequest = exchange.getRequest()
                                    .mutate()
                                    .header("userEmail", response.getUserEmail())
                                    .header("userId", response.getUserId())
                                    .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
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
