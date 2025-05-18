package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface UserDao {
    User findByEmail(String email);
    User findByUserId(UUID userId);
    void insert(User user);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    // 사용자 평균 페이스 조회
    Double findAvgPaceByUserId(UUID userId);
    // 사용자별 평균 페이스 업데이트
    void updateAvgPace(UUID userId, Double avgPace);
    // FCM 토큰 업데이트
    void updateFcmToken(@Param("userId") UUID userId, @Param("fcmToken") String fcmToken);
}