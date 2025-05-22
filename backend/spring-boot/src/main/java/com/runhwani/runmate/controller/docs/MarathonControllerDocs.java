package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.marathon.MarathonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Tag(name = "마라톤", description = "마라톤 API")
@RequestMapping("/api/marathons")
public interface MarathonControllerDocs {
    @Operation(
            summary = "마라톤 전체 조회",
            description = "현재 날짜 이후에 개최되는 마라톤과 거리 목록을 반환합니다.",
            responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    ResponseEntity<CommonResponse<List<MarathonResponse>>> getMarathons();

    @Operation(
            summary = "마라톤 Id로 조회",
            description = "marathonId에 해당하는 마라톤 정보와 거리 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            })
    @GetMapping("/{marathonId}")  // 추가
    ResponseEntity<CommonResponse<MarathonResponse>> getMarathon(
            @PathVariable("marathonId") UUID marathonId  // 추가
    );
}
