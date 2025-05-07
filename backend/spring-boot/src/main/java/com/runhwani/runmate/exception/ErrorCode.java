package com.runhwani.runmate.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "VALIDATION-001", "잘못된 요청입니다. 입력 값을 확인해주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE-001", "요청하신 리소스를 찾을 수 없습니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "CONFLICT-001", "이미 존재하는 데이터입니다. 중복을 확인해주세요."),
    INVALID_DATA(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION-002", "요청 데이터를 처리할 수 없습니다. 입력 값을 확인해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER-001", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY-001", "해당 엔티티를 찾을 수 없습니다."),

    // 로그인 관련 에러
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "LOGIN-001", "잘못된 이메일 또는 비밀번호입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "LOGIN-002", "해당 이메일을 가진 사용자가 없습니다."),
    LOGIN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LOGIN-003", "로그인 처리 중 서버 오류가 발생했습니다."),

    // 인증 및 JWT 관련 에러
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "유효하지 않은 Access Token입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-003", "만료된 Access Token입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "AUTH-009", "해당 리소스에 접근할 권한이 없습니다."),

    // 회원 가입 관련 에러
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "SIGNUP-001", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "SIGNUP-002", "이미 사용 중인 닉네임입니다."),

    // 로그아웃 관련 에러
    LOGOUT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LOGOUT-001", "로그아웃 처리 중 서버 오류가 발생했습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "LOGOUT-002", "유효하지 않은 토큰입니다.");


    private final HttpStatus status;
    private final String errorCode;
    private final String message;
} 