package org.vrajpatel.fintrackergateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FintrackerGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FintrackerGatewayApplication.class, args);
    }

}
