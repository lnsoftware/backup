package com.royasoft.vwt.soa.sundry.insideBuy.api.interfaces;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsInfo;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsType;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Order;
import com.royasoft.vwt.soa.sundry.utils.Response;

/**
 * 山东 内购需求（二期）
 * 
 * @Author: yucong
 * @Since: 2019年3月25日
 */
public interface InsideBuyInterface {

    /**
     * <li>获取商品列表</li>
     * 
     * @param phoneId
     * @return
     * @throws Exception
     */
    public Response getGoodsList(Long phoneId) throws Exception;

    /**
     * <li>获取单个商品详情</li>
     * 
     * @param modelNum
     * @return
     * @throws Exception
     */
    public Response getGoodsDetail(String modelNum) throws Exception;

    /**
     * <li>根据phoneId获取用户购物车列表</li>
     * 
     * @param modelNum
     * @return
     * @throws Exception
     */
    public Response getShoppingCartList(Long phoneId) throws Exception;

    /**
     * <li>购物车添加功能</li>
     * 
     * @param
     * @return
     * @throws Exception
     */
	public Response saveShoppingCart(Long phoneId, Long goodsInfoId, Long goodsModelId, Integer goodsCount)
			throws Exception;

    /**
     * <li>购物车更新功能</li>
     * 
     * @param
     * @return
     * @throws Exception
     */
	public Response updateShoppingCart(Long cartId, Integer goodsCount) throws Exception;

	/**
	 * <li>购物车删除功能</li>
	 * 
	 * @param cartId
	 * @return
	 * @throws Exception
	 */
    public Response deleteShoppingCart(Long cartId) throws Exception;

    /**
	 * <li>获取订单列表</li>
	 * 
	 * @param phoneId
	 * @return
	 * @throws Exception
	 */
    public Response getOrderList(Long phoneId) throws Exception;

	/**
	 * <li>订单删除功能</li>
	 * 
	 * @param orderGroup
	 * @param goodsCount
	 * @param goodsInfoId
	 * @param goodsModelId
	 * @return
	 * @throws Exception
	 */
	public Response deleteShoppingCart(List<JSONObject> list) throws Exception;

	/**
	 * <li>订单保存功能</li>
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public Response saveOrder(List<Order> list) throws Exception;

	/***********************************************************************/

	/**
	 * <li>后台-获取商品列表</li>
	 * 
	 * @param page
	 * @param rows
	 * @return
	 */
	public Response getGoodsListForManager(int page, int rows);

	/**
	 * <li>后台-商品新增功能</li>
	 * 
	 * @param goodsInfo
	 * @return
	 */
	public Response saveGoodsForManager(GoodsInfo goodsInfo);

	/**
	 * <li>后台-获取单个商品详情</li>
	 * 
	 * @param goodsInfoId
	 * @return
	 */
	public Response getGoodsInfoForManager(Long goodsInfoId);

	/**
	 * <li>后台-商品编辑功能</li>
	 * 
	 * @param goodsInfo
	 * @return
	 */
	public Response updateGoodsForManager(GoodsInfo goodsInfo);

	/**
	 * <li>后台-商品删除功能</li>
	 * 
	 * @param goodsInfoId
	 * @return
	 */
	public Response deleteGoodsForManager(Long goodsInfoId);

	/**
	 * <li>后台-获取商品类型列表</li>
	 * 
	 * @param page
	 * @param rows
	 * @return
	 */
	public Response getTypeListForManager(int page, int rows);

	/**
	 * <li>后台-商品类型新增功能</li>
	 * 
	 * @param goodsType
	 * @return
	 */
	public Response saveTypeForManager(GoodsType goodsType);

	/**
	 * <li>后台-获取单个商品类型详情</li>
	 * 
	 * @param goodsTypeId
	 * @return
	 */
	public Response getTypeDetailForManager(Long goodsTypeId);

	/**
	 * <li>后台-商品类型编辑功能</li>
	 * 
	 * @param goodsType
	 * @return
	 */
	public Response updateTypeForManager(GoodsType goodsType);

	/**
	 * <li>后台-商品类型删除功能</li>
	 * 
	 * @param goodsTypeId
	 * @return
	 */
	public Response deleteTypeForManager(Long goodsTypeId);

	/**
	 * <li>后台-获取活动详情</li>
	 * 
	 * @return
	 */
	public Response getActivityDetail();

}
