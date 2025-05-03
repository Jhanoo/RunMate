// src/main/java/com/runhwani/runmate/service/GroupService.java
package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.model.Group;

public interface GroupService {
    /**
     * 새 그룹을 생성하고 생성된 도메인 객체를 반환
     */
    Group createGroup(GroupRequest request);
}
