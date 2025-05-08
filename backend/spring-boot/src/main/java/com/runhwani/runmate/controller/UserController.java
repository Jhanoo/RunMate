package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.UserControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.user.UserProfileResponse;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @Override
    public ResponseEntity<CommonResponse<UserProfileResponse>> getMyProfile() {
        try {
            UserProfileResponse response = userService.getCurrentUserProfile();
            return ResponseEntity.ok(new CommonResponse<>("프로필 조회 성공", response));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getStatus())
                    .body(new CommonResponse<>(e.getErrorCode().getMessage(), null));
        }
    }
} 