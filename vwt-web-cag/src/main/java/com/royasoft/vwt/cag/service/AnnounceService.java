/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceInfoInterface;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceAnnexVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceContentVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceInfoVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceVo;
import com.royasoft.vwt.soa.business.square.api.interfaces.GraphicPushInterface;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * 公告业务处理类
 *
 * @Author:MB
 * @Since:2015年8月26日
 */
@Scope("prototype")
@Service
public class AnnounceService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AnnounceService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;
    @Autowired
    private AnnounceInfoInterface announceInfoInterface;
    @Autowired
    private ZkUtil zkUtil;

    @Autowired
    private GraphicPushInterface graphicPushInterface;
    
    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private OperationLogService operationLogService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");

    private final SimpleDateFormat dateFormatDetail = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.announce_queue.take();// 获取队列处理数据
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
                        case FunctionIdConstant.ANNOUNCEDETAIL:
                            res = getAnnounceInfoById(request_body, user_id);
                            break;
                        case FunctionIdConstant.ANNOUNCELIST:
                            res = getAnnounceList(request_body, user_id);
                            break;
                        case FunctionIdConstant.ANNOUNCEWINDOWSLIST:
                        	res = getAnnounceWindowsList(request_body, user_id);
                        	break;
                        case FunctionIdConstant.ANNOUNCERECORD:
                            res = getAnnounceRecordById(request_body, user_id);
                            break;
                        case FunctionIdConstant.GRAPHICRECORD:
                            res = getGraphicRecordById(request_body, tel_number, user_id, user_id);
                            break;
                        case FunctionIdConstant.OASQUARELIST:
                        	res = getOaSquareIdList(request_body,user_id);
                        	break;
                        default:
                            res = ResponsePackUtil.returnFaileInfo(); // 未知请求
                            break;
                    }
                    // 响应成功
                    ResponsePackUtil.responseStatusOK(channel, res);
                    String responseStatus = ResponsePackUtil.getResCode(res);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                }
            } catch (Exception e) {
                logger.error("任务业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.responseStatusFaile(channel, "异常");
            } finally {
                //2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }
        }
    }

    /**
     * 获取公告列表
     * 
     * @param requestBody 请求内容
     * @param userId 用户id
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getAnnounceList(String requestBody, String userId) {
        logger.debug("获取公告列表,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String userName = requestJson.getString("userName");
        String content = requestJson.getString("content");
        String pageIndex = requestJson.getString("pageIndex");
        String pageSize = requestJson.getString("pageSize");

        logger.debug("获取公告列表(解析body),userName:{},content:{},pageIndex:{},pageSize:{}", userName, content, pageIndex, pageSize);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(pageSize);
        valicateList.add(pageIndex);
        valicateList.add(userName);

        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

        Map<String, Object> conditions = new HashMap<String, Object>();

        Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

        /** 若标题不为空则对标题进行模糊查询 */
        if (null != content && !"".equals(content))
            conditions.put("LIKE_announceTitle", content);
        Date date = new Date();
