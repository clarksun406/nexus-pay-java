package com.nexuspay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexuspayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexuspayApplication.class, args);
    }
}
