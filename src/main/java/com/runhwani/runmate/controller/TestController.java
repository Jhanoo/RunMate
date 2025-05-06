package com.runhwani.runmate.controller;

import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserDao userDao;

    @GetMapping("/test/add-user")
    public String addTestUser() {
        try {
            User user = User.builder()
                .userId(UUID.randomUUID())
                .email("test2@test.com")
                .password("test123")
                .nickname("testuser2")
                .createdAt(LocalDateTime.now())
                .build();
            
            userDao.insert(user);
            return "User added successfully: " + user.getEmail();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
} 