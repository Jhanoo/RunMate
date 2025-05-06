package com.runhwani.runmate.exception;

import com.runhwani.runmate.dto.common.CommonResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @RestControllerAdvice
    public static class GlobalExceptionHandler {
        @ExceptionHandler(CustomException.class)
        public ResponseEntity<CommonResponse<Void>> handleCustomException(CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getStatus())
                    .body(new CommonResponse<>(e.getMessage(), null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("서버에 오류가 발생했습니다.", null));
        }
    }
} 