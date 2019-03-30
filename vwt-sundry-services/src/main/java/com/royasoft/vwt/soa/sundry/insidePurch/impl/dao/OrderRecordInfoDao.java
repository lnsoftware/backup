package com.royasoft.vwt.soa.sundry.insidePurch.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.OrderRecordInfo;

public interface OrderRecordInfoDao
		extends PagingAndSortingRepository<OrderRecordInfo, Long>, JpaSpecificationExecutor<OrderRecordInfo> {

	@Modifying
	@Query(value = "delete from OrderRecordInfo o where o.telNum=:telNum")
	public void deleteByTelNum(@Param("telNum") String telNum);
	
}
