package com.runhwani.runmate.dto.response.course;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private UUID courseId;
    private String courseName;
    private boolean isShared;
    private double distance;
    private double avgElevation;
    private String startLocation;
    private Creator creator;
    private int likeCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Creator {
        private String nickname;
        private String profileImage;
    }
}
