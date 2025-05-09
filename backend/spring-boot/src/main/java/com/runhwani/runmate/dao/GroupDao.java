// src/main/java/com/runhwani/runmate/mapper/GroupMapper.java
package com.runhwani.runmate.dao;

import com.runhwani.runmate.dto.response.group.GroupMemberResponse;
import com.runhwani.runmate.model.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GroupDao {
    /**
     * 새 그룹을 테이블에 삽입
     */
    void insertGroup(Group group);

    /**
     * group_members 에 신규 멤버 추가
     * @param groupId 그룹 ID
     * @param userId  사용자 ID
     */
    void insertGroupMember(@Param("groupId") UUID groupId,
                           @Param("userId") UUID userId);

    /**
     * 내가 속한 그룹 하나를 조회
     * @param userId 사용자 ID
     * @return 내가 가입한 그룹 정보 (없으면 null)
     */
    Group selectGroupByUserId(UUID userId);

    /**
     * 해당 그룹의 그룹원들 조회
     * @param groupId 그룹 ID
     * @return 해당 그룹의 그룹원 리스트
     */
    List<GroupMemberResponse> selectMembersByGroupId(UUID groupId);

    /**
     * 초대 코드로 그룹 조회
     * @param inviteCode 초대 코드
     * @return 해당 그룹
     */
    Group selectByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 현재 가입한 그룹에서 유저 레코드 삭제
     * @param userId 나의 ID
     * */
    int deleteMember(@Param("userId") UUID userId);

    /**
     * 완주 후 isFinished를 true로 변경
     * @param userId 나의 ID
     * */
    int updateIsFinished(UUID userId);
}
