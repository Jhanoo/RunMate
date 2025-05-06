package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface UserDao {
    User findByEmail(String email);
    User findByUserId(UUID userId);
    void insert(User user);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
} 