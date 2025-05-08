package com.runhwani.runmate.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "생성된 그룹 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID groupId;

    @Schema(description = "그룹명", example = "한강 러닝 모임")
    private String groupName;

    @Schema(description = "그룹장 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID leaderId;

    @Schema(description = "선택한 코스 ID", example = "4f5a6b7c-8d9e-0f1a-2b3c-4d5e6f7a8b9c")
    private UUID courseId;

    @Schema(description = "시작 일시 (ISO-8601, Offset 포함)", example = "2025-05-10T08:00:00+09:00")
    private OffsetDateTime startTime;

    @Schema(description = "시작 위치(주소)", example = "여의도 한강공원")
    private String startLocation;

    @Schema(description = "시작 지점 위도", example = "37.533422")
    private Double latitude;

    @Schema(description = "시작 지점 경도", example = "126.896495")
    private Double longitude;

    @Schema(description = "발급된 초대 코드", example = "a1b2c3d4")
    private String inviteCode;

    @Schema(description = "그룹 종료 여부", example = "false")
    private Boolean isFinished;
}
