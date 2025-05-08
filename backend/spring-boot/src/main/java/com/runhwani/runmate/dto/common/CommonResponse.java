package com.runhwani.runmate.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통 응답 형식
 *
 * @param <T> 실제 payload 타입
 */
@Schema(description = "공통 API 응답 래퍼")
public record CommonResponse<T>(
        @Schema(description = "응답 메시지", example = "OK") String message,
        @Schema(description = "실제 payload 데이터") T data) {

    /**
     * 성공 응답 생성
     */
    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>("OK", data);
    }

    /**
     * 실패 응답 생성
     */
    public static <T> CommonResponse<T> error(String msg) {
        return new CommonResponse<>(msg, null);
    }

}