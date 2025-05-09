package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Marathon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface MarathonDao {
    List<Marathon> findUpcomingMarathons(@Param("now")OffsetDateTime now);
}
