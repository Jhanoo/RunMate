package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.auth.LoginRequest;
import com.runhwani.runmate.dto.request.auth.SignupRequest;
import com.runhwani.runmate.dto.response.auth.TokenResponse;
import com.runhwani.runmate.dto.response.auth.SignupResponse;

/**
 * 인증 관련 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 회원가입
     */
    SignupResponse signup(SignupRequest request);

    /**
     * 로그인
     */
    TokenResponse login(LoginRequest request);

    /**
     * 로그아웃
     */
    void logout(String token);
    
    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    boolean checkEmailDuplicate(String email);
} 