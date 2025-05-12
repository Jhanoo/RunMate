package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dto.response.history.*;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.exception.ErrorCode;
import com.runhwani.runmate.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceImpl implements HistoryService {

    private final HistoryDao historyDao;

    @Override
    @Transactional(readOnly = true)
    public HistoryListResponse getHistoryList(int page, int size) {
        // SecurityContext에서 현재 인증된 사용자의 ID 가져오기
        String userIdStr = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_USER));
        
        // UUID로 변환
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 페이지 번호는 1부터 시작하지만, offset은 0부터 시작
        int offset = (page - 1) * size;
        
        // 전체 기록 수 조회
        long total = historyDao.countByUserId(userId);
        
        // 페이징된 기록 조회 (코스명, 그룹명 포함)
        List<Map<String, Object>> historyDetails = historyDao.findHistoryDetailsWithPaging(userId, offset, size);
        
        // DTO 변환
        List<HistoryResponse> historyResponses = historyDetails.stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
        
        // 응답 객체 생성
        return HistoryListResponse.builder()
                .total(total)
                .page(page)
                .size(size)
                .histories(historyResponses)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistoryDetail(UUID historyId) {
        // 현재 인증된 사용자의 ID 가져오기
        String userIdStr = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_USER));
        
        // UUID로 변환
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 히스토리 상세 정보 조회
        Map<String, Object> historyDetail = historyDao.findHistoryDetailById(historyId);
        if (historyDetail == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "히스토리 정보를 찾을 수 없습니다: " + historyId);
        }
        
        // 그룹 ID 가져오기
        UUID groupId = convertToUUID(historyDetail.get("group_id"));
        if (groupId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "그룹 러닝 기록이 아닙니다.");
        }
        
        // 그룹 러닝 참여자 기록 조회
        List<Map<String, Object>> groupRunners = historyDao.findGroupRunnersByGroupId(groupId);
        List<GroupRunnerResponse> groupRunResponses = groupRunners.stream()
                .map(this::convertToGroupRunnerResponse)
                .collect(Collectors.toList());
        
        // 내 상세 기록 조회
        Map<String, Object> myRunDetail = historyDao.findMyRunDetail(historyId, userId);
        if (myRunDetail == null) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS, "해당 기록에 접근 권한이 없습니다.");
        }
        
        // 코스 추가 여부 확인
        UUID courseId = convertToUUID(myRunDetail.get("course_id"));
        boolean addedToCourse = false;
        if (courseId != null) {
            addedToCourse = historyDao.isAddedToCourse(courseId, userId);
        }
        
        // 내 상세 기록 변환
        MyRunDetailResponse myRunResponse = convertToMyRunDetailResponse(myRunDetail, addedToCourse);
        
        // 응답 객체 생성
        return HistoryDetailResponse.builder()
                .historyId(historyId)
                .groupRun(groupRunResponses)
                .myRun(myRunResponse)
                .build();
    }
    
    /**
     * Map 형태의 히스토리 데이터를 HistoryResponse DTO로 변환
     */
    private HistoryResponse convertToHistoryResponse(Map<String, Object> historyDetail) {
        // UUID 변환
        UUID historyId = convertToUUID(historyDetail.get("history_id"));
        UUID groupId = convertToUUID(historyDetail.get("group_id"));
        
        // 시간 관련 데이터 변환
        OffsetDateTime startTime = convertToOffsetDateTime(historyDetail.get("start_time"));
        OffsetDateTime endTime = convertToOffsetDateTime(historyDetail.get("end_time"));
        
        // 달린 시간 계산 (초 단위)
        long duration = 0;
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime).getSeconds();
        }
        
        // 그룹 멤버 프로필 이미지 조회
        List<String> members = new ArrayList<>();
        if (groupId != null) {
            members = historyDao.findGroupMemberProfilesByGroupId(groupId);
        }
        
        // 거리 변환
        Double distance = convertToDouble(historyDetail.get("distance"));
        
        return HistoryResponse.builder()
                .historyId(historyId)
                .courseName((String) historyDetail.get("course_name"))
                .groupName((String) historyDetail.get("group_name"))
                .location((String) historyDetail.get("start_location"))
                .startTime(startTime)
                .duration(duration)
                .members(members)
                .myDistance(distance)
                .build();
    }
    
    /**
     * 그룹 러닝 참여자 기록 변환
     */
    private GroupRunnerResponse convertToGroupRunnerResponse(Map<String, Object> runner) {
        return GroupRunnerResponse.builder()
                .userId(convertToUUID(runner.get("user_id")))
                .nickname((String) runner.get("nickname"))
                .distance(convertToDouble(runner.get("distance")))
                .time(convertToLong(runner.get("time")))
                .avgPace(convertToDouble(runner.get("avg_pace")))
                .build();
    }
    
    /**
     * 내 상세 기록 변환
     */
    private MyRunDetailResponse convertToMyRunDetailResponse(Map<String, Object> myRun, boolean addedToCourse) {
        return MyRunDetailResponse.builder()
                .distance(convertToDouble(myRun.get("distance")))
                .time(convertToLong(myRun.get("time")))
                .avgPace(convertToDouble(myRun.get("avg_pace")))
                .avgBpm(convertToDouble(myRun.get("avg_bpm")))
                .calories(convertToDouble(myRun.get("calories")))
                .addedToCourse(addedToCourse)
                .build();
    }
    
    /**
     * Object를 UUID로 변환 (null 처리 포함)
     */
    private UUID convertToUUID(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof UUID) {
            return (UUID) obj;
        }
        return UUID.fromString(obj.toString());
    }
    
    /**
     * Object를 OffsetDateTime으로 변환 (null 처리 포함)
     */
    private OffsetDateTime convertToOffsetDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof OffsetDateTime) {
            return (OffsetDateTime) obj;
        }
        if (obj instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) obj;
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        throw new IllegalArgumentException("Cannot convert to OffsetDateTime: " + obj.getClass());
    }
    
    /**
     * Object를 Double로 변환 (null 처리 포함)
     */
    private Double convertToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Double) {
            return (Double) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return Double.parseDouble(obj.toString());
    }
    
    /**
     * Object를 Long으로 변환 (null 처리 포함)
     */
    private long convertToLong(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return Long.parseLong(obj.toString());
    }
} 