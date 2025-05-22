package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Marathon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface MarathonDao {
    List<Marathon> findUpcomingMarathons(@Param("now")OffsetDateTime now);

    Marathon findById(@Param("marathonId") UUID marathonId);
}
