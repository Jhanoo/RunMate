// src/main/java/com/runhwani/runmate/dto/request/course/CourseRequest.java
package com.runhwani.runmate.dto.request.course;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * 코스 생성 요청 DTO
 * - @NotBlank, @NotNull 애노테이션 추가로 필수 필드 검증
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    /** 코스 이름 (필수) */
    @NotBlank(message = "코스 이름은 필수입니다")
    private String name;

    /** 공유 여부 */
    private boolean isShared;

    /** 총 거리 (필수) */
    @NotNull(message = "distance가 필요합니다")
    private Double distance;

    /** 평균 고도 (필수) */
    @NotNull(message = "avgElevation이 필요합니다")
    private Double avgElevation;

    /** 시작 위치 (필수) */
    @NotBlank(message = "startLocation은 필수입니다")
    private String startLocation;
}
