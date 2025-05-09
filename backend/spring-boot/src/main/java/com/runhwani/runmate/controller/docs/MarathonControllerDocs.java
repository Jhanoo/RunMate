package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.marathon.MarathonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "마라톤", description = "마라톤 API")
@RequestMapping("/api/marathons")
public interface MarathonControllerDocs {
    @Operation(
            summary = "마라톤 조회",
            description = "현재 날짜 이후에 개최되는 마라톤과 거리 목록을 반환합니다.",
            responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    ResponseEntity<CommonResponse<List<MarathonResponse>>> getMarathons();
}
