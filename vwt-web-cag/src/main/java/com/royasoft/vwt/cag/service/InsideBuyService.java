/************************************************
 * Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.sundry.insideBuy.api.interfaces.InsideBuyInterface;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsInfo;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsModel;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Order;
import com.royasoft.vwt.soa.sundry.utils.Response;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 内购业务处理类（二期）
 *
 * @Author:yucong
 * @Since:2019年3月25日
 */
@Scope("prototype")
@Service
public class InsideBuyService implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InsideBuyService.class);

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	@Resource
	private InsideBuyInterface insideBuyInterface;

	@Autowired
	private OperationLogService operationLogService;

	@Override
	public void run() {
		while (true) {
			try {
				queue_packet = ServicesQueue.insidePurch_queue.take();// 获取队列处理数据
				msg = queue_packet.getMsg();// 获取请求信息
				channel = queue_packet.getChannel();// 获取连接
				if (msg instanceof HttpRequest) {
					HttpRequest request = (HttpRequest) msg;
					String function_id = queue_packet.getFunction_id(); // 获取功能ID
					String user_id = queue_packet.getUser_id(); // 获取用户ID
					String tel_number = queue_packet.getTel_number();
					String request_body = queue_packet.getRequest_body();// 获取参数实体

					/****************** 业务逻辑处理 ******************/

					String result = "";// 响应结果
					switch (function_id) {
					case FunctionIdConstant.INSIDE_BUY_GOODS_PAGE:
						result = getGoodsList(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_GOODS_DETAIL:
						result = getGoodsDetail(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_CART_PAGE:
						result = getShoppingCartList(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_CART_SAVE:
						result = saveShoppingCart(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_CART_UPDATE:
						result = updateShoppingCart(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_CART_DELETE:
						result = deleteShoppingCart(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_ORDER_SAVE:
						result = saveOrder(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_ORDER_PAGE:
						result = getOrderList(request_body);
						break;
					case FunctionIdConstant.INSIDE_BUY_ORDER_DELETE:
						result = deleteOrder(request_body);
						break;
					default:
						result = ResponsePackUtil.returnFaileInfo(); // 未知请求
						break;
					}

					// 响应成功
					ResponsePackUtil.responseStatusOK(channel, result);
					String responseStatus = ResponsePackUtil.getResCode(result);
					if (null != responseStatus && !"".equals(responseStatus))
						operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id,
								request_body, "", responseStatus);
				}
			} catch (Exception e) {
				logger.error("任务业务逻辑处理异常:{}", e);
				// 响应客户端异常
				ResponsePackUtil.responseStatusFaile(channel, "异常");
			} finally {
				// 2017/01/08 增加netty主动释放内存方法
				while (!ReferenceCountUtil.release(msg)) {
					// 自动释放netty计数器
				}
			}
		}
	}

	/**
	 * <li>获取商品列表</li>
	 * <li>phoneId查询用户购物车数量</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String getGoodsList(String requestBody) {
		logger.debug("内购-获取商品列表,参数获取,requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		Long phoneId = null;
		try {
			phoneId = requestJson.getLong("phoneId");
		} catch (Exception e) {
			logger.error("内购-获取商品列表,参数异常,requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.getGoodsList(phoneId);
			logger.debug("内购-获取商品列表,调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取商品列表,异常,requestBody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>根据机型编码查（modelNum）询对应机型下的所有型号</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String getGoodsDetail(String requestBody) {
		logger.debug("内购-获取单个商品详情,参数获取,request_body:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		String modelNum = requestJson.getString("modelNum");
		if (StringUtils.isEmpty(modelNum)) {
			logger.error("内购-获取单个商品详情,参数异常,requestBody：{}", requestBody);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.getGoodsDetail(modelNum);
			logger.debug("内购-获取单个商品详情,调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取单个商品详情,异常,requestBody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>根据phoneId获取用户购物车列表</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String getShoppingCartList(String requestBody) {
		logger.debug("内购-获取购物车列表，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		Long phoneId = null;
		try {
			phoneId = requestJson.getLong("phoneId");
		} catch (Exception e) {
			logger.error("内购-获取购物车列表，参数异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.getShoppingCartList(phoneId);
			logger.debug("内购-获取购物车列表，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取购物车列表，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>购物车添加功能</li>
	 * <li>goodsInfoId</li>
	 * <li>goodsModelId</li>
	 * <li>goodsCount</li>
	 * <li>phoneId</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String saveShoppingCart(String requestBody) {
		logger.debug("内购-购物车添加功能，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		// 参数处理
		Integer goodsCount = null;
		Long phoneId, goodsInfoId, goodsModelId = null;
		try {
			phoneId = requestJson.getLong("phoneId");
			goodsCount = requestJson.getInteger("goodsCount");
			goodsInfoId = requestJson.getLong("goodsInfoId");
			goodsModelId = requestJson.getLong("goodsModelId");
		} catch (Exception e) {
			logger.error("内购-购物车添加功能，参数异常，requestBody：{}", requestBody);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.saveShoppingCart(phoneId, goodsInfoId, goodsModelId, goodsCount);
			logger.debug("内购-购物车添加功能，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-购物车添加功能，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>购物车更新功能，根据cartId修改goodsCount，如果goodsCount=0，则调用删除功能</li>
	 * <li>cartId</li>
	 * <li>goodsCount</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String updateShoppingCart(String requestBody) {
		logger.debug("内购-购物车更新功能，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		// 参数处理
		Long cartId = null;
		Integer goodsCount = null;
		try {
			cartId = requestJson.getLong("cartId");
			goodsCount = requestJson.getInteger("goodsCount");
		} catch (Exception e) {
			logger.error("内购-购物车更新功能，参数异常，requestBody：{}", requestBody);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.updateShoppingCart(cartId, goodsCount);
			logger.debug("内购-购物车更新功能，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-购物车更新功能，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>购物车删除功能，在库存不足的情况下会根据 cartId 删除购物车信息</li>
	 * <li>cartId</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String deleteShoppingCart(String requestBody) {
		logger.debug("内购-购物车删除功能，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		// 参数处理
		Long cartId = null;
		try {
			cartId = requestJson.getLong("cartId");
		} catch (Exception e) {
			logger.error("内购-购物车删除功能，参数异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.deleteShoppingCart(cartId);
			logger.debug("内购-购物车删除功能，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-购物车删除功能，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>订单预约功能</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String saveOrder(String requestBody) {
		logger.debug("内购-订单预约功能，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		// 参数校验
		List<Order> list = parseJsonToListForSaveOrder(requestJson);
		if (CollectionUtils.isEmpty(list)) {
			logger.error("内购-订单预约功能，参数异常，requestBody:{}", requestBody);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.saveOrder(list);
			logger.debug("内购-订单预约功能，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			String code = null;
			String msg = e.getMessage();
			if (msg.contains("92025")) {
				code = ResponseInfoConstant.FAIL92025;
			} else if (msg.contains("92026")) {
				code = ResponseInfoConstant.FAIL92026;
			} else if (msg.contains("92027")) {
				code = ResponseInfoConstant.FAIL92027;
			} else {
				code = ResponseInfoConstant.FAIL1004;
			}
			logger.error("内购-订单预约功能，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(code, "");
		}
	}

	/**
	 * <li>获取订单列表，根据 phoneId 查询客户所有订单</li>
	 * <li>phoneId</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String getOrderList(String requestBody) {
		logger.debug("内购-获取订单列表,参数获取,requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		Long phoneId = null;
		try {
			phoneId = requestJson.getLong("phoneId");
		} catch (Exception e) {
			logger.error("内购-获取订单列表,参数异常,requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.getOrderList(phoneId);
			logger.debug("内购-获取订单列表,调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取订单列表,异常,requestBody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * <li>根据 orderGroup 删除订单</li>
	 * <li>根据 goodsModelId 找到某个产品，在库存里加 goodsCount</li>
	 * <li>orderGroup</li>
	 * <li>goodsCount</li>
	 * <li>goodsModelId</li>
	 * 
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	public String deleteOrder(String requestBody) {
		logger.debug("内购-订单删除功能，参数获取，requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		Object orders = requestJson.get("orders");

		// 参数校验
		List<JSONObject> list = parseJsonToListFordeleteOrder(orders);
		if (CollectionUtils.isEmpty(list)) {
			logger.error("内购-订单删除功能，参数异常，requestBody:{}", requestBody);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		try {
			Response response = insideBuyInterface.deleteShoppingCart(list);
			logger.debug("内购-订单删除功能，调用服务响应，response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-订单删除功能，异常，requestBody:{}，e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/********************************************************************************************/

	/**
	 * <li>将 List<map> 对象中的键值对放入 GoodsModel</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	@SuppressWarnings("unchecked")
	private List<JSONObject> parseJsonToListFordeleteOrder(Object object) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) object;
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		List<JSONObject> result = new ArrayList<>();
		JSONObject tem = null;
		try {
			for (Map<String, Object> map : list) {
				tem = new JSONObject();
				String orderGroup = String.valueOf(map.get("orderGroup"));
				if (StringUtils.isEmpty(orderGroup)) {
					return null;
				}
				tem.put("orderGroup", orderGroup);
				tem.put("goodsCount", Integer.parseInt(String.valueOf(map.get("goodsCount"))));
				tem.put("goodsModelId", Long.parseLong(String.valueOf(map.get("goodsModelId"))));
				result.add(tem);
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	/**
	 * <li>将 List<map> 对象中的键值对放入 GoodsModel</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	@SuppressWarnings("unchecked")
	private List<Order> parseJsonToListForSaveOrder(JSONObject jsonObject) {
		// 获取购买者信息
		JSONObject infoMap = (JSONObject) jsonObject.get("info");

		// 购买者信息校验
		String username, address, businessHall = null;
		Long phoneNum = null;
		try {
			username = infoMap.getString("username");
			address = infoMap.getString("address"); // 派送地址非必填
			businessHall = infoMap.getString("businessHall");
			phoneNum = infoMap.getLong("phoneNum");
			if (StringUtils.isEmpty(username) || StringUtils.isEmpty(businessHall) || !isMobile(phoneNum.toString())) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

		// 创建结果集
		List<Order> orderList = new ArrayList<>();

		// 创建参数
		Date date = new Date();
		String orderGroup = UUID.randomUUID().toString();
		Order order = null;
		GoodsInfo goodsInfo = null;
		GoodsModel goodsModel = null;

		// 获取订单信息
		List<JSONObject> list = (List<JSONObject>) jsonObject.get("orders");
		for (JSONObject orderMap : list) {
			order = new Order();
			goodsInfo = new GoodsInfo();
			goodsModel = new GoodsModel();

			Long goodsInfoId, goodsModelId, phoneId = null;
			Integer goodsCount = null;
			try {
				goodsInfoId = orderMap.getLong("goodsInfoId");
				goodsModelId = orderMap.getLong("goodsModelId");
				phoneId = orderMap.getLong("phoneId");
				goodsCount = orderMap.getInteger("goodsCount");

				goodsInfo.setId(goodsInfoId);
				goodsModel.setId(goodsModelId);

				order.setAddress(address);
				order.setBusinessHall(businessHall);
				order.setCreateTime(date);
				order.setGoodsCount(goodsCount);
				order.setGoodsInfo(goodsInfo);
				order.setGoodsModel(goodsModel);
				order.setOrderGroup(orderGroup);
				order.setPhoneId(phoneId);
				order.setPhoneNum(phoneNum);
				order.setUsername(username);

				orderList.add(order);
			} catch (Exception e) {
				return null;
			}
		}
		return orderList;
	}

	// 验证手机号
	private boolean isMobile(final String str) {
		Pattern p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
		Matcher m = p.matcher(str);
		return m.matches();
	}
}
