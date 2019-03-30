package com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces;

import java.util.List;

import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.GoodsInfoVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.SaveOrderVO;
import com.royasoft.vwt.soa.sundry.utils.Response;

/**
 * 山东 内购 需求
 * 
 * @author daizl
 *
 */
public interface InsidePruchInterface {

	/**
	 * 查询商品详情
	 * 
	 * @param goodsId
	 * @return
	 * @throws Exception
	 */
	public Response detailGoodsInfo(Long goodsId) throws Exception;

	/**
	 * 删除前检查该商品是否有订单
	 * 
	 * @param goodsId
	 * @param detailId
	 * @return
	 * @throws Exception
	 */
	public Response checkOrderRecord(Long goodsId, Long detailId) throws Exception;

	/**
	 * 新增商品信息
	 * 
	 * @param goodsInfoVO
	 * @return
	 * @throws Exception
	 */
	public Response insertGoodsInfo(GoodsInfoVO goodsInfoVO) throws Exception;

	/**
	 * 修改商品信息
	 * 
	 * @param goodsInfoVO
	 * @return
	 * @throws Exception
	 */
	public Response updateGoodsInfo(GoodsInfoVO goodsInfoVO) throws Exception;

	/**
	 * 删除商品信息（会删除对应的订单信息）
	 * 
	 * @param goodsId
	 * @return
	 * @throws Exception
	 */
	public Response deleteGoodsInfo(Long goodsId) throws Exception;

	/**
	 * 管理平台查询商品信息
	 * 
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	public Response pageGoodsInfoForPt(int page, int rows) throws Exception;

	/**
	 * 客户端查询商品信息
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public Response pageGoodsInfoForClient(String userId) throws Exception;

	/**
	 * 客户端预定商品
	 * @param userId
	 * @param recAddress
	 * @param orderList
	 * @return
	 * @throws Exception
	 */
	public Response orderGoods(String userId,String recAddress, List<SaveOrderVO> orderList) throws Exception;
	
	/**
	 * 客户端取消预订
	 * 
	 * @param userId
	 * @param orderList
	 * @return
	 * @throws Exception
	 */
	public Response orderCancel(String userId) throws Exception;

	/**
	 * 管理平台查询预定记录
	 * 
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	public Response pageOrderInfoForPt(int page, int rows) throws Exception;

	/**
	 * 客户端查询预定记录
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public Response pageOrderInfoForClient(String userId) throws Exception;

}
