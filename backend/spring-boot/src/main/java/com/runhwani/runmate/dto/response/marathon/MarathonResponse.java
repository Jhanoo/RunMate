package com.runhwani.runmate.dto.response.marathon;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarathonResponse {
    private UUID marathonId;
    private String name;
    private LocalDate date;
    private String location;
    private List<String> distance;
}
