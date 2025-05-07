// src/main/java/com/runhwani/runmate/mapper/GroupMapper.java
package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Group;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupDao {
    /**
     * 새 그룹을 테이블에 삽입
     */
    void insert(Group group);

    // (선택) 이후 찾기용
    Group findById(java.util.UUID groupId);
}
