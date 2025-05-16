package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.GroupControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.request.group.JoinGroupRequest;
import com.runhwani.runmate.dto.response.group.GroupMemberResponse;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import com.runhwani.runmate.dto.response.group.JoinGroupResponse;
import com.runhwani.runmate.model.Course;
import com.runhwani.runmate.model.Group;
import com.runhwani.runmate.model.History;
import com.runhwani.runmate.service.CourseService;
import com.runhwani.runmate.service.GroupService;
import com.runhwani.runmate.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GroupController implements GroupControllerDocs {

    private final GroupService groupService;
    private final CourseService courseService;
    private final HistoryService historyService;

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
            return ResponseEntity.ok(CommonResponse.error("가입된 그룹이 없습니다."));
        }

        Course course;
        String courseName = null;
        UUID courseId = myGroup.getCourseId();

        if (courseId != null) {
            course = courseService.getCourseByCourseId(courseId);
            courseName = course.getCourseName();
        }

        // 2) 조회한 그룹의 ID 로 멤버 리스트 조회
        List<GroupMemberResponse> memberList = groupService.getGroupMembers(myGroup.getGroupId());

        // 3) 도메인 → 응답 DTO 매핑
        GroupResponse groupRes = GroupResponse.builder()
                .groupId(myGroup.getGroupId())
                .courseName(courseName)
                .leaderId(myGroup.getLeaderId())
                .inviteCode(myGroup.getInviteCode())
                .groupName(myGroup.getGroupName())
                .courseId(myGroup.getCourseId())
                .startTime(myGroup.getStartTime())
                .startLocation(myGroup.getStartLocation())
                .latitude(myGroup.getLatitude())
                .longitude(myGroup.getLongitude())
                .status(myGroup.getStatus())
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

        try {
            groupService.leaveGroup(userId);
        } catch (ResponseStatusException e) {
            // ex.getStatusCode() → 404, ex.getReason() → "현재 가입한 그룹이 없습니다."
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(CommonResponse.error(e.getReason()));
        }
        return ResponseEntity.ok(CommonResponse.ok(null));
    }


    public ResponseEntity<CommonResponse<Void>> finishGroup(UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());

        Group myGroup = groupService.getMyGroup(userId);

        // 가입된 그룹이 없으면 404 반환
        if (myGroup == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.error("현재 가입한 그룹이 없습니다."));
        }

        // GroupMember의 is_finished를 true로 변경
        try {
            groupService.finishGroup(userId);
        } catch (ResponseStatusException e) {
            // ex.getStatusCode() → 404, ex.getReason() → "현재 가입한 그룹이 없습니다."
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(CommonResponse.error(e.getReason()));
        }

        // 그룹장이면 그룹 상태를 완료로 변경
        if (myGroup.getLeaderId().equals(userId)) {
            groupService.updateStatus(myGroup.getGroupId(), 2);
        }

        return ResponseEntity.ok(CommonResponse.ok(null));
    }

    public ResponseEntity<CommonResponse<Void>> startGroup(UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());

        Group myGroup = groupService.getMyGroup(userId);

        // 가입된 그룹이 없으면 404 반환
        if (myGroup == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.error("현재 가입한 그룹이 없습니다."));
        }

        // 그룹장이 아니면 401 반환
        if (!myGroup.getLeaderId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.error("그룹장이 아닙니다."));
        }

        groupService.updateStatus(myGroup.getGroupId(), 1);

        return ResponseEntity.ok(CommonResponse.ok(null));
    }

    public ResponseEntity<CommonResponse<Boolean>> hasGroupHistory(UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());

        boolean hasHistory = historyService.hasGroupHistory(userId);

        return ResponseEntity.ok(CommonResponse.ok(hasHistory));
    }
}
