package com.runhwani.runmate.dto.common;

import java.util.List;

import com.runhwani.runmate.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

	@Schema(description = "HTTP 상태 코드", example = "401")
	private final int status;
	@Schema(description = "에러 메시지", example = "인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요.")
	private final String message;
	@Schema(description = "에러 코드", example = "AUTH-001")
	private final String errorCode;
	@Schema(description = "필드별 에러 상세 정보")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final List<FieldErrorDetail> errors;

	// 기본적인 ErrorResponse 생성
	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.message(errorCode.getMessage())
			.errorCode(errorCode.getErrorCode())
			.errors(null) // 필드 오류가 없는 일반적인 예외
			.build();
	}

	// 유효성 검사 실패 시 필드 오류 포함
	public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.message(errorCode.getMessage())
			.errorCode(errorCode.getErrorCode())
			.errors(fieldErrors)
			.build();
	}

	// 필드 오류를 담는 내부 클래스
	@Getter
	@Builder
	public static class FieldErrorDetail {
		private final String field;  // 오류가 발생한 필드명
		private final String message; // 해당 필드의 에러 메시지
	}
}

