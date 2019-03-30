/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces.InsidePruchInterface;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.GoodsInfoVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.OrderRecordInfoVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.OrderRecordVO;
import com.royasoft.vwt.soa.sundry.utils.Response;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

@Scope("prototype")
@Service
public class InsidePurchService implements Runnable {

	private static String[] titleArr = { "预约时间", "员工姓名", "电话", "员工编号", "公司/部门", "派送地址","预约信息" };

	@Autowired
	private InsidePruchInterface insidePruchInterface;

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private ZkUtil zkUtil;

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	private final Logger logger = LoggerFactory.getLogger(InsidePurchService.class);

	@Override
	public void run() {
		while (true) {
			try {
				// 获取InternetAuth的队列处理数据
				queue_packet = ServicesQueue.inside_queue.take();
				msg = queue_packet.getMsg();// 获取请求信息
				channel = queue_packet.getChannel();// 获取连接
				if (msg instanceof HttpRequest) {
					HttpRequest request = (HttpRequest) msg;
					String function_id = queue_packet.getFunction_id(); // 获取功能ID
					String user_id = queue_packet.getUser_id(); // 获取用户ID
					String tel_number = queue_packet.getTel_number();
					String request_body = queue_packet.getRequest_body();// 获取参数实体

					logger.debug("内购商品处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id,
							request_body);
					/***************************** 业务逻辑处理 *********************************************/

					String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

					switch (function_id) {
					// 山东需求-内购手机-商品-新增
					case FunctionIdConstant.INSIDE_PURCH_GOODS_ADD:
						resInfo = addGoods(request_body);
						break;
					// 山东需求-内购手机-商品-删除
					case FunctionIdConstant.INSIDE_PURCH_GOODS_DELETE:
						resInfo = deleteGoods(request_body);
						break;
					case FunctionIdConstant.INSIDE_PURCH_CHECK_ORDERS:
						resInfo = checkOrders(request_body);
						break;
					// 山东需求-内购手机-商品-详情
					case FunctionIdConstant.INSIDE_PURCH_GOODS_DETAIL:
						resInfo = detailGoods(request_body);
						break;
					// 山东需求-内购手机-商品-分页
					case FunctionIdConstant.INSIDE_PURCH_GOODS_PAGE:
						resInfo = pageGoods(request_body);
						break;
					// 山东需求-内购手机-商品-修改
					case FunctionIdConstant.INSIDE_PURCH_GOODS_UPDATE:
						resInfo = updateGoods(request_body);
						break;
					// 山东需求-内购手机-订购-导出
					case FunctionIdConstant.INSIDE_PURCH_ORDERS_EXPORT:
						resInfo = exportOrders(request_body);
						break;
					// 山东需求-内购手机-订购-列表
					case FunctionIdConstant.INSIDE_PURCH_ORDERS_PAGE:
						resInfo = pageOrders(request_body);
						break;
					default:
						break;
					}
					ResponsePackUtil.cagHttpResponse(channel, resInfo);
					String responseStatus = ResponsePackUtil.getResCode(resInfo);
					if (null != responseStatus && !"".equals(responseStatus)) {
						operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id,
								request_body, "", responseStatus);
					}
					continue;
					// 响应成功
				}
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} catch (Exception e) {
				logger.error("内购商品业务逻辑处理异常", e);
				// 响应客户端异常
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} finally {
				// channel.close();
			}
		}

	}

	/**
	 * 内购商品-新增商品
	 * 
	 * @return
	 */
	public String addGoods(String request_body) {
		logger.debug("内购商品-新增商品,入参:{}", request_body);
		try {
			GoodsInfoVO vo = JSON.parseObject(request_body, GoodsInfoVO.class);
			Response response = insidePruchInterface.insertGoodsInfo(vo);
			logger.debug("内购商品-新增商品 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-新增商品 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-修改商品
	 * 
	 * @return
	 */
	public String updateGoods(String request_body) {
		logger.debug("内购商品-修改商品,入参:{}", request_body);
		try {
			GoodsInfoVO vo = JSON.parseObject(request_body, GoodsInfoVO.class);
			Response response = insidePruchInterface.updateGoodsInfo(vo);
			logger.debug("内购商品-修改商品 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-修改商品 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-删除商品
	 * 
	 * @return
	 */
	public String deleteGoods(String request_body) {
		logger.debug("内购商品-删除商品,入参:{}", request_body);
		try {
			JSONObject requestJson = JSON.parseObject(request_body);
			Response response = insidePruchInterface.deleteGoodsInfo(requestJson.getLong("goodsId"));
			logger.debug("内购商品-删除商品 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-删除商品 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-删除商品前检查是否有订单
	 * 
	 * @return
	 */
	public String checkOrders(String request_body) {
		logger.debug("内购商品-删除商品前检查是否有订单,入参:{}", request_body);
		try {
			JSONObject requestJson = JSON.parseObject(request_body);
			Response response = insidePruchInterface.checkOrderRecord(requestJson.getLong("goodsId"),
					requestJson.getLong("detailId"));
			logger.debug("内购商品-删除商品前检查是否有订单 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-删除商品前检查是否有订单 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-商品详情
	 * 
	 * @return
	 */
	public String detailGoods(String request_body) {
		logger.debug("内购商品-查询商品详情,入参:{}", request_body);
		try {
			JSONObject requestJson = JSON.parseObject(request_body);
			Response response = insidePruchInterface.detailGoodsInfo(requestJson.getLong("goodsId"));
			logger.debug("内购商品-查询商品详情 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-查询商品详情 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-商品列表
	 * 
	 * @return
	 */
	public String pageGoods(String request_body) {
		logger.debug("内购商品-商品列表,入参:{}", request_body);
		try {
			JSONObject requestJson = JSON.parseObject(request_body);
			Response response = insidePruchInterface.pageGoodsInfoForPt(requestJson.getIntValue("page"),
					requestJson.getIntValue("rows"));
			logger.debug("内购商品-商品列表 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-商品列表 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-订购列表
	 * 
	 * @return
	 */
	public String pageOrders(String request_body) {
		logger.debug("内购商品-查询订购列表,入参:{}", request_body);
		try {
			JSONObject requestJson = JSON.parseObject(request_body);
			Response response = insidePruchInterface.pageOrderInfoForPt(requestJson.getIntValue("page"),
					requestJson.getIntValue("rows"));
			logger.debug("内购商品-查询订购列表 调取服务返回结果,response:{}", JSON.toJSONString(response));
			return ResponsePackUtil.buildPack(response);
		} catch (Exception e) {
			logger.error("内购商品-查询订购列表 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 内购商品-订购信息导出
	 * 
	 * @return
	 */
	public String exportOrders(String request_body) {
		logger.debug("内购商品-订购信息导出,入参:{}", request_body);
		try {
			boolean flag = true;
			int page = 1;
			int rows = 200000;
			JSONArray all = new JSONArray();
			while (flag) {
				Response response = insidePruchInterface.pageOrderInfoForPt(page, rows);
				logger.debug("内购商品-订购信息导出 调取服务返回结果,response:{},response_body:{}", JSON.toJSONString(response),response.getResponse_body().toString());
				JSONObject result = JSON.parseObject(response.getResponse_body().toString());
				long nums = result.getLongValue("nums");
				JSONArray array = result.getJSONArray("data");
				if (!array.isEmpty()) {
					all.addAll(array);
				}
				page++;
				if (page > nums) {
					flag = false;
				}
			}
			byte[] b = null;
			String url = "";
			String fastDFSNode = BaseConstant.fastDFSNode;
			String trackerAddr = zkUtil.findData(fastDFSNode);
			if(CollectionUtils.isEmpty(all)){
				return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");
			}
			b = writeExcel(all);
			url = FastDFSUtil.uploadFile(b, "xls");
			return ResponsePackUtil.buildPack("0000", trackerAddr + url);
		} catch (Exception e) {
			logger.error("内购商品-订购信息导出 调取服务异常,request_body:{},e:{}", request_body, e);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
	}

	/**
	 * 生成excel
	 * 
	 * @param list
	 * @param corpId
	 * @param request
	 * @param response
	 * @author Jiangft 2016年5月25日
	 * @throws IOException
	 * @throws WriteException
	 * @throws RowsExceededException
	 * 
	 *             listName 表头集合 listValue Map建集合 list 封装Map集合
	 */
	private byte[] writeExcel(JSONArray array) throws IOException, RowsExceededException, WriteException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		WritableWorkbook wwb = Workbook.createWorkbook(os);
		WritableSheet sheet = wwb.createSheet("sheet1", 0);
		sheet.mergeCells(0, 0, 6, 0);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
		WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
		WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
		titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
		titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
		titleFormate.setWrap(true);
		Label title = new Label(0, 0, "预约信息", titleFormate);
		sheet.setRowView(0, 600, false);// 设置第一行的高度
		sheet.addCell(title);
		CellView cellView = new CellView();
		cellView.setAutosize(true);
		for (int i = 0; i < titleArr.length; i++) {
			sheet.addCell(new Label(i, 1, titleArr[i]));
		}
		for (int i = 0; i < array.size(); i++) {
			OrderRecordInfoVO vo = JSON.parseObject(array.getString(i), OrderRecordInfoVO.class);
			sheet.addCell(new Label(0, i + 2, vo.getOrderTime(),titleFormate));
			sheet.addCell(new Label(1, i + 2, vo.getUserName(),titleFormate));
			sheet.addCell(new Label(2, i + 2, vo.getTelNum(),titleFormate));
			sheet.addCell(new Label(3, i + 2, vo.getJobNum(),titleFormate));
			sheet.addCell(new Label(4, i + 2, vo.getDeptName(),titleFormate));
			sheet.addCell(new Label(5, i + 2, vo.getRecAddress(),titleFormate));
			
			List<OrderRecordVO> recordList = vo.getRecordList();
			StringBuffer str = new StringBuffer();
			for (int j = 0; j < recordList.size(); j++) {
				str.append(recordList.get(j).getName() + recordList.get(j).getTypeName() + " x" + recordList.get(j).getOrderCount());
				if(j!=recordList.size()-1){
					str.append("\r\n");
				}
			}
			sheet.addCell(new Label(6, i + 2, str.toString(),titleFormate));
		}
		wwb.write();
		wwb.close();
		byte[] b = os.toByteArray();
		os.close();
		return b;
	}

	/**
	 * trim
	 * 
	 * @param obj
	 * @return
	 * @author Jiangft 2016年5月19日
	 */
	public static String trim(Object obj) {
		return (obj == null) ? "" : obj.toString().trim();
	}
}
