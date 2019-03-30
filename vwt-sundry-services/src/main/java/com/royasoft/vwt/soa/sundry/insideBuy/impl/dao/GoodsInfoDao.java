package com.royasoft.vwt.soa.sundry.insideBuy.impl.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsInfo;

public interface GoodsInfoDao extends JpaRepository<GoodsInfo, Long> {

	@Query("select g from GoodsInfo g order by g.goodsType.typeOrder, g.goodsNum, g.updateTime")
	Page<GoodsInfo> getGoodsListForManager(Pageable pageable);


}
