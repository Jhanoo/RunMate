package com.runhwani.runmate.dto.response.course;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLikeResponse {
    private boolean liked;
    private int totalLikes;
}
