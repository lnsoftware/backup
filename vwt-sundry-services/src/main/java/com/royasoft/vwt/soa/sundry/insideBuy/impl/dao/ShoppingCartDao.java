package com.royasoft.vwt.soa.sundry.insideBuy.impl.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.ShoppingCart;

public interface ShoppingCartDao extends JpaRepository<ShoppingCart, Long> {

    @Query("select count(c) from ShoppingCart c where c.phoneId = :phoneId")
    public Integer findNumOfCartByPhoneId(@Param("phoneId") Long phoneId);

    @Modifying
    @Query("delete from ShoppingCart cart where cart.id = :cartId")
    int deleteByCartId(@Param("cartId") Long cartId);

	@Query("select c from ShoppingCart c where c.phoneId=:phoneId and c.goodsInfo.id=:goodsInfoId and c.goodsModel.id=:goodsModelId")
	public ShoppingCart findByPhoneInfoModelId(@Param("phoneId") Long phoneId, @Param("goodsInfoId") Long goodsInfoId,
			@Param("goodsModelId") Long goodsModelId);

	@Modifying
	@Query("update ShoppingCart c set c.goodsCount=:goodsCount where c.cartId=:cartId")
	public int updateGoodsCountByCartId(@Param("cartId") Long cartId, @Param("goodsCount") Integer goodsCount);
}
