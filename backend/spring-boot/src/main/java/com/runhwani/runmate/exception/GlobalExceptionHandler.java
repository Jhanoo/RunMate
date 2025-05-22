package com.runhwani.runmate.exception;

import com.runhwani.runmate.dto.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new CommonResponse<>(e.getErrorCode().getMessage(), null));
    }

    // 400 - 잘못된 요청
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ResponseEntity
                .badRequest()
                .body(new CommonResponse<>(ErrorCode.INVALID_REQUEST.getMessage(), null));
    }

    // 401 - 인증되지 않은 사용자
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<CommonResponse<Void>> handleUnauthorizedException(SecurityException e) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED_USER.getStatus())
                .body(new CommonResponse<>(ErrorCode.UNAUTHORIZED_USER.getMessage(), null));
    }

    // 403 - 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN_ACCESS.getStatus())
                .body(new CommonResponse<>(ErrorCode.FORBIDDEN_ACCESS.getMessage(), null));
    }

    // 404 - 리소스를 찾을 수 없음
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
                .body(new CommonResponse<>(ErrorCode.RESOURCE_NOT_FOUND.getMessage(), null));
    }

    // 500 - 서버 내부 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new CommonResponse<>(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), null));
    }
} 