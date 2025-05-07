package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.History;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryDao {
    void insert(History history);

    History findById(java.util.UUID historyId);

    History findByUserId(java.util.UUID userId);
}
