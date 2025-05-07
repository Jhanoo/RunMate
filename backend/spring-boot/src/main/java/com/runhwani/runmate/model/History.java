package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 달리기 기록
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {
    /** PK (histories.history_id) */
    private UUID historyId;
    /** 사용자 (users.user_id FK) */
    private UUID userId;
    /** 코스 (courses.course_id FK, nullable) */
    private UUID courseId;
    /** 그룹 (groups.group_id FK, nullable) */
    private UUID groupId;
    /** GPX 파일 경로 */
    private String gpxFile;
    /** 시작 위치(주소) */
    private String startLocation;
    /** 시작 시각 */
    private OffsetDateTime startTime;
    /** 종료 시각 */
    private OffsetDateTime endTime;
    /** 달린 거리 */
    private Double distance;
    /** 평균 심박수 */
    private Double avgBpm;
    /** 평균 페이스 */
    private Double avgPace;
    /** 평균 케이던스 */
    private Double avgCadence;
    /** 평균 고도 */
    private Double avgElevation;
    /** 소모 칼로리 */
    private Double calories;
    /** 생성 시각 */
    private OffsetDateTime createdAt;
}
