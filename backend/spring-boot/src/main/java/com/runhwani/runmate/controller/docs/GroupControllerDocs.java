package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.request.group.JoinGroupRequest;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import com.runhwani.runmate.dto.response.group.JoinGroupResponse;
import com.runhwani.runmate.model.Group;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Swagger 문서 전용 인터페이스: 그룹 생성 API
 */
@Tag(name = "그룹", description = "그룹 생성 및 조회 API")
@RequestMapping("/api/groups")
public interface GroupControllerDocs {

    @Operation(
            summary = "그룹 생성",
            description = "그룹명, 코스 ID(선택), 시작 일시 및 위치 정보를 입력받아 새 달리기 그룹을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "그룹 생성 성공"
                    )
            }
    )
    @PostMapping("/create")
    ResponseEntity<CommonResponse<Group>> createGroup(
            @RequestBody GroupRequest request,
            @AuthenticationPrincipal UserDetails principal
    );

    @Operation(
            summary = "내가 가입한 그룹 조회",
            description = "토큰에 담긴 내 userId로 내가 속한 그룹 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "만약 속한 그룹이 없으면 data: null 반환"
                    )
            }
    )
    @GetMapping("/current")
    ResponseEntity<CommonResponse<GroupResponse>> getMyGroup(@AuthenticationPrincipal UserDetails principal);

    @Operation(
            summary = "초대코드로 그룹 가입",
            description = "초대코드를 입력받아 해당 그룹에 가입 처리하고, 그룹 정보(아이디, 이름)를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "그룹 입장 성공 및 그룹 정보 반환"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "유효한 초대코드를 가진 그룹이 없는 경우",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "NotFoundExample",
                                            value = "{\"message\": \"유효한 초대코드를 가진 그룹이 없습니다.\", \"data\": null}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/join")
    ResponseEntity<CommonResponse<JoinGroupResponse>> joinGroup(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid JoinGroupRequest req);


    @Operation(
            summary = "그룹 나가기 (탈퇴)",
            description = "사용자가 현재 가입한 그룹에서 탈퇴 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "삭제할 그룹 멤버 정보가 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "NotFound",
                                            value = "{\"message\": \"그룹 멤버 정보가 없습니다 or 이미 탈퇴한 상태입니다.\", \"data\": null}"
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/leave")
    ResponseEntity<CommonResponse<Void>> leaveGroup(@AuthenticationPrincipal UserDetails principal);
}
