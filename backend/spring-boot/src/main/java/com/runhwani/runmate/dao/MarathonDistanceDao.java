package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.MarathonDistance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MarathonDistanceDao {
    List<MarathonDistance> findByMarathonId(@Param("marathonId") UUID marathonId);
}
