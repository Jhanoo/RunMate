// src/main/java/com/runhwani/runmate/dto/GroupResponse.java
package com.runhwani.runmate.dto.response.group;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 그룹 생성 응답용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    /** 생성된 그룹 ID */
    private UUID groupId;
    /** 발급된 초대 코드 */
    private String inviteCode;
    /** 그룹명 */
    private String groupName;
    /** 연관 코스 ID */
    private UUID courseId;
    /** 시작 일시 */
    private OffsetDateTime startTime;
    /** 시작 위치 */
    private String startLocation;
    /** 시작 지점 위도 */
    private Double latitude;
    /** 시작 지점 경도 */
    private Double longitude;
}
