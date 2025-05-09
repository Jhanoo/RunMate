package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface HistoryDao {
    void insert(History history);

    History findByUserId(java.util.UUID userId);

    // course 생성시 사용
    Optional<History> selectHistoryById(@Param("historyId") UUID historyId);
}
