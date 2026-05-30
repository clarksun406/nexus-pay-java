package com.nexuspay.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.nexuspay")
@EntityScan(basePackages = "com.nexuspay.domain.entity")
@EnableJpaRepositories(basePackages = "com.nexuspay.repository")
public class NexuspayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexuspayApplication.class, args);
    }
}
