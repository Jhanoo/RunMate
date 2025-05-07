package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 그룹 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    /** PK (groups.group_id) */
    private UUID groupId;
    /** 그룹 이름 */
    private String groupName;
    /** 연관 코스 (courses.course_id FK) */
    private UUID courseId;
    /** 시작 시각 */
    private OffsetDateTime startTime;
    /** 시작 위치(주소) */
    private String startLocation;
    /** 시작 지점 위도 */
    private Double latitude;
    /** 시작 지점 경도 */
    private Double longitude;
    /** 초대 코드 */
    private String inviteCode;
}
