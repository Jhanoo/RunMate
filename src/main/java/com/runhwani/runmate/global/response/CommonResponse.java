package com.arizona.lipit.global.response;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResponse<T> {

	@Schema(description = "HTTP 상태 코드", example = "200")
	private int status;
	@Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
	private String message;
	@Schema(description = "응답 데이터")
	private T data;

	// 200 OK 응답 (기본 메시지)
	public static <T> CommonResponse<T> ok(T data) {
		return CommonResponse.<T>builder()
			.status(HttpStatus.OK.value())
			.message("요청이 성공적으로 처리되었습니다.")
			.data(data)
			.build();
	}

	// 200 OK 응답 (커스텀 메시지)
	public static <T> CommonResponse<T> ok(String message, T data) {
		return CommonResponse.<T>builder()
			.status(HttpStatus.OK.value())
			.message(message)
			.data(data)
			.build();
	}

	// 201 Created 응답 (기본 메시지)
	public static <T> CommonResponse<T> created(T data) {
		return CommonResponse.<T>builder()
			.status(HttpStatus.CREATED.value())
			.message("리소스가 성공적으로 생성되었습니다.")
			.data(data)
			.build();
	}

	// 201 Created 응답 (커스텀 메시지)
	public static <T> CommonResponse<T> created(String message, T data) {
		return CommonResponse.<T>builder()
			.status(HttpStatus.CREATED.value())
			.message(message)
			.data(data)
			.build();
	}
}
