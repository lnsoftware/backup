package com.royasoft.vwt.soa.sundry.insideBuy.impl.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Activity;

public interface ActivityDao extends JpaRepository<Activity, Long> {

    @Query("select a from Activity a where a.endTime > :nowTime")
    public List<Activity> findActivityByEndTime(@Param("nowTime") Date nowTime);


}
