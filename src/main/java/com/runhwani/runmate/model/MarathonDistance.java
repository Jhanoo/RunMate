package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;

/**
 * 마라톤 거리(종목) 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarathonDistance {
    /** PK (marathon_distances.distance_id) */
    private UUID distanceId;
    /** 대회 (marathons.marathon_id FK) */
    private UUID marathonId;
    /** 거리 (예: "42.195km") */
    private String distance;
}
