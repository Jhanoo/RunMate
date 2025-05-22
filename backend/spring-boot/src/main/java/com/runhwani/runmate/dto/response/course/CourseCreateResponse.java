package com.runhwani.runmate.dto.response.course;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseCreateResponse {
    private UUID courseId;
}
