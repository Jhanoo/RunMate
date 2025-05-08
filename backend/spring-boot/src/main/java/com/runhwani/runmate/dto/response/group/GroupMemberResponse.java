package com.runhwani.runmate.dto.response.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 생성 응답 정보")
public class GroupMemberResponse {

    @Schema(description = "그룹원 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID memberId;

    @Schema(description = "닉네임", example = "runner1")
    private String nickname;

    @Schema(description = "프로필 사진 경로", example = "imageUrl")
    private String profileImage;
}

