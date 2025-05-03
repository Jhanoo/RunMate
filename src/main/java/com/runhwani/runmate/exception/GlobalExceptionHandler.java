package com.runhwani.runmate.exception;

import com.runhwani.runmate.dto.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 전역 예외 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Spring에서 상태코드와 메시지를 던질 때 사용하는 예외(ResponseStatusException)를 처리
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CommonResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        // ex.getReason()에 담긴 메시지를 그대로 클라이언트에 전달
        CommonResponse<Void> body = CommonResponse.error(ex.getReason());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(body);
    }

    /**
     * @Valid, @RequestBody 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        // 첫 번째 필드 오류 메시지를 취합해서 전달
        String message = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        CommonResponse<Void> body = CommonResponse.error(message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * IllegalArgumentException 등 비즈니스 로직에서 던진 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArg(IllegalArgumentException ex) {
        CommonResponse<Void> body = CommonResponse.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * 그 외 모든 예외 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleAny(Exception ex) {
        // 로그로 남기고 싶다면 여기서 로깅
        CommonResponse<Void> body = CommonResponse.error("서버에 오류가 발생했습니다.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
