package com.runhwani.runmate.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FCMService {

    /**
     * 단일 기기에 FCM 알림 전송
     * 
     * @param token 대상 기기의 FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택 사항)
     * @return 메시지 ID
     */
    public String sendNotification(String token, String title, String body, Map<String, String> data) {
        try {
            // 토큰 유효성 검사
            if (token == null || token.isEmpty()) {
                log.warn("FCM 토큰이 없습니다. 알림을 전송할 수 없습니다.");
                return null;
            }
            
            // 상세 로그 추가 (간소화)
            log.info("FCM 알림 전송 - 제목: {}, 내용: {}", title, body);
            
            // 알림 메시지 구성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 메시지 구성
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notification);
            
            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            // 안드로이드 설정
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();
            messageBuilder.setAndroidConfig(androidConfig);
            
            try {
                // 메시지 전송
                String response = FirebaseMessaging.getInstance().sendAsync(messageBuilder.build()).get();
                log.info("FCM 알림 전송 성공: {}", response);
                return response;
            } catch (ExecutionException e) {
                // FCM 오류 처리
                if (e.getCause() instanceof FirebaseMessagingException) {
                    FirebaseMessagingException fcmError = (FirebaseMessagingException) e.getCause();
                    
                    // 토큰 관련 오류인 경우
                    if (isTokenRelatedError(fcmError)) {
                        log.warn("FCM 토큰 오류: {} - {}", fcmError.getMessagingErrorCode(), token);
                        return null; // 토큰 오류는 null 반환으로 처리
                    }
                    
                    log.error("FCM 오류: {}", fcmError.getMessage());
                }
                throw new RuntimeException("FCM 알림 전송 중 오류 발생", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FCM 알림 전송 중 인터럽트 발생");
            throw new RuntimeException("FCM 알림 전송 중 인터럽트 발생", e);
        } catch (Exception e) {
            log.error("FCM 알림 전송 중 예외 발생: {}", e.getMessage());
            throw new RuntimeException("FCM 알림 전송 중 오류 발생", e);
        }
    }
    
    /**
     * 주제(topic)에 FCM 알림 전송
     * 
     * @param topic 대상 주제
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택 사항)
     * @return 메시지 ID
     */
    public String sendTopicNotification(String topic, String title, String body, Map<String, String> data) {
        try {
            // 상세 로그 추가
            log.info("===== FCM 주제 알림 전송 시작 =====");
            log.info("대상 주제: {}", topic);
            log.info("알림 제목: {}", title);
            log.info("알림 내용: {}", body);
            log.info("추가 데이터: {}", data);
            
            // 알림 메시지 구성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 메시지 구성
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(notification);
            
            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            // 안드로이드 설정
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();
            messageBuilder.setAndroidConfig(androidConfig);
            
            // 메시지 전송
            String response = FirebaseMessaging.getInstance().sendAsync(messageBuilder.build()).get();
            log.info("===== FCM 주제 알림 전송 완료 =====");
            log.info("메시지 ID: {}", response);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            log.error("===== FCM 주제 알림 전송 실패 =====", e);
            log.error("오류 메시지: {}", e.getMessage());
            throw new RuntimeException("FCM 주제 알림 전송 중 오류 발생", e);
        }
    }
    
    /**
     * 토큰 관련 오류인지 확인
     */
    private boolean isTokenRelatedError(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT || 
               errorCode == MessagingErrorCode.UNREGISTERED || 
               errorCode == MessagingErrorCode.SENDER_ID_MISMATCH;
    }
} 