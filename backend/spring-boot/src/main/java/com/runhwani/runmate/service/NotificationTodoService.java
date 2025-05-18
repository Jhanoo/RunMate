package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.NotificationTodoDao;
import com.runhwani.runmate.dao.UserDao;
import com.runhwani.runmate.model.Todo;
import com.runhwani.runmate.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationTodoService {

    private final NotificationTodoDao notificationTodoDao;
    private final NotificationService notificationService;
    private final UserDao userDao;
    
    /**
     * 사용자의 오늘 할 일 목록 조회
     */
    public List<Todo> getTodayTodosByUserId(UUID userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return notificationTodoDao.findTodosByUserIdAndDate(userId, today);
    }
    
    /**
     * 특정 날짜의 할 일 목록 조회
     */
    public List<Todo> getTodosByUserIdAndDate(UUID userId, LocalDate date) {
        return notificationTodoDao.findTodosByUserIdAndDate(userId, date);
    }
    
    /**
     * 할 일 생성
     */
    @Transactional
    public Todo createTodo(Todo todo) {
        todo.setTodoId(UUID.randomUUID());
        todo.setIsDone(false);
        notificationTodoDao.insert(todo);
        return todo;
    }
    
    /**
     * 할 일 완료 상태 변경
     */
    @Transactional
    public Todo updateTodoStatus(UUID todoId, boolean isDone) {
        Todo todo = notificationTodoDao.findById(todoId);
        if (todo == null) {
            throw new IllegalArgumentException("존재하지 않는 할 일입니다.");
        }
        
        todo.setIsDone(isDone);
        notificationTodoDao.updateStatus(todoId, isDone);
        return todo;
    }
    
    /**
     * 할 일 삭제
     */
    @Transactional
    public void deleteTodo(UUID todoId) {
        notificationTodoDao.delete(todoId);
    }
    
    /**
     * 특정 할 일에 대한 알림 전송
     * @return 성공 여부
     */
    public boolean sendTodoNotification(Todo todo) {
        String title = "오늘의 할 일 알림";
        String body = todo.getContent();
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "TODO");
        data.put("todoId", todo.getTodoId().toString());
        
        // curriculumId가 null이 아닌 경우에만 추가
        if (todo.getCurriculumId() != null) {
            data.put("curriculumId", todo.getCurriculumId().toString());
        }
        
        return notificationService.sendNotificationToUser(todo.getUserId(), title, body, data);
    }
    
    /**
     * 사용자의 오늘 할 일 알림 즉시 전송
     * @return 성공 여부
     */
    public boolean sendUserTodayTodoNotifications(UUID userId) {
        // 사용자 정보 조회 및 FCM 토큰 확인
        User user = userDao.findByUserId(userId);
        if (user == null || user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            log.warn("FCM 토큰이 없는 사용자: {}", userId);
            return false;
        }
        
        List<Todo> todayTodos = getTodayTodosByUserId(userId);
        boolean anyNotificationSent = false;
        
        if (todayTodos.isEmpty()) {
            // 오늘 할 일이 없는 경우
            String title = "오늘의 할 일";
            String body = "나만의 러닝 커리큘럼을 생성해 보세요!";
            
            Map<String, String> data = new HashMap<>();
            data.put("type", "TODO_EMPTY");
            
            anyNotificationSent = notificationService.sendNotificationToUser(userId, title, body, data);
        } else {
            // 오늘 할 일이 있는 경우
            for (Todo todo : todayTodos) {
                // isDone이 null인 경우 false로 처리
                boolean isDone = todo.getIsDone() != null && todo.getIsDone();
                
                if (!isDone) { // 완료되지 않은 할 일만 알림 전송
                    boolean sent = sendTodoNotification(todo);
                    if (sent) {
                        anyNotificationSent = true;
                    }
                }
            }
        }
        
        return anyNotificationSent;
    }
    
    /**
     * 매일 오전 9시에 모든 사용자에게 오늘의 할 일 알림 전송
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul")
    public void sendDailyTodoNotifications() {
        log.info("일일 할 일 알림 전송 시작");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Todo> allTodayTodos = notificationTodoDao.findAllTodosByDate(today);
        
        // 사용자별로 그룹화
        Map<UUID, Boolean> processedUsers = new HashMap<>();
        
        for (Todo todo : allTodayTodos) {
            UUID userId = todo.getUserId();
            
            // 이미 처리한 사용자는 건너뛰기
            if (processedUsers.containsKey(userId)) {
                continue;
            }
            
            boolean success = sendUserTodayTodoNotifications(userId);
            processedUsers.put(userId, success);
        }
        
        log.info("일일 할 일 알림 전송 완료: {} 명의 사용자에게 전송됨", processedUsers.size());
    }
} 