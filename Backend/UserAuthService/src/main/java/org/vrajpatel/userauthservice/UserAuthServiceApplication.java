package org.vrajpatel.userauthservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class UserAuthServiceApplication {

    private final Logger log = LoggerFactory.getLogger(UserAuthServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }

    @Bean(name="email_service_thread_executor")
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setRejectedExecutionHandler(this::rejectedExecution);
        return executor;

    }

    void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.error("Rejected execution of task {} from {}", r.toString(), executor.toString());
    }
}
