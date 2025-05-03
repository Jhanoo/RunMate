package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.GroupControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import com.runhwani.runmate.model.Group;
import com.runhwani.runmate.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GroupController implements GroupControllerDocs {

    private final GroupService groupService;

    @Override
    public ResponseEntity<CommonResponse<GroupResponse>> createGroup(
            @RequestBody GroupRequest request
    ) {
        // 서비스에서 도메인 모델로 생성 로직 수행
        Group created = groupService.createGroup(request);

        // 도메인 모델 → 응답 DTO 매핑
        GroupResponse resp = GroupResponse.builder()
                .groupId(created.getGroupId())
                .inviteCode(created.getInviteCode())
                .groupName(created.getGroupName())
                .courseId(created.getCourseId())
                .startTime(created.getStartTime())
                .startLocation(created.getStartLocation())
                .latitude(created.getLatitude())
                .longitude(created.getLongitude())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(resp));
    }
}
