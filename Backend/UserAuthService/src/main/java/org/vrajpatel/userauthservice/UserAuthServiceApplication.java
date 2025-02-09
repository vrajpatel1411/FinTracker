package org.vrajpatel.userauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class UserAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }

}
