package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.MarathonControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.marathon.MarathonResponse;
import com.runhwani.runmate.service.MarathonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MarathonController implements MarathonControllerDocs {

    private final MarathonService marathonService;

    @Override
    public ResponseEntity<CommonResponse<List<MarathonResponse>>> getMarathons() {
        List<MarathonResponse> data = marathonService.getUpcomingMarathons();
        return ResponseEntity.ok(CommonResponse.ok(data));
    }
}
