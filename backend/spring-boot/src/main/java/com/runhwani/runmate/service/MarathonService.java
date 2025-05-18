package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.response.marathon.MarathonResponse;

import java.util.List;
import java.util.UUID;

public interface MarathonService {
    List<MarathonResponse> getUpcomingMarathons();

    MarathonResponse getMarathon(UUID marathonId);
}
