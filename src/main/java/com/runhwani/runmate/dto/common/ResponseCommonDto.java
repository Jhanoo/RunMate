package com.runhwani.runmate.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 아주 간단한 공통 응답 포맷
 * @param <T> 실제 payload 타입
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCommonDto<T> {
    private String message;  // 처리 결과 메시지
    private T data;          // 실제 반환할 데이터

    /**
     * 성공 응답 생성
     */
    public static <T> ResponseCommonDto<T> success(T data) {
        return new ResponseCommonDto<>("OK", data);
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ResponseCommonDto<T> error(String msg) {
        return new ResponseCommonDto<>(msg, null);
    }
}