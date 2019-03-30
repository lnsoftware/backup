package com.royasoft.vwt.soa.sundry.insidePurch.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.GoodsDetail;

public interface GoodsDetailDao
		extends PagingAndSortingRepository<GoodsDetail, Long>, JpaSpecificationExecutor<GoodsDetail> {
}
