package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.CurriculumControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.dto.response.curriculum.CurriculumCreateResponse;
import com.runhwani.runmate.dto.response.curriculum.TodoResponse;
import com.runhwani.runmate.model.Curriculum;
import com.runhwani.runmate.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public ResponseEntity<CommonResponse<Curriculum>> getCurriculum(UserDetails principal) {
        // 1. JWT 토큰에서 userId 추출
        UUID userId = UUID.fromString(principal.getUsername());

        // 2. 서비스 호출: Curriculum 반환
        Curriculum curriculum = curriculumService.getMyCurriculum(userId);

        return ResponseEntity.ok(CommonResponse.ok(curriculum));
    }


}
