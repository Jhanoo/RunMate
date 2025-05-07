package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.AuthControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.auth.LoginRequest;
import com.runhwani.runmate.dto.request.auth.SignupRequest;
import com.runhwani.runmate.dto.response.auth.SignupResponse;
import com.runhwani.runmate.dto.response.auth.TokenResponse;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    public ResponseEntity<CommonResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        try {
            TokenResponse response = authService.login(request);
            return ResponseEntity.ok(new CommonResponse<>("로그인 성공", response));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getStatus())
                    .body(new CommonResponse<>(e.getErrorCode().getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<CommonResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        try {
            SignupResponse response = authService.signup(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new CommonResponse<>("회원가입 성공", response));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getStatus())
                    .body(new CommonResponse<>(e.getErrorCode().getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok(new CommonResponse<>("로그아웃 성공", null));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getStatus())
                    .body(new CommonResponse<>(e.getErrorCode().getMessage(), null));
        }
    }
}