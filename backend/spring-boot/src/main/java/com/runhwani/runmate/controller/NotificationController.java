package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.NotificationControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.notification.FcmTokenRequest;
import com.runhwani.runmate.service.NotificationService;
import com.runhwani.runmate.service.NotificationTodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDocs {

    private final NotificationService notificationService;
    private final NotificationTodoService notificationTodoService;
    
    /**
     * 테스트 알림 전송
     */
    @PostMapping("/test/{userId}")
    public ResponseEntity<CommonResponse<Boolean>> testNotification(@PathVariable UUID userId) {
        String title = "테스트 알림";
        String body = "FCM 알림 테스트입니다.";
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "TEST");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        try {
            boolean success = notificationService.sendNotificationToUser(userId, title, body, data);
            
            if (success) {
                return ResponseEntity.ok(new CommonResponse<>("알림 전송 성공", true));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new CommonResponse<>("알림 전송 실패 - FCM 토큰이 없거나 유효하지 않습니다", false));
            }
        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("알림 전송 중 오류 발생: " + e.getMessage(), false));
        }
    }
    
    /**
     * 오늘의 할 일 알림 전송
     */
    @PostMapping("/todos/{userId}")
    public ResponseEntity<CommonResponse<Boolean>> sendTodayTodoNotifications(@PathVariable UUID userId) {
        try {
            boolean success = notificationTodoService.sendUserTodayTodoNotifications(userId);
            
            if (success) {
                return ResponseEntity.ok(new CommonResponse<>("오늘의 할 일 알림 전송 완료", true));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new CommonResponse<>("알림 전송 실패 - FCM 토큰이 없거나 유효하지 않습니다", false));
            }
        } catch (Exception e) {
            log.error("할 일 알림 전송 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>("할 일 알림 전송 중 오류 발생: " + e.getMessage(), false));
        }
    }
    
    // 기존 토큰 관련 메서드들...
} 