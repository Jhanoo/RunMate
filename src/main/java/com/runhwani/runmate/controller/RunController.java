package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.RunControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.run.RunEndRequest;
import com.runhwani.runmate.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runs")
public class RunController implements RunControllerDocs {
    private final RunService runService;

    @Override
    public ResponseEntity<CommonResponse<Void>> endRun(
            UserDetails principal,
            MultipartFile gpxFile,
            RunEndRequest request
    ) {
        try {
            UUID userId = UUID.fromString(principal.getUsername());
            runService.endRun(userId, gpxFile, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonResponse.error(e.getMessage()));
        }
    }
}
