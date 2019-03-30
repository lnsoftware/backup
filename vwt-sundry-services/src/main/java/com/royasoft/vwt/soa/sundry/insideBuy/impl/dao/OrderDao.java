package com.royasoft.vwt.soa.sundry.insideBuy.impl.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Order;

public interface OrderDao extends JpaRepository<Order, Long> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from vwt_ins_order where order_group = :orderGroup")
	int deleteByOrderGroup(@Param("orderGroup") String orderGroup);

	@Query("select count(o) from Order o where o.goodsInfo.id = :goodsInfoId")
	public int findAllByGoodsInfoId(@Param("goodsInfoId") Long goodsInfoId);

}
