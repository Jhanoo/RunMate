package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
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
}
