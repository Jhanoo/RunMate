package com.runhwani.runmate.service.impl;

import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.dto.response.user.UserProfileResponse;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.exception.ErrorCode;
import com.runhwani.runmate.model.User;
import com.runhwani.runmate.security.SecurityUtil;
import com.runhwani.runmate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * 사용자 관련 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        // SecurityContext에서 현재 인증된 사용자의 ID 가져오기
        String userIdStr = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_USER));
        
        // UUID로 변환
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 사용자 ID로 사용자 정보 조회
        User user = userDao.findByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        
        // 응답 DTO 생성 및 반환
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImage())
                .build();
    }
} 