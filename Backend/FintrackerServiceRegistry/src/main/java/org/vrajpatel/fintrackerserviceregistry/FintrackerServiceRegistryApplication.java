package org.vrajpatel.fintrackerserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FintrackerServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintrackerServiceRegistryApplication.class, args);
	}

}
