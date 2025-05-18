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
            // 상세 로그 추가
            log.info("===== FCM 알림 전송 시작 =====");
            log.info("대상 토큰: {}", token);
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
            
            // 메시지 전송
            String response = FirebaseMessaging.getInstance().sendAsync(messageBuilder.build()).get();
            log.info("===== FCM 알림 전송 완료 =====");
            log.info("메시지 ID: {}", response);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            log.error("===== FCM 알림 전송 실패 =====", e);
            log.error("오류 메시지: {}", e.getMessage());
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
} 