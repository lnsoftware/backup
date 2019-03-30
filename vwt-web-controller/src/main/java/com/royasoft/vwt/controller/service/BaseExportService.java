/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.business.export.api.ExportInterface;

/**
 * 
 * @author ht
 * @since 2016-12-27
 */
@Scope("prototype")
@Service
public class BaseExportService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BaseExportService.class);
	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	@Autowired
	private ExportInterface exportInterface;

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private ZkUtil zkUtil;
//
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void run() {
		while (true) {
			try {
				queue_packet = ServicesQueue.export_queue.take();// 获取队列处理数据
				msg = queue_packet.getMsg();// 获取请求信息
				channel = queue_packet.getChannel();// 获取连接
				if (msg instanceof HttpRequest) {
					HttpRequest request = (HttpRequest) msg;

					String function_id = queue_packet.getFunction_id();

					String user_id = queue_packet.getUser_id(); // 获取用户ID

					String request_body = queue_packet.getRequest_body();// 获取参数实体

					String tel_number = queue_packet.getTel_number();
					/**************************** 业务逻辑处理 *****************************************/
					String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

					switch (function_id) {
					// 导出部门总人数
					case FunctionIdConstant.PARTCOUNT:
						resInfo = exportPartCount(request_body);
						break;
					// 导出部门-各部门已激活的人数
					case FunctionIdConstant.PARTACTIVATIONCOUNT:
						resInfo = exportPartActivationCount(request_body);
						break;
					// 导出激活用户数
					case FunctionIdConstant.ACTIVATIONMEMBER:
						resInfo = exportActivationMember(request_body);
						break;
					// 导出日新增用户量
					case FunctionIdConstant.NEWMEMBER:
						resInfo = exportNewMember(request_body);
						break;
					// 导出日活用户量/日登陆用户总数
					case FunctionIdConstant.ALIVEMEMBER:
						resInfo = exportAliveMember(request_body);
						break;
					// 导出服务号总数
					case FunctionIdConstant.SERVICECOUNT:
						resInfo = exportServiceCount();
						break;
						// 查询个人登录情况
                    case FunctionIdConstant.QUERYLOGONMSG:
                        resInfo = getLogonMsgByTelNum(request_body);
                        break;
                     // 查询工作圈每日新增消息数量
                    case FunctionIdConstant.QUERYWORKCOUNT:
                        resInfo = getWorkMsgCount(request_body);
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
				}
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} catch (Exception e) {
				logger.error("投票处理类异常", e);
			} finally {

			}

		}
	}

	/**
	 * 导出部门总人数
	 * 
	 * @param requestBody
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportPartCount(String requestBody) {

		logger.debug("导出部门总人数结果列表,requestBody:{}", requestBody);

		JSONObject requestJson = JSONObject.parseObject(requestBody);

		String partfullname = trim(requestJson.getString("partfullname"));
		if (StringUtils.isEmpty(partfullname)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}

		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getpartCount(partfullname);
		} catch (Exception e1) {
			logger.debug("导出部门总人数结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		if (list == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "部门总人数";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("部门全称");
			listName.add("数量");
			listValue.add("partfullName");
			listValue.add("count");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

	}

	/**
	 * 导出部门-各部门已激活的人数
	 * 
	 * @param requestBody
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportPartActivationCount(String requestBody) {

		logger.debug("导出部门-各部门已激活的人数结果列表,requestBody:{}", requestBody);

		//JSONObject requestJson = JSONObject.parseObject(requestBody);

		/*String partfullname = trim(requestJson.getString("partfullname"));
		if (StringUtils.isEmpty(partfullname)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}*/

		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getDeptMemMsg();
		} catch (Exception e1) {
			logger.debug("部门-各部门已激活的人数结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "部门-各部门已激活的人数";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("部门全称");
			listName.add("激活人数");
			listName.add("部门人数");
			listValue.add("partfullName");
			listValue.add("countClient");
			listValue.add("countMem");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

	}

	/**
	 * 导出激活用户数
	 * 
	 * @param requestBody
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportActivationMember(String requestBody) {

		logger.debug("导出激活用户结果列表,requestBody:{}", requestBody);
/*
		JSONObject requestJson = JSONObject.parseObject(requestBody);

		String partfullname = trim(requestJson.getString("partfullname"));
		if (StringUtils.isEmpty(partfullname)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}*/

		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getClientUser();
		} catch (Exception e1) {
			logger.debug("导出激活用户结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		if (list == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "激活用户数";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("电话号码");
			listName.add("员工姓名");
			listName.add("部门名称");
			listName.add("职位");
			listName.add("部门全称");
			listValue.add("telnum");
			listValue.add("membername");
			listValue.add("partname");
			listValue.add("duty");
			listValue.add("partfullName");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

	}

	/**
	 * 导出日新增用户量
	 * 
	 * @param requestBody
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportNewMember(String requestBody) {

		logger.debug("导出日新增用户量结果列表,requestBody:{}", requestBody);

		JSONObject requestJson = JSONObject.parseObject(requestBody);

		//String partfullname = trim(requestJson.getString("partfullname"));
		String createTime = trim(requestJson.getString("date"));
		/*if (StringUtils.isEmpty(partfullname) || StringUtils.isEmpty(createTime)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}*/
		if(StringUtils.isEmpty(createTime)){
		    createTime=sdf.format(new Date());
		}
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getClientUserByDate(createTime);
		} catch (Exception e1) {
			logger.debug("导出日新增用户量结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		if (list == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "日新增用户量";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("部门");
			listName.add("数量");
			listValue.add("partfullName");
			listValue.add("count");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

	}

	/**
	 * 导出日活用户量/日登陆用户总数
	 * 
	 * @param requestBody
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportAliveMember(String requestBody) {

		logger.debug("导出日活用户量/日登陆用户总数结果列表,requestBody:{}", requestBody);

		JSONObject requestJson = JSONObject.parseObject(requestBody);

		//String partfullname = trim(requestJson.getString("partfullname"));
		String logonTime = trim(requestJson.getString("date"));
		/*if (StringUtils.isEmpty(partfullname)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
		}*/
		if(StringUtils.isEmpty(logonTime)){
		    logonTime=sdf.format(new Date());
        }

		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getLogonMem(logonTime);
		} catch (Exception e1) {
			logger.debug("导出日活用户量/日登陆用户总数结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		if (list == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "日活用户量/日登陆用户总数";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("用户号码");
			listName.add("员工姓名");
			listName.add("部门名称");
			listName.add("职位");
			listName.add("部门全称");
			listName.add("登录时间");
			listName.add("客户端版本");
			listValue.add("username");
			listValue.add("membername");
			listValue.add("partname");
			listValue.add("duty");
			listValue.add("partfullName");
			listValue.add("logontime");
			listValue.add("clientVersion");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

	}
	
	
	/**
     * 查询个人登录情况
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月25日
     */
    public String getLogonMsgByTelNum(String requestBody) {

        logger.debug("查询个人登录情况总数结果列表,requestBody:{}", requestBody);

        JSONObject requestJson = JSONObject.parseObject(requestBody);

        //String partfullname = trim(requestJson.getString("partfullname"));
        String telNum = trim(requestJson.getString("telNum"));
        if (StringUtils.isEmpty(telNum)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = exportInterface.getLogonMsgByTelNum(telNum);
        } catch (Exception e1) {
            logger.debug("导出日活用户量/日登陆用户总数结果列表出错");
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
        if (list == null) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
        byte[] b = null;
        String url = "";
        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {
            logger.error("获取zk节点异常e:{}", e);
        }
        try {
            String titleName = "日活用户量/日登陆用户总数";
            List<String> listName = new ArrayList<>();
            List<String> listValue = new ArrayList<>();
            listName.add("用户号码");
            listName.add("客户端");
            listName.add("登录时间");
            listName.add("版本号");
            listName.add("手机类型");
            listName.add("登录次数");
            listValue.add("username");
            listValue.add("clienttype");
            listValue.add("logontime");
            listValue.add("clientversion");
            listValue.add("clientmodel");
            listValue.add("count");
            b = writeExcel(titleName, listName, listValue, list);
            url = FastDFSUtil.uploadFile(b, "xls");
        } catch (Exception e) {
            logger.error("导出excel报错", e);
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
        }

        return ResponsePackUtil.buildPack("0000", trackerAddr + url);

    }
	
    /**
     * 查询工作圈每日新增数量
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月25日
     */
    public String getWorkMsgCount(String requestBody) {

        logger.debug("查询个人登录情况总数结果列表,requestBody:{}", requestBody);

        JSONObject requestJson = JSONObject.parseObject(requestBody);

        //String partfullname = trim(requestJson.getString("partfullname"));
        String date = trim(requestJson.getString("date"));
        if (StringUtils.isEmpty(date)) {
            date=sdf.format(new Date());
        }

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = exportInterface.getWorkMsgCount(date);
        } catch (Exception e1) {
            logger.debug("导出日活用户量/日登陆用户总数结果列表出错");
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
        if (list == null) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
        byte[] b = null;
        String url = "";
        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {
            logger.error("获取zk节点异常e:{}", e);
        }
        try {
            String titleName = "日活用户量/日登陆用户总数";
            List<String> listName = new ArrayList<>();
            List<String> listValue = new ArrayList<>();
            listName.add("新增数量");
            listValue.add("count");
            b = writeExcel(titleName, listName, listValue, list);
            url = FastDFSUtil.uploadFile(b, "xls");
        } catch (Exception e) {
            logger.error("导出excel报错", e);
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
        }

        return ResponsePackUtil.buildPack("0000", trackerAddr + url);

    }
    
	/**
	 * 导出服务号总数
	 * 
	 * @return
	 * @author Jiangft 2016年5月25日
	 */
	public String exportServiceCount() {

		logger.debug("导出服务号总数结果列表");

		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = exportInterface.getServiceCount();
		} catch (Exception e1) {
			logger.debug("导出服务号总数结果列表出错");
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		if (list == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
		}
		byte[] b = null;
		String url = "";
		String fastDFSNode = BaseConstant.fastDFSNode;
		String trackerAddr = "";
		try {
			trackerAddr = zkUtil.findData(fastDFSNode);
		} catch (Exception e) {
			logger.error("获取zk节点异常e:{}", e);
		}
		try {
			String titleName = "服务号总数";
			List<String> listName = new ArrayList<>();
			List<String> listValue = new ArrayList<>();
			listName.add("名称");
			listName.add("创建时间");
			listValue.add("name");
			listValue.add("createTime");
			b = writeExcel(titleName, listName, listValue, list);
			url = FastDFSUtil.uploadFile(b, "xls");
		} catch (Exception e) {
			logger.error("导出excel报错", e);
			ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
		}

		return ResponsePackUtil.buildPack("0000", trackerAddr + url);

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
	private byte[] writeExcel(String titleName, List<String> listName, List<String> listValue,
			List<Map<String, Object>> list) throws IOException, RowsExceededException, WriteException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		WritableWorkbook wwb = Workbook.createWorkbook(os);
		WritableSheet sheet = wwb.createSheet("sheet1", 0);
		sheet.mergeCells(0, 0, 5, 0);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
		WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
		WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
		titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
		titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
		Label title = new Label(0, 0, titleName, titleFormate);
		sheet.setRowView(0, 600, false);// 设置第一行的高度
		sheet.addCell(title);
		for (int i = 0; i < listName.size(); i++) {
			sheet.addCell(new Label(i+1, 1, listName.get(i)));
		}

		for (int m = 0; m < list.size(); m++) {
		    sheet.addCell(new Label(0, m + 2, m + 1 + ""));
			for (int j = 0; j < listValue.size(); j++) {
				sheet.addCell(new Label(j+1, m + 2, trim(list.get(m).get(listValue.get(j)))));
			}
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
