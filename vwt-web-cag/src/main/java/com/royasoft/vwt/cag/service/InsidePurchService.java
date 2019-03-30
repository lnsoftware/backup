/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces.InsidePruchInterface;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.SaveOrderVO;
import com.royasoft.vwt.soa.sundry.utils.Response;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 内购业务处理类
 *
 * @Author:MB
 * @Since:2015年8月26日
 */
@Scope("prototype")
@Service
public class InsidePurchService implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InsidePurchService.class);

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	@Resource
	private InsidePruchInterface insidePruchInterface;

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

					/***************************** 业务逻辑处理 *********************************************/

					String res = "";// 响应结果
					switch (function_id) {
					case FunctionIdConstant.INSIDE_PURCH_GOODS_ORDER:
						res = orderGoods(request_body);
						break;
					case FunctionIdConstant.INSIDE_PURCH_GOODS_PAGE:
						res = pageGoods(request_body);
						break;
					case FunctionIdConstant.INSIDE_PURCH_ORDERS_DELETE:
						res = deleteOrder(request_body);
						break;
					case FunctionIdConstant.INSIDE_PURCH_ORDERS_PAGE:
						res = pageOrders(request_body);
						break;
					default:
						res = ResponsePackUtil.returnFaileInfo(); // 未知请求
						break;
					}
					// 响应成功
					ResponsePackUtil.responseStatusOK(channel, res);
					String responseStatus = ResponsePackUtil.getResCode(res);
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
	 * 获取订购详情
	 * 
	 * @param requestBody
	 * @return
	 */
	public String pageOrders(String requestBody) {
		logger.debug("内购-获取订购详情,requestBody:{}", requestBody);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		try {
			Response response = insidePruchInterface.pageOrderInfoForClient(requestJson.getString("userId"));
			logger.debug("内购-获取订购详情 调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取订购详情异常,requestbody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * 订购商品
	 * 
	 * @param requestBody
	 * @return
	 */
	public String orderGoods(String requestBody) {
		logger.debug("内购-订购商品,requestBody:{}", requestBody);
		try {
			JSONObject requestJson = JSONObject.parseObject(requestBody);
			Response response = insidePruchInterface.orderGoods(requestJson.getString("userId"),
					requestJson.getString("recAddress"),
					JSON.parseArray(requestJson.getString("orderList"), SaveOrderVO.class));
			logger.debug("内购-订购商品 调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-订购商品 异常,requestbody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * 获取商品列表
	 * 
	 * @param requestBody
	 * @return
	 */
	public String pageGoods(String requestBody) {
		logger.debug("内购-获取商品列表,requestBody:{}", requestBody);
		try {
			JSONObject requestJson = JSONObject.parseObject(requestBody);
			Response response = insidePruchInterface.pageGoodsInfoForClient(requestJson.getString("userId"));
			logger.debug("内购-获取商品列表 调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-获取商品列表 异常,requestbody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}

	/**
	 * 取消订购
	 * 
	 * @param requestBody
	 * @return
	 */
	public String deleteOrder(String requestBody) {
		logger.debug("内购-取消订购,requestBody:{}", requestBody);
		try {
			JSONObject requestJson = JSONObject.parseObject(requestBody);
			Response response = insidePruchInterface.orderCancel(requestJson.getString("userId"));
			logger.debug("内购-取消订购 调用服务响应,response", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购-取消订购 异常,requestbody:{},e:{}", requestBody, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "");
		}
	}
}
