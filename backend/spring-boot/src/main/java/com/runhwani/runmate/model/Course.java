package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 코스 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    /** PK (courses.course_id) */
    private UUID courseId;
    /** 코스 이름 */
    private String courseName;
    /** 공유 여부 */
    private Boolean isShared;
    /** 총 거리 */
    private Double distance;
    /** 평균 고도 */
    private Double avgElevation;
    /** 시작 위치(주소) */
    private String startLocation;
    /** GPX 파일 경로 */
    private String gpxFile;
    /** 작성자 (users.user_id FK) */
    private UUID createdBy;
    /** 생성 시각 */
    private OffsetDateTime createdAt;
}
