package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.run.RunEndRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Tag(name = "달리기", description = "러닝 관련 API")
@RequestMapping("/api/runs")
public interface RunControllerDocs {

    @Operation(
            summary = "달리기 종료",
            description = "GPX 파일과 러닝 통계 데이터를 전송하여 달리기를 종료하고 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    // multipart/form-data 각 파트 스펙을 StringToClassMapItem 으로 정의
                                    properties = {
                                            @StringToClassMapItem(key = "gpxFile", value = File.class),
                                            @StringToClassMapItem(key = "request", value = RunEndRequest.class)
                                    }, requiredProperties = {"gpxFile", "request"}
                            ), encoding = {
                                    @Encoding(name = "gpxFile", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
                                    @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "달리기 종료 및 저장 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "실패 응답 예시",
                                            value = "{\"message\":\"GPX 파일 파싱 오류\",\"data\":null}"
                                    )
                            )),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping(
            path = "/end",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CommonResponse<Void>> endRun(
            @AuthenticationPrincipal UserDetails principal,

            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "gpxFile",
                    description = "경로 정보를 담은 GPX 파일",
                    required = true,
                    content = @Content(mediaType = "application/gpx+xml")
            )
            @RequestPart("gpxFile") MultipartFile gpxFile,

            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "request",
                    description = "달리기 통계 정보",
                    required = true
            )
            @RequestPart("request") RunEndRequest request
    );
}
