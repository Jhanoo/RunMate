package com.runhwani.runmate.domain.auth.service;

import com.runhwani.runmate.domain.auth.dto.LoginRequest;
import com.runhwani.runmate.domain.auth.dto.SignupRequest;
import com.runhwani.runmate.domain.auth.dto.TokenResponse;
import com.runhwani.runmate.domain.auth.dto.LogoutResponse;
import com.runhwani.runmate.domain.auth.entity.Member;
import com.runhwani.runmate.domain.auth.repository.MemberRepository;
import com.runhwani.runmate.global.jwt.JwtProvider;
import com.runhwani.runmate.global.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        // 회원 정보 저장
        Member member = Member.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();
        
        memberRepository.save(member);

        // 토큰 발급
        String token = jwtProvider.generateToken(member.getEmail());
        return new TokenResponse(token);
    }

    public TokenResponse login(LoginRequest request) {
        // 회원 정보 조회
        Member member = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 발급
        String token = jwtProvider.generateToken(member.getEmail());
        return new TokenResponse(token);
    }

    public LogoutResponse logout(String token) {
        // 토큰 블랙리스트에 추가
        jwtProvider.invalidateToken(token);
        return new LogoutResponse("로그아웃 되었습니다.");
    }
} 