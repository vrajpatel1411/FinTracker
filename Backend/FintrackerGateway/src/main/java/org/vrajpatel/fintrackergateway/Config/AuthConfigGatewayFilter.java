package org.vrajpatel.fintrackergateway.Config;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private WebClient webClient;


    public AuthConfigGatewayFilter() {
        super(Config.class);
        this.webClient = WebClient.builder().baseUrl("http://localhost:8080/userauthservice/api/auth").defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Autowired
    private AuthServiceRouteValidator authServiceRouteValidator;

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> {

            String authHeader=null;
            try {
                 authHeader = exchange.getRequest().getCookies().getFirst("jwttoken").getValue();
            }
            catch (NullPointerException e) {
                return Mono.error(new BadException("Jwt Token not found in request header"));
            }
            if (authHeader == null || authHeader.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is empty"));
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
                                        .build();
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            } else {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT validation failed"));
                            }
                        })
                        .onErrorResume(Exception.class, e -> {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.BAD_GATEWAY,
                                    "Error validating JWT: " + e.getMessage()
                            ));
                        });

        };
    }
}
