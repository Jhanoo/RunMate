package com.runhwani.runmate.dto.common;

/**
 * 공통 응답 형식
 *
 * @param <T> 실제 payload 타입
 */
public record CommonResponse<T>(String message, T data) {

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