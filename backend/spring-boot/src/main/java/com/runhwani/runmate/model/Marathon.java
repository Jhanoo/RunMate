package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

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
    private OffsetDateTime date;
    /** 대회 장소 */
    private String location;
    /** 생성 시각 */
    private OffsetDateTime createdAt;
}
