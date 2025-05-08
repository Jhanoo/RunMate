// src/main/java/com/runhwani/runmate/dto/request/group/JoinGroupRequest.java
package com.runhwani.runmate.dto.request.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "그룹 초대코드로 입장 요청")
public class JoinGroupRequest {
    @Schema(description = "초대 코드", example = "b793bfb6")
    @NotBlank
    private String inviteCode;
}
