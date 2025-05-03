package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 사용자별 마라톤 커리큘럼
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curriculum {
    /** PK (curricula.curriculum_id) */
    private UUID curriculumId;
    /** 사용자 (users.user_id FK) */
    private UUID userId;
    /** 대회 (marathons.marathon_id FK, nullable) */
    private UUID marathonId;
    /** 목표 거리 */
    private String goalDist;
    /** 목표 날짜 */
    private LocalDateTime goalDate;
    /** 달리기 경험 여부 */
    private Boolean runExp;
    /** 경험 거리(예: "10km") */
    private String distExp;
    /** 주당 빈도(예: "3회") */
    private String freqExp;
}
