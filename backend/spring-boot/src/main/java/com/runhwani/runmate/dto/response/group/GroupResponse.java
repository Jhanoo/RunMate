package com.runhwani.runmate.dto.response.group;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 생성 응답 정보")
public class GroupResponse {

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

    @ArraySchema(
            arraySchema = @Schema(description = "그룹 멤버 리스트"),
            schema = @Schema(implementation = GroupMemberResponse.class))
    private List<GroupMemberResponse> members;
}
