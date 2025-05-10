package com.runhwani.runmate.dto.response.curriculum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

// CurriculumCreateResponse.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커리큘럼 생성 응답 (생성된 curriculum ID)")
public class CurriculumCreateResponse {
    @Schema(description = "생성된 커리큘럼 ID", example = "8b7f6e1a-...")
    private UUID curriculumId;
}

