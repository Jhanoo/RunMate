package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.CurriculumControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.dto.response.curriculum.CurriculumCreateResponse;
import com.runhwani.runmate.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CurriculumController implements CurriculumControllerDocs {

    private final CurriculumService curriculumService;

    @Override
    public ResponseEntity<CommonResponse<CurriculumCreateResponse>> createCurriculum(
            CurriculumCreateRequest request,
            UserDetails principal) {

        // JWT 에서 꺼낸 userId (UUID 문자열)
        UUID userId = UUID.fromString(principal.getUsername());

        // 서비스 호출 후 생성된 ID 반환
        UUID curriculumId = curriculumService.generateCurriculum(userId, request);
        return ResponseEntity.ok(
                CommonResponse.ok(new CurriculumCreateResponse(curriculumId))
        );
    }
}
