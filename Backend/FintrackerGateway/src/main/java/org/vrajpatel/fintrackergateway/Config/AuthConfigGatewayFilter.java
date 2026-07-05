package org.vrajpatel.fintrackergateway.Config;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.vrajpatel.fintrackergateway.Config.Exception.BadException;
import org.vrajpatel.fintrackergateway.service.AuthServiceClient;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Component
public class AuthConfigGatewayFilter extends AbstractGatewayFilterFactory<AuthConfigGatewayFilter.Config> {

    Logger logger = LoggerFactory.getLogger(AuthConfigGatewayFilter.class);

    private final AuthServiceClient authServiceClient;


    public static class Config{
    }

    @Value("${domain}")
    private String domain;


    public AuthConfigGatewayFilter(AuthServiceClient authServiceClient) {
        super(Config.class);
        this.authServiceClient = authServiceClient;
    }

    public Optional<String> extractCookie(ServerWebExchange exchange, String cookieName) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(cookieName))
                .map(HttpCookie::getValue)
                .filter(val -> !val.isEmpty());
    }

    public Mono<Void> handleTokenRefresh(ServerWebExchange exchange, GatewayFilterChain chain){
        return extractCookie(exchange,"refreshToken")
                .map(refreshToken->authServiceClient.refreshAccessToken(refreshToken)
                    .flatMap(response->{
                        if(response.getData()!=null && response.getData().getAccessToken()!=null){
                            ServerWebExchange mutatedExchange=mutateRequestWithUserInfo(exchange,response.getData().getUserEmail(),response.getData().getUserId());
                            mutatedExchange.getResponse().beforeCommit(()->{
                                mutatedExchange.getResponse().addCookie(buildAuthCookie(response.getData().getAccessToken()));
                                return Mono.empty();
                            });

                            return chain.filter(mutatedExchange);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,"Refresh token Verification failed"));
                    })
                        .onErrorResume(Exception.class, e->{
                            logger.error("Error during token refresh : {}", e.getMessage());
                            return Mono.error(new BadException("Error Validating refresh token"));
                        })
                )
                .orElseGet(()->Mono.error(new BadException("Refresh Token is Empty")));
    }

    private ResponseCookie buildAuthCookie(String accessToken) {
        return ResponseCookie.from("accessToken",accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .domain(domain)
                .maxAge(300)
                .build();
    }

    private ServerWebExchange mutateRequestWithUserInfo(ServerWebExchange exchange, String userEmail, String userId) {
        ServerHttpRequest mutatedRequest=exchange.getRequest().mutate().header("userEmail",userEmail).header("userId",userId).build();
        return exchange.mutate().request(mutatedRequest).build();
    }

    @NotNull
    @Override
    public GatewayFilter apply(@NotNull Config cfg) {
        return (exchange, chain) -> {

            Optional<String> accessToken= extractCookie(exchange, "accessToken");

            if(accessToken.isEmpty()){
                logger.info("Access token Missing, attempting token refresh");
                return handleTokenRefresh(exchange,chain);
            }

            return authServiceClient.validateAccessToken(accessToken.get())
                    .flatMap(response->{

                        if(response.isSuccess() && response.getData()!=null){
                            ServerWebExchange mutatedExchange=mutateRequestWithUserInfo(exchange,response.getData().getUserEmail(),response.getData().getUserId());
                            return chain.filter(mutatedExchange);
                        }
                        logger.warn("Access token validation failed reported by auth service.");
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT validation failed"));
                    })
                    .onErrorResume(Exception.class, e -> {
                        logger.error("Error validating JWT: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Auth service unavailable"));
                    });
        };
    }

}
