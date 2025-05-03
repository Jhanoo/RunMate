// src/main/java/com/runhwani/runmate/service/impl/GroupServiceImpl.java
package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.model.Group;
import com.runhwani.runmate.dao.GroupDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupDao groupDao;

    @Override
    public Group createGroup(GroupRequest request) {
        // UUID 생성 및 초대 코드 생성
        UUID newId = UUID.randomUUID();
        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        // courseId 가 넘어왔을 때만 로직 적용
        Optional<UUID> hasCourse = Optional.ofNullable(request.getCourseId());

        // 도메인 모델에 요청 데이터 셋팅
        Group g = Group.builder()
                .groupId(newId)
                .groupName(request.getGroupName())
                .courseId(request.getCourseId())
                .startTime(request.getStartTime())
                .startLocation(request.getStartLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .inviteCode(inviteCode)
                .build();

        // DB에 insert
        groupDao.insert(g);
        return g;
    }
}
