package com.royasoft.vwt.soa.sundry.insidePurch.impl.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.GoodsDetail;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.OrderRecord;

public interface OrderRecordDao
		extends PagingAndSortingRepository<OrderRecord, Long>, JpaSpecificationExecutor<OrderRecord> {

	@Modifying
	@Query(value = "delete from OrderRecord o where o.goodsDetail=:detail")
	public void deleteRecord(@Param("detail") GoodsDetail detail);
	
	@Query(value = "select o from OrderRecord o where o.goodsDetail=:detail")
	public List<OrderRecord> findRecordByDetail(@Param("detail") GoodsDetail detail);

	@Query(value = "select count(0) from OrderRecord o where o.goodsDetail=:detail ")
	public Long findRecordCount(@Param("detail") GoodsDetail detail);

	public List<OrderRecord> findByTelNumOrderByOrderTime(String telNum);

	@Query(value = "select sum(o.orderCount) from OrderRecord o where o.telNum=:telNum")
	public Long findUserOrderCount(@Param("telNum") String telNum);

	@Modifying
	@Query(value = "delete from OrderRecord o where o.telNum=:telNum")
	public void deleteByTelNum(@Param("telNum") String telNum);
}
