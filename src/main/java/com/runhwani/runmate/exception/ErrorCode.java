package com.runhwani.runmate.exception;


import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 유효성 검증 관련 에러 -> @Valid 관리
	// INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION-001", "이메일 형식이 올바르지 않습니다."),
	// INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION-002", "비밀번호 형식이 올바르지 않습니다."),
	// EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "VALIDATION-003", "이메일을 입력해 주세요."),
	// PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "VALIDATION-004", "비밀번호를 입력해 주세요."),

	// 공통 에러
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "VALIDATION-001", "잘못된 요청입니다. 입력 값을 확인해주세요."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE-001", "요청하신 리소스를 찾을 수 없습니다."),
	DATA_CONFLICT(HttpStatus.CONFLICT, "CONFLICT-001", "이미 존재하는 데이터입니다. 중복을 확인해주세요."),
	INVALID_DATA(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION-002", "요청 데이터를 처리할 수 없습니다. 입력 값을 확인해주세요."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER-001", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "SERVER-002", "서버가 잘못된 응답을 받았습니다."),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVER-003", "현재 서버가 점검 중입니다. 잠시 후 다시 시도해주세요."),
	GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SERVER-004", "서버 요청 시간이 초과되었습니다. 다시 시도해주세요."),

	// 로그인 관련 에러
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "LOGIN-001", "잘못된 비밀번호입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "LOGIN-002", "해당 이메일을 가진 사용자가 없습니다."),
	LOGIN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LOGIN-003", "로그인 처리 중 서버 오류가 발생했습니다."),

	// 로그아웃 관련 에러
	LOGOUT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LOGOUT-001", "로그아웃 처리 중 서버 오류가 발생했습니다."),

	// 인증 및 JWT 관련 에러
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "유효하지 않은 Access Token입니다."),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-003", "만료된 Access Token입니다."),
	ACCESS_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-004", "Access Token이 누락되었습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "유효하지 않은 Refresh Token입니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-006", "만료된 Refresh Token입니다. 다시 로그인해주세요."),
	REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-007", "Refresh Token이 누락되었습니다."),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-008", "인증에 실패했습니다."),
	FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "AUTH-009", "해당 리소스에 접근할 권한이 없습니다."),

	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-010", "만료된 JWT 토큰입니다."),
	MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-011", "잘못된 형식의 JWT 토큰입니다."),
	TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-012", "잘못된 JWT 서명입니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-013", "지원되지 않는 JWT 토큰입니다."),
	TOKEN_TAMPERED(HttpStatus.UNAUTHORIZED, "AUTH-014", "JWT 토큰이 변조되었습니다."),

	// 회원 가입 관련 에러
	EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "SIGNUP-001", "이미 존재하는 이메일입니다."),
	PASSWORDS_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "SIGNUP-002", "비밀번호가 일치하지 않습니다."),
	SIGNUP_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP-003", "회원 가입 처리 중 서버 내부 오류가 발생했습니다."),

	// 마이페이지 관련 에러
	INVALID_USER_ID(HttpStatus.BAD_REQUEST, "MYPAGE-001", "유효하지 않은 userId입니다."),
	STUDY_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPAGE-002", "학습 내역을 찾을 수 없습니다."),
	MYPAGE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MYPAGE-003", "학습 내역 조회 중 서버 오류가 발생했습니다."),

	// 회원(Member) 관련 에러
	MEMBER_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "해당 ID를 가진 사용자를 찾을 수 없습니다."),
	MEMBER_ID_MISSING(HttpStatus.BAD_REQUEST, "MEMBER-002", "사용자 ID가 누락되었습니다."),
	INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "FCM-001", "유효하지 않은 FCM 토큰 값입니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH-010", "권한이 없습니다.");

	private final HttpStatus status;
	private final String errorCode;
	private final String message;
}
