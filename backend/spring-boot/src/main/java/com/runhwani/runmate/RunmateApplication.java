package com.runhwani.runmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RunmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunmateApplication.class, args);
    }

}
