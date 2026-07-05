package org.vrajpatel.fintrackergateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.vrajpatel.fintrackergateway.ResponseDto.ApiResponseDTO;
import org.vrajpatel.fintrackergateway.ResponseDto.JwtValidationData;
import org.vrajpatel.fintrackergateway.ResponseDto.TokenDTO;
import org.vrajpatel.fintrackergateway.requestDTO.TokenRequestDTO;
import reactor.core.publisher.Mono;


@Component
public class AuthServiceClient {

    private final WebClient webClient;

//    @Value("${validationUrl}")
//    private String validationUrl;

    public AuthServiceClient(@Value("${validationUrl}") String validationUrl) {
        this.webClient = WebClient.builder().baseUrl(validationUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public Mono<ApiResponseDTO<JwtValidationData>> validateAccessToken(String token) {
        return webClient.post()
                .uri("/validate")
                .bodyValue(new TokenRequestDTO(token)) // Use an actual DTO object, not String.format
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<JwtValidationData>>() {});
    }

    public Mono<ApiResponseDTO<TokenDTO>> refreshAccessToken(String refreshToken) {
        return webClient.post()
                .uri("/getNewAccessToken")
                .bodyValue(new TokenRequestDTO(refreshToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<TokenDTO>>() {});
    }
}
