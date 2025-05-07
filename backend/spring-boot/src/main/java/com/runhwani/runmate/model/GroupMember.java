package com.runhwani.runmate.model;

import lombok.*;
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
    /** 리더 여부 */
    private Boolean isLeader;
}
