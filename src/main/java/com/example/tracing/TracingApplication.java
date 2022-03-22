package com.example.tracing;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TracingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TracingApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(){
        return  args -> {
        };
    }


}
