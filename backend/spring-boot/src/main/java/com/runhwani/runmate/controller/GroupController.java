package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.GroupControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.request.group.JoinGroupRequest;
import com.runhwani.runmate.dto.response.group.GroupMemberResponse;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import com.runhwani.runmate.dto.response.group.JoinGroupResponse;
import com.runhwani.runmate.model.Group;
import com.runhwani.runmate.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GroupController implements GroupControllerDocs {

    private final GroupService groupService;

    @Override
    public ResponseEntity<CommonResponse<Group>> createGroup(
            @RequestBody GroupRequest request,
            UserDetails principal
    ) {
        UUID userId = UUID.fromString(principal.getUsername());

        // 서비스에서 도메인 모델로 생성 로직 수행
        Group created = groupService.createGroup(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(created));
    }

    @Override
    public ResponseEntity<CommonResponse<GroupResponse>> getMyGroup(UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());

        // 1) 내가 속한 그룹 도메인 조회
        Group myGroup = groupService.getMyGroup(userId);
        if (myGroup == null) {
            // 가입된 그룹이 없으면 null 반환
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.ok(null));
        }

        // 2) 조회한 그룹의 ID 로 멤버 리스트 조회
        List<GroupMemberResponse> memberList = groupService.getGroupMembers(myGroup.getGroupId());

        // 3) 도메인 → 응답 DTO 매핑
        GroupResponse groupRes = GroupResponse.builder()
                .groupId(myGroup.getGroupId())
                .leaderId(myGroup.getLeaderId())
                .inviteCode(myGroup.getInviteCode())
                .groupName(myGroup.getGroupName())
                .courseId(myGroup.getCourseId())
                .startTime(myGroup.getStartTime())
                .startLocation(myGroup.getStartLocation())
                .latitude(myGroup.getLatitude())
                .longitude(myGroup.getLongitude())
                .members(memberList)
                .build();

        return ResponseEntity.ok(CommonResponse.ok(groupRes));
    }

    public ResponseEntity<CommonResponse<JoinGroupResponse>> joinGroup(UserDetails principal, JoinGroupRequest req) {
        UUID userId = UUID.fromString(principal.getUsername());
        JoinGroupResponse res = groupService.joinByInviteCode(userId, req);

        return ResponseEntity.ok(CommonResponse.ok(res));
    }

    public ResponseEntity<CommonResponse<Void>> leaveGroup(UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());
        groupService.leaveGroup(userId);

        return ResponseEntity.ok(CommonResponse.ok(null));
    }

}
