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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "/api/auth/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<SignupResponse>> signup(
            @RequestPart("data") SignupRequest data,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        
        try {
            // profileImage가 별도로 전달되면 DTO에 설정
            if (profileImage != null) {
                // SignupRequest에 profileImage 설정 로직 필요
                // 리플렉션이나 새 객체 생성 방식으로 처리 가능
                // 여기서는 간단히 새 객체 생성 방식 사용
                data = new SignupRequest(
                    data.getEmail(),
                    data.getPassword(),
                    data.getNickname(),
                    data.getBirthdayStr(),
                    data.getGenderStr(),
                    profileImage
                );
            }
            
            SignupResponse response = authService.signup(data);
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