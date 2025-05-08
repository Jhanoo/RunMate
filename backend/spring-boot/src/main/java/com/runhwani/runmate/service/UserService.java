package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.response.user.UserProfileResponse;

/**
 * 사용자 관련 서비스 인터페이스
 */
public interface UserService {
    
    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     * @return 사용자 프로필 정보
     */
    UserProfileResponse getCurrentUserProfile();
} 