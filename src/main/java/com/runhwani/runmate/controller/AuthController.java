//package com.runhwani.runmate.controller;
//
//import com.runhwani.runmate.dto.request.auth.LoginRequest;
//import com.runhwani.runmate.dto.request.auth.SignupRequest;
//import com.runhwani.runmate.dto.response.auth.TokenResponse;
//import com.runhwani.runmate.dto.response.auth.LogoutResponse;
//import com.runhwani.runmate.service.AuthService;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@Tag(name = "1. 인증", description = "인증 관련 API")
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthController implements AuthControllerDocs {
//
//    private final AuthService authService;
//
//    @Override
//    @PostMapping("/signup")
//    public ResponseEntity<TokenResponse> signup(@RequestBody SignupRequest request) {
//        return ResponseEntity.ok(authService.signup(request));
//    }
//
//    @Override
//    @PostMapping("/login")
//    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//
//    @Override
//    @PostMapping("/logout")
//    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String bearerToken) {
//        String token = bearerToken.substring(7); // "Bearer " 제거
//        return ResponseEntity.ok(authService.logout(token));
//    }
//}