package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 마라톤 대회 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marathon {
    /** PK (marathons.marathon_id) */
    private UUID marathonId;
    /** 대회 이름 */
    private String name;
    /** 대회 일시 */
    private LocalDateTime date;
    /** 대회 장소 */
    private String location;
    /** 생성 시각 */
    private LocalDateTime createdAt;
}
