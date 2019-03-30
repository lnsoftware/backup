package com.royasoft.vwt.soa.sundry.insideBuy.impl.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsModelDao extends JpaRepository<GoodsModelDao, Long> {

	/**
	 * 客户下订单，从库存减去对应数量
	 * 
	 */

	@Modifying
	@Query("update GoodsModel g set g.inventory = (g.inventory - :preBuyCount) where g.inventory >= :preBuyCount and g.id = :goodsModelId")
	int minusInventoryById(@Param("goodsModelId") Long goodsModelId, @Param("preBuyCount") Integer preBuyCount);

	/**
	 * 客户取消订单，将购买数量返回给库存
	 * 
	 */
	@Modifying
	@Query("update GoodsModel m set m.inventory = (m.inventory + :goodsCount) where id = :goodsModelId")
	int addInventoryById(@Param("goodsModelId") Long goodsModelId, @Param("goodsCount") Integer goodsCount);

}
