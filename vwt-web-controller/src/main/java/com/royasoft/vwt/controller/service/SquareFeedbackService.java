/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.PageUtils;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.soa.business.squeareFeedback.api.interfaces.SqueareFeedbackInterface;
import com.royasoft.vwt.soa.business.squeareFeedback.api.vo.SqueareFeedbackVo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

@Scope("prototype")
@Service
public class SquareFeedbackService implements Runnable {

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private SqueareFeedbackInterface squeareFeedbackInterface;

	@Autowired
	private ZkUtil zkUtil;

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	private final Logger logger = LoggerFactory.getLogger(SquareFeedbackService.class);

	@Override
	public void run() {
		while (true) {
			try {
				queue_packet = ServicesQueue.sqfeedback_queue.take();// 获取队列处理数据
				long t1 = System.currentTimeMillis();
				logger.info("==============开始时间:{}", t1);
				msg = queue_packet.getMsg();// 获取请求信息
				channel = queue_packet.getChannel();// 获取连接
				if (msg instanceof HttpRequest) {
					HttpRequest request = (HttpRequest) msg;
					String function_id = queue_packet.getFunction_id(); // 获取功能ID
					String user_id = queue_packet.getUser_id(); // 获取用户ID
					String request_body = queue_packet.getRequest_body();// 获取参数实体
					String tel_number = queue_packet.getTel_number();
					logger.debug("服务号反馈处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id,
							request_body);
					/***************************** 业务逻辑处理 *********************************************/

					String res = "";// 响应结果
					if (function_id == null || function_id.length() <= 0) {
						ResponsePackUtil.CalibrationParametersFailure(channel, "服务号反馈业务请求参数校验失败！");
					} else {
						res = sendTaskBusinessLayer(function_id, user_id, request_body, request);
					}
					ResponsePackUtil.responseStatusOK(channel, res); // 响应成功
					// 加入操作日志
					String responseStatus = ResponsePackUtil.getResCode(res);
					if (null != responseStatus && !"".equals(responseStatus))
						ResponsePackUtil.cagHttpResponse(channel, res);
					if (null != responseStatus && !"".equals(responseStatus)) {
						operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id,
								request_body, "", responseStatus);
					}
					continue;
				}
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} catch (Exception e) {
				logger.error("用户反馈业务逻辑处理异常", e);
				// 响应客户端异常
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} finally {
				// channel.close();
			}
		}

	}

	/**
	 * 服务号反馈分模块请求
	 * 
	 * @param function_id
	 * @param user_id
	 * @param request_body
	 * @param msg
	 * @return
	 */
	private String sendTaskBusinessLayer(String function_id, String user_id, String request_body, Object request) {

		String res = "";
		switch (function_id) {

		case FunctionIdConstant.SQUEAREFEEDBACKFINDALL:
			res = getSqFeedbackList(request_body, user_id);
			break;
		case FunctionIdConstant.SQUEAREFEEDBACKFINDONE:
			res = getSqFeedbackInfo(request_body, user_id);
			break;
		case FunctionIdConstant.SQUEAREFEEDBACKSAVE:
			res = deleteSqFeedbackById(request_body, user_id);
			break;
		default:
			res = ResponsePackUtil.returnFaileInfo(); // 未知请求
		}
		return res;
	}

	/**
	 * 用户反馈列表查询
	 * 
	 * @param request_body
	 * @param user_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getSqFeedbackList(String request_body, String user_id) {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
		logger.debug("获取反馈列表,requestBody:{},userId:{}", request_body, user_id);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		int pageIndex = 1;
		int pageSize = 10;

		// 地市区域条件查询
		Map<String, Object> condition = new HashMap<String, Object>();

		if (null != requestJson && !"".equals(requestJson)) {
			String page = requestJson.getString("page");// 前台传递的页面位置请求
			String limit = requestJson.getString("pageSize");// 前台传递的每页显示数
			String startTime = requestJson.getString("startTime");// 开始时间
			String endTime = requestJson.getString("endTime");// 结束时间
			String telNum = requestJson.getString("telNum");// 电话号码
		    //String feedBackStatus = requestJson.getString("fBFlag");// 反馈状态

			if (null != page && !"".equals(page)) {
				pageIndex = Integer.parseInt(page);
			}
			if (null != limit && !"".equals(limit)) {
				pageSize = Integer.parseInt(limit);
			}
			if (null != startTime && !"".equals(startTime)) {
				condition.put("start_time_questionDate", startTime+":00");
			}
			if (null != endTime && !"".equals(endTime)) {
				condition.put("end_time_questionDate", endTime+":00");
			}
			// conditions.put("end_time_planPushTime", dateFormat.format(new
			// Date()));

			if (null != telNum && !"".equals(telNum)) {
				condition.put("LIKE_telNum", telNum.trim());
			}
			
		}		
		condition.put("EQ_delFlag", "0");
		sortMap.put("questionDate", false);
		int total = 0;
		List<SqueareFeedbackVo> list = null;
		Map<String, Object> m = squeareFeedbackInterface.findAllByPage(pageIndex, pageSize, condition, sortMap);
		if (null != m) {
			list = (List<SqueareFeedbackVo>) m.get("content");
			total = PageUtils.getPageCount(Integer.parseInt(m.get("total").toString()), pageSize);
			if (total > 0) {
				// 封装后的数据
				List<Map<String, Object>> list1 = this.transeferTotable(list);
				model.put("success", true);
				model.put("items", list1);
				model.put("total", total);// 数据总数
				model.put("page", pageIndex);
			} else {
				// 数据不存在时返回一条无对应数据提示
				Map<String, Object> corpMap = new HashMap<String, Object>();
				corpMap.put("errorMessage", "数据不存在");
				List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
				tableList.add(corpMap);
				model.put("success", false);
				model.put("items", tableList);
				model.put("total", 1);// 数据总数
			}
		} else {
			// 数据查询异常返回异常提示
			Map<String, Object> corpMap = new HashMap<String, Object>();
			corpMap.put("errorMessage", "查询异常");
			List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
			tableList.add(corpMap);
			model.put("success", false);
			model.put("items", tableList);
			model.put("total", 1);// 数据总数
		}
		return JSONObject.toJSONString(model);
	}

	/**
	 * 服务号反馈列表查询
	 * 
	 * @param list
	 * @return
	 */
	public List<Map<String, Object>> transeferTotable(List<SqueareFeedbackVo> list) {
		List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
		try {
			logger.debug("反馈问题数据封装List:{}", list);
			for (SqueareFeedbackVo cv : list) {
				Map<String, Object> corpMap = new HashMap<String, Object>();
				// 数据封装
				corpMap.put("id", cv.getFkId());
				corpMap.put("question", cv.getQuestion());
				corpMap.put("membername", cv.getMembername());
				corpMap.put("squareName", cv.getSquareName());
				corpMap.put("telNum", cv.getTelNum());
				corpMap.put("comtent", cv.getContent());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(null!=cv.getFkDate()&&!"".equals(cv.getFkDate())){
					String str = sdf.format(cv.getFkDate());
					corpMap.put("fkDate", str);
				}else{
					corpMap.put("fkDate", "");
				}
				if(null!=cv.getCreateTime()&&!"".equals(cv.getCreateTime())){
                    String str = sdf.format(cv.getCreateTime());
                    corpMap.put("createTime", str);
                }else{
                    corpMap.put("createTime", "");
                }
				corpMap.put("FBFlag", cv.getFkFlag());
				tableList.add(corpMap);
			}
		} catch (Exception e) {
			logger.error("反馈问题数据封装异常", e);
		}
		return tableList;
	}

	public String getSqFeedbackInfo(String request_body, String user_id) {
		Map<String, Object> model = new HashMap<String, Object>();
		JSONObject requestJson = JSONObject.parseObject(request_body);
		String id = requestJson.getString("id");

		SqueareFeedbackVo squeareFeedbackVo = null;

		try {
			squeareFeedbackVo = squeareFeedbackInterface.findSqFeedbackById(id);
			// MemberInfoVO memberInfoVO =
			// memberInfoInterface.findById(squeareFeedbackVo.getUserId());
			if (null != squeareFeedbackVo) {
//				String fastDFSNode = Constants.fastDFSNode;
//				String trackerAddr = "";
//				try {
//					trackerAddr = zkUtil.findData(fastDFSNode);
//					logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
//				} catch (Exception e) {
//					logger.error("获取图片fast地址失败", e);
//				}
//				if (null != squeareFeedbackVo.getImg1() && !"".equals(squeareFeedbackVo.getImg1())) {
//					squeareFeedbackVo.setImg1(trackerAddr + squeareFeedbackVo.getImg1());
//				}
//
//				if (null != squeareFeedbackVo.getImg2() && !"".equals(squeareFeedbackVo.getImg2())) {
//					squeareFeedbackVo.setImg2(trackerAddr + squeareFeedbackVo.getImg2());
//				}
//
//				if (null != squeareFeedbackVo.getImg3() && !"".equals(squeareFeedbackVo.getImg3())) {
//					squeareFeedbackVo.setImg3(trackerAddr + squeareFeedbackVo.getImg3());
//				}
//
//				if (null != squeareFeedbackVo.getImg4() && !"".equals(squeareFeedbackVo.getImg4())) {
//					squeareFeedbackVo.setImg4(trackerAddr + squeareFeedbackVo.getImg4());
//				}
//
//				if (null != squeareFeedbackVo.getImg5() && !"".equals(squeareFeedbackVo.getImg5())) {
//					squeareFeedbackVo.setImg5(trackerAddr + squeareFeedbackVo.getImg5());
//				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(null!=squeareFeedbackVo.getFkDate()&&!"".equals(squeareFeedbackVo.getFkDate())){
					String str = sdf.format(squeareFeedbackVo.getFkDate());
					squeareFeedbackVo.setFkDateStr(str);
				}else{
					squeareFeedbackVo.setFkDateStr("");
				}
				if(null!=squeareFeedbackVo.getQuestionDate()&&!"".equals(squeareFeedbackVo.getQuestionDate())){
					String str = sdf.format(squeareFeedbackVo.getQuestionDate());
					squeareFeedbackVo.setQuestionDateStr(str);
				}else{
					squeareFeedbackVo.setQuestionDateStr("");
				}
				model.put("success", true);
				model.put("questionFeedBack", squeareFeedbackVo);
				model.put("resultMsg", Constants.ACTION_SUCCESS);
			} else {
				model.put("success", false);
				model.put("errorMessage", "没有数据");
			}
		} catch (Exception e) {
			model.put("success", false);
			model.put("errorMessage", Constants.ACTION_FAIL);
			logger.error("反馈问题查看异常", e);
		}
		return JSONObject.toJSONString(model);
	}

	/**
	 * 删除服务号反馈
	 * 
	 * @param request_body
	 * @param user_id
	 * @return
	 */
	public String deleteSqFeedbackById(String request_body, String user_id) {
		Map<String, Object> model = new HashMap<String, Object>();
		SqueareFeedbackVo resVo = new SqueareFeedbackVo();
		JSONObject requestJson = JSONObject.parseObject(request_body);
		String fkId = requestJson.getString("id");
		try {
			SqueareFeedbackVo vo = squeareFeedbackInterface.findSqFeedbackById(fkId);
			if (vo == null) {
				model.put("success", false);
				model.put("resultMsg", Constants.ACTION_FAIL);
			} else {
				vo.setDelFlag(1);
				resVo = squeareFeedbackInterface.save(vo);
			}

		} catch (Exception e) {
			logger.error("删除服务号反馈异常", e);
		}
		if (resVo != null) {
			model.put("success", true);
			model.put("resultMsg", Constants.ACTION_SUCCESS);
		} else {
			model.put("success", false);
			model.put("resultMsg", Constants.ACTION_FAIL);
		}
		return JSONObject.toJSONString(model);
	}

}
