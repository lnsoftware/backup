package com.royasoft.vwt.soa.sundry.insidePurch.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.GoodsInfo;

public interface GoodsInfoDao extends PagingAndSortingRepository<GoodsInfo, Long>, JpaSpecificationExecutor<GoodsInfo> {

	@Query(value = "select max(g.sort) from GoodsInfo g")
	public Long findMaxSort();
}
