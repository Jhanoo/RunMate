package com.runhwani.runmate.dto.request.group;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 그룹 생성 요청용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequest {
    /** 그룹명 */
    private String groupName;
    /** 연관 코스 ID */
    private UUID courseId;
    /** 시작 일시 */
    private LocalDateTime startTime;
    /** 시작 위치(주소) */
    private String startLocation;
    /** 시작 지점 위도 (선택) */
    private Double latitude;
    /** 시작 지점 경도 (선택) */
    private Double longitude;
}