//        conditions.put("start_time_sendTime", dateFormatDetail.format(date));
//        conditions.put("end_time_stopTime", dateFormatDetail.format(date));
        //查询最近两个月已发布的数据
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-2);
//        conditions.put("start_time_stopTime", dateFormatDetail.format(date));
        conditions.put("end_time_sendTime", dateFormatDetail.format(date));
        conditions.put("start_time_sendTime", dateFormatDetail.format(cal.getTime()));
        conditions.put("EQ_status", 1);
        conditions.put("EQ_pushStatus", 1);
        /** 排序 */
        sortMap.put("isTop", false);
        sortMap.put("createTime", false);

        /** 分页查询 */
        Map<String, Object> announceMap = announceInfoInterface.findPageByRecevierAndType(Integer.valueOf(pageIndex), Integer.valueOf(pageSize), conditions, sortMap, userName, 1);

        if (null == announceMap || null == announceMap.get("content") || "".equals(announceMap.get("content")))
            return ResponsePackUtil.buildPack("0000", "");
        /** 获取当前页内容 */
        List<AnnounceInfoVo> announceInfoVos = (List<AnnounceInfoVo>) announceMap.get("content");

        logger.debug("获取公告列表,username:{},当前页数据量:{}", userName, announceInfoVos.size());

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("announcementInfoList", announceInfoVos);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey || "".equals(userKey))
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }
    
    /**
     * 获取公告弹框列表
     * 
     * @param requestBody 请求内容
     * @param userId 用户id
     * @return
     * @Description:
     */
    @SuppressWarnings("unchecked")
    public String getAnnounceWindowsList(String requestBody, String userId) {
        logger.debug("获取公告列表,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
//        String userName = requestJson.getString("userName");
//        String content = requestJson.getString("content");
        String pageIndex = requestJson.getString("pageIndex");
        String pageSize = requestJson.getString("pageSize");

        logger.debug("获取公告列表(解析body)pageIndex:{},pageSize:{}", pageIndex, pageSize);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(pageSize);
        valicateList.add(pageIndex);

        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");

        Map<String, Object> conditions = new HashMap<String, Object>();

        Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

       
        Date date = new Date();
        conditions.put("start_time_stopTime", dateFormatDetail.format(date));
        conditions.put("end_time_sendTime", dateFormatDetail.format(date));
//        conditions.put("start_time_sendTime", dateFormatDetail.format(date));
//        conditions.put("end_time_stopTime", dateFormatDetail.format(date));
        conditions.put("EQ_status", 1);
        conditions.put("EQ_pushStatus", 1);
        /** 排序 */
        sortMap.put("isTop", false);
        sortMap.put("createTime", false);

        /** 分页查询 */
        Map<String, Object> announceMap = announceInfoInterface.findPageByRecevierAndType(Integer.valueOf(pageIndex), Integer.valueOf(pageSize), conditions, sortMap, userId, 1);

        if (null == announceMap || null == announceMap.get("content") || "".equals(announceMap.get("content")))
            return ResponsePackUtil.buildPack("0000", "");
        /** 获取当前页内容 */
        List<AnnounceInfoVo> announceInfoVos = (List<AnnounceInfoVo>) announceMap.get("content");
        List<AnnounceInfoVo> arry = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(announceInfoVos)) {
			for (AnnounceInfoVo announceInfoVo : announceInfoVos) {
				if (arry.size()>4) {
					continue;
				}
				if (StringUtils.isEmpty(redisInterface.getString("ANNOUNCE:READ:"+announceInfoVo.getId()+userId))) {
					//多次弹屏
					if (1==announceInfoVo.getRateTimes()) {
						arry.add(announceInfoVo);
					}
					if (0==announceInfoVo.getRateTimes() && StringUtils.isNotEmpty(redisInterface.getString("ANNOUNCE:"+announceInfoVo.getId()+userId))) {
						arry.add(announceInfoVo);
						redisInterface.del("ANNOUNCE:"+announceInfoVo.getId()+userId);
					}
				}
			}
		}
        logger.debug("获取公告列表,当前页数据量:{}", arry.size());

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("announcementInfoList", arry);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(userId);
        // if (null == userKey || "".equals(userKey))
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 获取公告详情
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getAnnounceInfoById(String requestBody, String userId) {
        logger.debug("获取公告详情,requestBody:{},telNum:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");

        logger.debug("获取公告详情(解析body),id:{}", id);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(id);
        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1007, "");

        /** 公告被阅读数 */
        int recordOfAnnounce = announceInfoInterface.getRecordCountByAnnounceId(Long.valueOf(id), userId);

        /** 查询详情 */
        AnnounceVo announceVo = announceInfoInterface.findAnnounceById(Long.valueOf(id), userId);

        String announceHtml = createHtml(announceVo, recordOfAnnounce);

        logger.debug("获取公告详情(组装html),announceHtml:{}", announceHtml);

        // StringBuffer announceHtml = new StringBuffer();
        // announceHtml.append("<script type='text/javascript'>").append(" window.location.href='" + ParamConfig.nginx_address +
        // "/h5/html/news/index.html?id=" + id + "&userid=" + userId + "'")
        // .append("</script>");
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("announcementInfo", announceHtml);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(telNum);
        // if (null == userKey || "".equals(userKey))
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        //标记已读
        if (StringUtils.isNotEmpty(redisInterface.getString("ANNOUNCE:READ:"+id+userId))) {
        	redisInterface.setString("ANNOUNCE:READ:"+id+userId,userId);
		}
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 获取公告详情阅读次数
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getAnnounceRecordById(String requestBody, String userId) {
        logger.debug("获取公告详情阅读次数,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");

        logger.debug("获取公告详情阅读次数(解析body),id:{}", id);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(id);
        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1009, "");

        /** 公告被阅读数 */
        int recordOfAnnounce = announceInfoInterface.getRecordCountByAnnounceId(Long.valueOf(id), userId);

        logger.debug("获取公告详情阅读次数,recordOfAnnounce:{}", recordOfAnnounce);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("announcementRecord", recordOfAnnounce);
        /** 加密返回body */
        // String userKey = getUserKeyByTelNum(telNum);
        // if (null == userKey || "".equals(userKey))
        // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userId);
        return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 校验获取列表请求参数
     * 
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @return
     * @Description:
     */
    private boolean valicateParams(List<String> strList) {
        for (String string : strList) {
            if (null == string || "".equals(string))
                return false;
        }
        return true;
    }

    /**
     * 生成公告详情html
     * 
     * @param announceVo
     * @return
     * @Description:
     */
    private String createHtml(AnnounceVo announceVo, int recordOfAnnounce) {
        AnnounceInfoVo announceInfoVo = announceVo.getAnnounceInfoVo();
        List<AnnounceAnnexVo> announceAnnexVos = announceVo.getAnnounceAnnexVoList();
        List<AnnounceContentVo> announceContentVos = announceVo.getAnnounceContentVoList();

        String announceTitle = announceInfoVo.getAnnounceTitle();
        String sendPart = announceInfoVo.getPartName();
        String sendPersonName = announceInfoVo.getSendPersonName();
        long sendType = announceInfoVo.getSendType();
        Date sendTime = announceInfoVo.getSendTime();

        if (null == sendPart || "".equals(sendPart))
            sendPart = "无";

        if (null == sendPersonName || "".equals(sendPersonName))
            sendPersonName = "无";

        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"utf-8\" />");
        sb.append("<title>公告详情</title>");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\">");
        sb.append("<link rel=\"stylesheet\" href=\"http://112.4.17.105:10025/h5/stylesheets/public.css?version=0.1.1\">");
        sb.append("<link rel=\"stylesheet\" href=\"http://112.4.17.105:10025/h5/stylesheets/webnews_20160513.css?version=0.1.1\">");
        sb.append("<style>");
        sb.append("html body{margin: 0px auto; padding: 0px auto;}");
        sb.append("header{ margin-top:2px; font-size:16px; margin-left:20px; width:90%;}");
        sb.append("header p{ word-break: break-all;font-family:'Microsoft YaHei',微软雅黑;font-size:20px;color:#000000;}");
        sb.append(".title_left_div{color:#808080;margin-left:20px;margin-top: 4px;width:25%;float:left;font-family:'Microsoft YaHei',微软雅黑;word-break: break-all;font-size:14px;}");
        sb.append(".title_right_div{color:#808080;width:65%;margin-top: 4px;text-align:left;float:left;font-family:'Microsoft YaHei',微软雅黑;word-break: break-all;font-size:14px;}");
        sb.append("section img{ width:100%;}");
        sb.append("section div {width: 90%;margin-left: 20px;margin-top: 10px;text-indent: 0em;word-break: break-all;}");
        sb.append(".annex_left_div{margin:10px 0px 0px 20px;font-family:'Microsoft YaHei',微软雅黑;color:#2980b9;float:left;white-space:nowrap;width:18%;font-size:14px}");
        sb.append(".annex_right_div{margin-top: 10px;width:72%;text-align:left;float:left;font-family:'Microsoft YaHei',微软雅黑;word-break: break-all;font-size:14px}");

        sb.append(".footer {margin:20px 0px 15px 20px;width: 90%;float:left;font-family:'Microsoft YaHei',微软雅黑;color:#666666;}");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class='body-wrap' style='padding-bottom: 10px; height: 100%; overflow: auto; display: block;'>");
        sb.append("<header><p>" + announceTitle + "</p></header>");
        sb.append("<div>");
        if (sendType == 1) {
            sb.append("<div class='title_left_div'>发布部门：</div><div class='title_right_div'>" + sendPart + "</div><br/>" + "<div class='title_left_div'>发布日期：</div><div class='title_right_div'>"
                    + dateFormat.format(sendTime) + "</div>");
        } else if (sendType == 2) {
            sb.append("<div class='title_left_div'>发布部门：</div><div class='title_right_div'>" + sendPart + "</div><br/><div class='title_left_div'>发布人员：</div><div class='title_right_div'>"
                    + sendPersonName + "</div><br/><div class='title_left_div'>发布日期：</div><div class='title_right_div'>" + dateFormat.format(sendTime) + "</div>");
        }

        sb.append("<div style='clear:both;'></div>");
        sb.append("</div>");
        sb.append("<section>");
        // 正文
        String fileAddress = "";
        for (AnnounceContentVo announceContentVo : announceContentVos) {
            if (null != announceContentVo.getAnnouncePic() && !"".equals(announceContentVo.getAnnouncePic())) {
                if (announceContentVo.getAnnouncePic().startsWith("/group")) {
                    fileAddress = ParamConfig.file_server_url;
                } else {
                    fileAddress = ParamConfig.nginx_address;
                }
                sb.append("<img src='" + fileAddress + "/" + announceContentVo.getAnnouncePic() + "' onclick=\"window.location='img" + fileAddress + "/" + announceContentVo.getAnnouncePic() + "'\"/>");
            }

            sb.append("<div>" + announceContentVo.getAnnounceContent() + "</div>");
        }
        sb.append("</section>");
        sb.append("<div style='margin-top:10px'>");
        if (null != announceAnnexVos && announceAnnexVos.size() > 0) {

            // 附件
            for (int i = 0; i < announceAnnexVos.size(); i++) {
                if (announceAnnexVos.get(i).getAnnexUrl().startsWith("/group")) {
                    fileAddress = ParamConfig.file_server_url;
                } else {
                    fileAddress = ParamConfig.nginx_address;
                }
                sb.append("<div class='annex_left_div'>");
                sb.append("附件" + (i + 1) + "：</div><div class='annex_right_div'><a href='" + fileAddress + "/" + announceAnnexVos.get(i).getAnnexUrl() + "'>" + announceAnnexVos.get(i).getAnnexName()
                        + "</a>");
                sb.append("</div></br>");
            }

        }
        sb.append("<div class='footer'><font id='announceRecord' name='announceRecord' style='font-size:14px'>阅读人数 </font></div>");

        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    public String getGraphicRecordById(String requestBody, String telNum, String userId, String userKey) {
        logger.debug("获取图文阅读次数,requestBody:{},telNum:{},userKey:{}", requestBody, userId, userKey);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String serviceId = requestJson.getString("serviceID");
        String newsId = requestJson.getString("newsID");
        String corpId = requestJson.getString("corpID");
        logger.debug("获取图文阅读次数(解析body),serviceId:{},newsId{},userId{}", serviceId, newsId, userId);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(serviceId);
        valicateList.add(newsId);
        valicateList.add(telNum);
        valicateList.add(corpId);
        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1021, "");

        /** 公告被阅读数 */

        int recordOfGraphic = graphicPushInterface.getRecordOfGraphic(serviceId, newsId, userId, corpId);
        if (-1 == recordOfGraphic)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1022, "");

        logger.debug("获取图文阅读次数,recordOfGraphic:{}", recordOfGraphic);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("graphicRecord", recordOfGraphic);
        /** 加密返回body */
        String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userKey);
        return ResponsePackUtil.buildPack("0000", resBody);
    }
    
    
    /**
     * oa推送服务号列表
     * @param requestBody
     * @param telNum
     * @param userId
     * @param userKey
     * @return
     */
    public String getOaSquareIdList(String requestBody,String userKey) {
    	logger.debug("oa推送服务号列表,requestBody:{}", requestBody);
    	JSONObject bodyJson = new JSONObject();
    	//多个服务号逗号拼接
		try {
			String list = zkUtil.findData("/royasoft/vwt/cag/oasquare");
			
	    	bodyJson.put("list", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bodyJson.put("list", "");
		}
    	
    	/** 加密返回body */
    	String resBody = ResponsePackUtil.encryptData(JSONObject.toJSONString(bodyJson), userKey);
    	return ResponsePackUtil.buildPack("0000", resBody);
    }

    /**
     * 获取用户对应的密钥
     * 
     * @param telNum
     * @return
     * @Description:
     */
    // private String getUserKeyByTelNum(String userId) {
    // logger.debug("获取用户对应的密钥,userId:{}", userId);
    // try {
    // ClientUserVO clientUserVO = clientUserInterface.findById(userId);
    // logger.debug("获取用户对应的密钥,userId:{},clientUserVO:{}", userId, clientUserVO);
    // if (null == clientUserVO)
    // return null;
    // logger.debug("获取用户对应的密钥,userId:{},privateKey:{}", userId, clientUserVO.getPrivateKey());
    // return clientUserVO.getPrivateKey();
    // } catch (Exception e) {
    // logger.error("获取用户对应的密钥异常,userId:{}", userId, e);
    // return null;
    // }
    //
    // }
}
