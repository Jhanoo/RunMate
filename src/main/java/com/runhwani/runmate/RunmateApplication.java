package com.runhwani.runmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RunmateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunmateApplication.class, args);
    }

    @RestController
    static class HelloController {
        @GetMapping("/api/hello")
        public String hello() {
            return "Hello, RunHwani Team!";
        }
        
        @GetMapping("/api/test")
        public String test() {
            return "Application is running properly!";
        }
    }
}
