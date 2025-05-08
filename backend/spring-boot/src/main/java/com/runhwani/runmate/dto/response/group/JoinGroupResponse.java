// src/main/java/com/runhwani/runmate/dto/response/group/JoinGroupResponse.java
package com.runhwani.runmate.dto.response.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "그룹 입장 응답")
public class JoinGroupResponse {
    @Schema(description = "그룹 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID groupId;

    @Schema(description = "그룹명", example = "주말러닝")
    private String groupName;
}
