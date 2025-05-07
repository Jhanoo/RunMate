package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.dto.request.auth.LoginRequest;
import com.runhwani.runmate.dto.request.auth.SignupRequest;
import com.runhwani.runmate.dto.response.auth.SignupResponse;
import com.runhwani.runmate.dto.response.auth.TokenResponse;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.exception.ErrorCode;
import com.runhwani.runmate.model.User;
import com.runhwani.runmate.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 인증 관련 서비스 구현 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 검사
        if (userDao.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 닉네임 중복 검사
        if (userDao.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        
        // 사용자 생성
        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .createdAt(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        
        userDao.insert(user);
        
        return SignupResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userDao.findByEmail(request.getEmail());
        log.debug("user: {}", user);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        log.debug("userId = {}", user.getUserId());
        String token = jwtProvider.generateToken(String.valueOf(user.getUserId()));
        return new TokenResponse(token);
    }

    @Override
    public void logout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        try {
            token = token.substring(7);
            jwtProvider.invalidateToken(token);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LOGOUT_SERVER_ERROR);
        }
    }
} 