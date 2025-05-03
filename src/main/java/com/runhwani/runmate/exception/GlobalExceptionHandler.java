package com.runhwani.runmate.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException.BadGateway;
import org.springframework.web.client.HttpServerErrorException.GatewayTimeout;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;

import com.runhwani.runmate.dto.common.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// CustomException 처리 (ErrorCode 활용)
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus().value())
			.body(ErrorResponse.of(errorCode));
	}

	// 400 - 잘못된 요청 (입력 값 오류)
	// 유효성 검사 실패 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();

		// 필드별 오류 메시지 리스트 생성
		List<ErrorResponse.FieldErrorDetail> fieldErrors = bindingResult.getFieldErrors().stream()
			.map(error -> ErrorResponse.FieldErrorDetail.builder()
				.field(error.getField())
				.message(error.getDefaultMessage())
				.build())
			.collect(Collectors.toList());

		// 유효성 검사 실패 ErrorResponse 생성
		return ResponseEntity.badRequest()
			.body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, fieldErrors));
	}

	// 401 - 인증되지 않은 사용자 (토큰 인증 실패)
	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedException(SecurityException ex) {
		return ResponseEntity
			.status(ErrorCode.UNAUTHORIZED_USER.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.UNAUTHORIZED_USER));
	}

	// 403 - 권한 없음
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
		return ResponseEntity
			.status(ErrorCode.FORBIDDEN_ACCESS.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.FORBIDDEN_ACCESS));
	}

	// 404 - 리소스를 찾을 수 없음
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
		return ResponseEntity
			.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	// 409 - 데이터 중복 오류
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleConflictException(IllegalStateException ex) {
		return ResponseEntity
			.status(ErrorCode.DATA_CONFLICT.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.DATA_CONFLICT));
	}

	// 422 - 유효하지 않은 데이터 (데이터 검증 실패)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(IllegalArgumentException ex) {
		return ResponseEntity
			.status(ErrorCode.INVALID_DATA.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.INVALID_DATA));
	}

	// 500 - 서버 내부 오류
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	// 502 - 잘못된 게이트웨이 응답
	@ExceptionHandler(BadGateway.class)
	public ResponseEntity<ErrorResponse> handleBadGatewayException(Exception ex) {
		return ResponseEntity
			.status(ErrorCode.BAD_GATEWAY.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.BAD_GATEWAY));
	}

	// 503 - 서버 점검 중
	@ExceptionHandler(ServiceUnavailable.class)
	public ResponseEntity<ErrorResponse> handleServiceUnavailableException(Exception ex) {
		return ResponseEntity
			.status(ErrorCode.SERVICE_UNAVAILABLE.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.SERVICE_UNAVAILABLE));
	}

	// 504 - 요청 시간 초과
	@ExceptionHandler(GatewayTimeout.class)
	public ResponseEntity<ErrorResponse> handleGatewayTimeoutException(Exception ex) {
		return ResponseEntity
			.status(ErrorCode.GATEWAY_TIMEOUT.getStatus().value())
			.body(ErrorResponse.of(ErrorCode.GATEWAY_TIMEOUT));
	}
}
