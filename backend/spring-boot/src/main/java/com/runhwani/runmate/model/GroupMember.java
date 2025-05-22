package com.runhwani.runmate.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 그룹 멤버 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {
    /** PK (group_members.member_id) */
    private UUID memberId;
    /** 그룹 (groups.group_id FK) */
    private UUID groupId;
    /** 사용자 (users.user_id FK) */
    private UUID userId;
    /** 그룹 참여 시각 */
    private OffsetDateTime joinedAt;
    /** 완주 여부 */
    private Boolean isFinished;
}
