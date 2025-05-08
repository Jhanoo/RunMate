package com.runhwani.runmate.dto.response.course;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDetailResponse {
    private UUID id;
    private String name;
    private boolean isShared;
    private double distance;
    private double avgElevation;
    private String startLocation;
    private String gpxFile;
    private Integer avgEstimatedTime;   // 초 단위
    private Integer userEstimatedTime;  // 초 단위
    private int likes;
}
