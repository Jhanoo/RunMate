// src/main/java/com/runhwani/runmate/service/GroupService.java
package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.request.group.JoinGroupRequest;
import com.runhwani.runmate.dto.response.group.GroupMemberResponse;
import com.runhwani.runmate.dto.response.group.JoinGroupResponse;
import com.runhwani.runmate.model.Group;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    /**
     * 새 그룹을 생성하고 생성된 도메인 객체를 반환
     */
    Group createGroup(GroupRequest request, UUID userId);

    /**
     * 현재 사용자가 가입한 그룹 조회
     */
    Group getMyGroup(UUID userId);

    /**
     * 그룹의 멤버 리스트 조회
     */
    List<GroupMemberResponse> getGroupMembers(UUID groupId);


    /**
     * 초대코드로 그룹 입장
     */
    JoinGroupResponse joinByInviteCode(UUID userId, JoinGroupRequest request);

    /**
     * 현재 가입한 그룹 나가기(탈퇴)
     */
    void leaveGroup(UUID userId);

    /**
     * 완주 후 그룹 나가기(isfinished = true)
     */
    void finishGroup(UUID userId);

    /**
     * 그룹 상태 변경하기(그룹장)
     */
    void updateStatus(UUID groupId, int status);

}
