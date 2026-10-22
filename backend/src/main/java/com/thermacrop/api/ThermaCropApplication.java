package com.thermacrop.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThermaCropApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThermaCropApplication.class, args);
    }
}
