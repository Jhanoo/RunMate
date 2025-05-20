package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FCMService fcmService;
    private final UserDao userDao;
    
    /**
     * 사용자에게 알림 전송
     * 
     * @param userId 대상 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택 사항)
     * @return 성공 여부
     */
    @Transactional
    public boolean sendNotificationToUser(UUID userId, String title, String body, Map<String, String> data) {
        try {
            // 사용자 정보 조회
            User user = userDao.findByUserId(userId);
            if (user == null) {
                log.warn("사용자를 찾을 수 없음: {}", userId);
                return false;
            }
            
            if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                log.warn("FCM 토큰이 없는 사용자: {}", userId);
                return false;
            }
            
            try {
                // FCM 알림 전송
                String result = fcmService.sendNotification(user.getFcmToken(), title, body, data);
                
                // 토큰 오류로 null이 반환된 경우
                if (result == null) {
                    log.warn("FCM 토큰 오류로 사용자({})의 토큰을 초기화합니다.", userId);
                    user.setFcmToken(null);
                    userDao.updateFcmToken(userId, null);
                    return false;
                }
                
                // 전송 성공
                log.info("FCM 알림 전송 성공 - 사용자: {}", userId);
                return true;
            } catch (Exception e) {
                // 오류 메시지에서 토큰 관련 오류 확인
                String errorMsg = e.getMessage();
                if (errorMsg != null && (
                    errorMsg.contains("not a valid FCM registration token") ||
                    errorMsg.contains("UNREGISTERED") ||
                    errorMsg.contains("SENDER_ID_MISMATCH"))) {
                    
                    log.warn("FCM 토큰 오류로 사용자({})의 토큰을 초기화합니다.", userId);
                    user.setFcmToken(null);
                    userDao.updateFcmToken(userId, null);
                }
                
                log.error("FCM 알림 전송 실패 - 사용자: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("알림 전송 처리 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 그룹 참여 알림 전송 예시
     * 
     * @param userId 대상 사용자 ID
     * @param groupId 그룹 ID
     * @param groupName 그룹 이름
     * @return 성공 여부
     */
    public boolean sendGroupJoinNotification(UUID userId, String groupId, String groupName) {
        String title = "그룹 참여 알림";
        String body = groupName + " 그룹에 참여했습니다.";
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "GROUP_JOIN");
        data.put("groupId", groupId);
        data.put("groupName", groupName);
        
        return sendNotificationToUser(userId, title, body, data);
    }
    
    /**
     * 특정 주제에 알림 전송
     * 
     * @param topic 주제
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택 사항)
     * @return 성공 여부
     */
    public boolean sendTopicNotification(String topic, String title, String body, Map<String, String> data) {
        try {
            fcmService.sendTopicNotification(topic, title, body, data);
            return true;
        } catch (Exception e) {
            log.error("주제 알림 전송 실패: {}", topic, e);
            return false;
        }
    }
} 