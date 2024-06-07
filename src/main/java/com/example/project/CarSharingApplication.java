package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarSharingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarSharingApplication.class, args);
    }

}
