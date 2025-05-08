// src/main/java/com/runhwani/runmate/service/impl/GroupServiceImpl.java
package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.GroupDao;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.request.group.JoinGroupRequest;
import com.runhwani.runmate.dto.response.group.GroupMemberResponse;
import com.runhwani.runmate.dto.response.group.JoinGroupResponse;
import com.runhwani.runmate.exception.EntityNotFoundException;
import com.runhwani.runmate.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupDao groupDao;

    @Transactional
    @Override
    public Group createGroup(GroupRequest request, UUID userId) {
        // UUID 생성 및 초대 코드 생성
        UUID newGroupId = UUID.randomUUID();
        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        // 도메인 모델에 요청 데이터 셋팅
        Group group = Group.builder().groupId(newGroupId).groupName(request.getGroupName()).leaderId(userId).courseId(request.getCourseId()).startTime(request.getStartTime()).startLocation(request.getStartLocation()).latitude(request.getLatitude()).longitude(request.getLongitude()).inviteCode(inviteCode).isFinished(false).build();

        // DB에 insert
        groupDao.insertGroup(group);

        groupDao.insertGroupMember(group.getGroupId(), userId);
        return group;
    }

    @Override
    public Group getMyGroup(UUID userId) {
        return groupDao.selectGroupByUserId(userId);
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(UUID groupId) {
        return groupDao.selectMembersByGroupId(groupId);
    }

    @Override
    @Transactional
    public JoinGroupResponse joinByInviteCode(UUID userId, JoinGroupRequest request) {
        // 1) 초대코드로 그룹 조회
        Group group = groupDao.selectByInviteCode(request.getInviteCode());
        if (group == null) {
            throw new EntityNotFoundException("유효한 초대코드를 가진 그룹이 없습니다.");
        }

        // 2) group_members 에 추가
        groupDao.insertGroupMember(group.getGroupId(), userId);

        // 3) 응답 반환
        return JoinGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .build();
    }

    @Override
    @Transactional
    public void leaveGroup(UUID userId) {
        // 삭제 시도
        int deleted = groupDao.deleteMember(userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "현재 가입한 그룹이 없습니다.");
        }
    }
}
