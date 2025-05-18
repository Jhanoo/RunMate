package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            
            // FCM 알림 전송
            fcmService.sendNotification(user.getFcmToken(), title, body, data);
            return true;
        } catch (Exception e) {
            log.error("사용자 알림 전송 실패: {}", userId, e);
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