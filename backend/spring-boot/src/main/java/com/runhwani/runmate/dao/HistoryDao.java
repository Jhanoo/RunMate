package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface HistoryDao {
    /**
     * 달리기 기록 저장
     */
    void insert(History history);

    /**
     * ID로 달리기 기록 조회
     */
    History findById(UUID historyId);

    /**
     * 사용자 ID로 달리기 기록 목록 조회
     */
    List<History> findByUserId(UUID userId);
    
    /**
     * 사용자 ID로 가장 최근 달리기 기록 조회
     */
    Optional<History> findLatestByUserId(UUID userId);
    
    /**
     * 페이징 처리된 사용자 기록 조회 (코스명, 그룹명 포함)
     */
    List<Map<String, Object>> findHistoryDetailsWithPaging(@Param("userId") UUID userId, 
                                                          @Param("offset") int offset, 
                                                          @Param("limit") int limit);
    
    /**
     * 사용자의 전체 기록 수 조회
     */
    long countByUserId(@Param("userId") UUID userId);
    
    /**
     * 그룹 멤버 프로필 이미지 조회
     */
    List<String> findGroupMemberProfilesByGroupId(@Param("groupId") UUID groupId);

    /**
     * 히스토리 ID로 상세 정보 조회
     */
    Map<String, Object> findHistoryDetailById(@Param("historyId") UUID historyId);
    
    /**
     * 그룹 ID로 그룹 러닝 참여자 기록 조회
     */
    List<Map<String, Object>> findGroupRunnersByGroupId(@Param("groupId") UUID groupId);
    
    /**
     * 히스토리 ID와 사용자 ID로 내 기록 조회
     */
    Map<String, Object> findMyRunDetail(@Param("historyId") UUID historyId, @Param("userId") UUID userId);
    
    /**
     * 코스 ID와 사용자 ID로 코스 추가 여부 확인
     */
    boolean isAddedToCourse(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    /**
     * 히스토리 ID로 상세 정보 조회 (Optional 반환)
     */
    Optional<History> selectHistoryById(@Param("historyId") UUID historyId);
    
    /**
     * 코스 생성 후 히스토리에 코스 ID 추가
     */
    void updateHistoryCourseId(@Param("historyId") UUID historyId, @Param("courseId") UUID courseId);
}
