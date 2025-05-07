package com.runhwani.runmate.dto.request.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 생성 요청 정보")
public class GroupRequest {

    @Schema(description = "그룹명", example = "한강 러닝 모임")
    private String groupName;

    @Schema(description = "연관 코스 ID (없으면 null)", example = "4f5a6b7c-8d9e-0f1a-2b3c-4d5e6f7a8b9c")
    private UUID courseId;

    @Schema(description = "시작 일시 (ISO-8601, Offset 포함)", example = "2025-05-10T08:00:00+09:00")
    private OffsetDateTime startTime;

    @Schema(description = "시작 위치(주소)", example = "여의도 한강공원")
    private String startLocation;

    @Schema(description = "시작 지점 위도", example = "37.533422")
    private Double latitude;

    @Schema(description = "시작 지점 경도", example = "126.896495")
    private Double longitude;
}
