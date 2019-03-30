/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.business.vote.api.interfaces.VoteInterface;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteControlVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteCorpControlVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteOptionsVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteRecordVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteSubjectVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;

/**
 * 投票业务模块
 *
 * @Author:wuyf
 * @Since:2016年5月20日
 */
@Scope("prototype")
@Service
public class VoteService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    private String userIdTmp = null;

    private String telNumTmp = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private VoteInterface voteInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private DepartMentInterface departMentInterface;


    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.vote_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = ""; // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("投票业务模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.GETVOTESUBJECT:
                            resInfo = getVoteSubject(request_body, user_id);
                            break;
                        case FunctionIdConstant.SUBMITVOTE:
                            resInfo = submitote(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETVOTEOPTIONS:
                            resInfo = getVoteOptions(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETVOTERESULT:
                            resInfo = getVoteResult(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETVOTEOPTIONSDETAILL:
                            resInfo = getVoteVptionsDetaill(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETVOTESUBJECTCOUNT:
                            resInfo = getVoteSubjectCount(request_body, user_id);
                            break;
                        case FunctionIdConstant.GETOTHERVOTEOPTIONSDETAILL:
                            resInfo = getOtherVoteVptionsDetaill(request_body, user_id);
                            break;
                        default:
                            break;
                    }
                    logger.debug("投票业务模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        if (StringUtils.isEmpty(user_id) && StringUtils.isEmpty(tel_number)) {
                            operationLogService.saveOperationLogNew(channel, request, userIdTmp, telNumTmp, function_id, request_body, "", responseStatus);
                        } else {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                        }
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Throwable e) {
                logger.error("投票业务模块异常", e);
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }

        }
    }

    /**
     * 客户端获取投票主题
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getVoteSubject(String requestBody, String userId) {
        logger.debug("获取投票主题,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String memid = requestJson.getString("memid");
        String subjectId = requestJson.getString("subjectId");// 主题id
        String pageSize = requestJson.getString("row");
        logger.debug("获取投票主题(解析body),memid:{},subjectId:{}", memid, subjectId);
        /** 校验参数 */
        if (null == memid || "".equals(memid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1085, "");
        userIdTmp = memid;
        // 查询非公开权限
        String corpId = "";
        int rows = 10;
        try {
            rows = Integer.parseInt(pageSize);
        } catch (Exception e) {
            rows = 10;
        }
        try {
            ClientUserVO cv = clientUserInterface.findById(memid);
            if (null == cv)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1092, "");
            corpId = cv.getCorpId();
        } catch (Exception e) {
            logger.error("获取激活用户信息异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1093, "");
        }
        List<String> subjectIds=new ArrayList<String>();
        try {
            List<VoteControlVo> readVoList = voteInterface.findVoteControlVoListByMemid(memid);
            if (CollectionUtils.isNotEmpty(readVoList)) {
                for (VoteControlVo vo : readVoList) {
                    subjectIds.add(vo.getSubjectid());
                }
            }
            List<VoteCorpControlVo> readVoList1 = voteInterface.findVoteControlVoListByCorpId(corpId);
            if (CollectionUtils.isNotEmpty(readVoList1)) {
                for (VoteCorpControlVo vo : readVoList1) {
                    subjectIds.add(vo.getSubjectId());
                }
            }
        } catch (Exception e) {
            logger.error("获取用户投票权限异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }

        try {
            String trackerAddr = getFastDFS();
            List<Map<String, Object>> vsvList = null;
            if ("0".equals(subjectId)) {
                vsvList = voteInterface.findVoteSubjectFastByCondition(memid, corpId, subjectIds, rows);
            } else {
                vsvList = voteInterface.findVoteSubjectNextByCondition(subjectId, memid, corpId, subjectIds, rows);
            }
            if (null == vsvList)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
            List<Map<String, Object>> voteSubjectList = new ArrayList<Map<String, Object>>();
            Map<String, Object> map1 = null;
            if (vsvList.size() > 0) {
                for (int i = 0; i < vsvList.size(); i++) {
                    // 如果该主题是上一次最后的主题，去掉
                    if (subjectId.equals(vsvList.get(i).get("subjectid").toString())) {
                        continue;
                    }
                    map1 = new HashMap<String, Object>();
                    map1.put("subjectid", vsvList.get(i).get("subjectid"));
                    map1.put("subjectname", vsvList.get(i).get("subjectname"));
                    if (null==vsvList.get(i).get("coverimg")||StringUtils.isEmpty(vsvList.get(i).get("coverimg").toString())) {
                        map1.put("coverimg", null);
                    } else {
                        map1.put("coverimg", trackerAddr + vsvList.get(i).get("coverimg").toString());
                    }
                    map1.put("optionType", vsvList.get(i).get("optionType"));
                    Date starttime = (Date) vsvList.get(i).get("starttime");
                    Date endtime = (Date) vsvList.get(i).get("endtime");
                    Date nowdate = new Date();
                    if (starttime.getTime() > nowdate.getTime()) {
                        map1.put("canvote", 1);
                    } else if (nowdate.getTime() > endtime.getTime()) {
                        map1.put("canvote", 3);
                    } else {
                        map1.put("canvote", 2);
                    }
                    voteSubjectList.add(map1);
                }
            }

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("voteSubjectList", voteSubjectList);
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取投票主题异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }

    /**
     * 客户端提交投票信息
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String submitote(String requestBody, String userId) {
        logger.debug("客户端提交投票信息,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String memid = requestJson.getString("memid");
        String subjectId = requestJson.getString("subjectId");
        JSONArray list = requestJson.getJSONArray("list");
        String otherText = requestJson.getString("otherText");
        logger.debug("客户端提交投票信息(解析body),memid:{},subjectId:{},list:{}", memid, subjectId, list);
        /** 校验参数 */
        if (null == memid || "".equals(memid) || null == subjectId || "".equals(subjectId) || null == list || list.size() <= 0) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1087, "");
        }

        userIdTmp = memid;
        try {
            ClientUserVO cv = clientUserInterface.findById(memid);
            if (null == cv)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1092, "");
            // 获取投票记录
            List<VoteRecordVo> recordlist = voteInterface.findBySubjectidAndVoterid(subjectId, memid);
            // 主题
            VoteSubjectVo vsv = voteInterface.findVoteSubjectById(subjectId);
            Date endtime = vsv.getEndtime();
            Date nowdate = new Date();
            if (nowdate.getTime() > endtime.getTime()) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1095, "");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (recordlist.size() > 0) {
                if (1 == vsv.getVoteType()) {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1094, "");
                } else {
                    Date now = new Date();
                    String today = sdf.format(now);
                    for (VoteRecordVo vv : recordlist) {
                        if (today.equals(sdf.format(vv.getVotetime()))) {
                            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1094, "");
                        }
                    }
                }
            }
            String votername = cv.getUserName();
            String telnum = cv.getTelNum();
            String corpId = cv.getCorpId();
            String deptId = cv.getDeptId();
            DepartMentVO dv = departMentInterface.findById(deptId);
            if (null == dv)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1088, "");
            String deptment = dv.getPartName();
            Date votetime = new Date();

            for (Object optionid : list) {
                VoteOptionsVo vo = voteInterface.findVoteOptionById(optionid.toString());
                if (null == vo)
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
                VoteRecordVo vv = new VoteRecordVo();
                vv.setRecordid(UUID.randomUUID().toString().trim().replaceAll("-", ""));
                vv.setVoterid(memid);
                vv.setSubjectid(vo.getSubjectid());
                vv.setSubjectname(vo.getSubjectname());
                vv.setOptionid(optionid.toString());
                vv.setOptionname(vo.getOptionname());
                vv.setVotername(votername);
                vv.setTelnum(telnum);
                vv.setDeptid(deptId);
                vv.setDeptment(deptment);
                vv.setCorpid(corpId);
                vv.setVotetime(votetime);
                vv.setCreatedtime(new Date());
                if (9 == vo.getOptionType()) {
                    vv.setOtherText(otherText);
                }
                voteInterface.saveVoteRecord(vv);
            }

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("success", "success");
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("提交投票信息异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }


    /**
     * 客户端获取投票选项
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getVoteOptions(String requestBody, String userId) {
        logger.debug("获取投票主题选项,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String memid = requestJson.getString("memid");
        String subjectId = requestJson.getString("subjectId");
        logger.debug("获取投票主题选项(解析body),memid:{},subjectId:{}", memid, subjectId);
        /** 校验参数 */
        if (null == memid || "".equals(memid) || null == subjectId || "".equals(subjectId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1089, "");

        userIdTmp = memid;
        try {
            // 主题
            VoteSubjectVo vsv = voteInterface.findVoteSubjectById(subjectId);
            // 获取投票记录
            List<VoteRecordVo> recordlist = voteInterface.findBySubjectidAndVoterid(subjectId, memid);
            // 获取
            List<VoteOptionsVo> voteOptList = null;
            Map<String, Object> conditions2 = new HashMap<String, Object>();
            Map<String, Boolean> sortMap2 = new HashMap<String, Boolean>();
            conditions2.put("EQ_subjectid", subjectId);
            sortMap2.put("sort", true);
            voteOptList = voteInterface.findVoteOptionsByCondition(conditions2, sortMap2);
            if (null == voteOptList)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");

            String trackerAddr = getFastDFS();
            List<Map<String, Object>> voteOptMap = new ArrayList<Map<String, Object>>();
            Map<String, Object> map1 = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (voteOptList.size() > 0) {
                for (VoteOptionsVo vv : voteOptList) {
                    map1 = new HashMap<String, Object>();
                    map1.put("optionid", vv.getOptionid());
                    map1.put("optionname", vv.getOptionname());
                    map1.put("optioncontent", vv.getOptioncontent());
                    if (null == vv.getCoverimg() || "".equals(vv.getCoverimg())) {
                        map1.put("coverimg", null);
                    } else {
                        map1.put("coverimg", trackerAddr + vv.getCoverimg());
                    }
                    map1.put("optionType", vv.getOptionType());
                    voteOptMap.add(map1);
                }
            }
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("voteOptList", voteOptMap);
            if (StringUtils.isEmpty(vsv.getCoverimg())) {
                model.put("coverimg", null);
            } else {
                model.put("coverimg", trackerAddr + vsv.getCoverimg());
            }
            model.put("subjectcontent", vsv.getSubjectcontent());
            model.put("subjectid", vsv.getSubjectid());
            model.put("subjectname", vsv.getSubjectname());
            model.put("openTime", sdf.format(vsv.getOpenTime()));
            model.put("queryType", vsv.getQueryType());
            model.put("optionType", vsv.getOptionType());
            model.put("optionSet", vsv.getOptionSet());
            model.put("ticketNum", vsv.getTicketNum());
            Date starttime = vsv.getStarttime();
            Date endtime = vsv.getEndtime();
            Date nowdate = new Date();
            if (starttime.getTime() > nowdate.getTime()) {
                model.put("canvote", false);
                model.put("reason", "投票活动尚未开始,敬请期待!");
            } else if (nowdate.getTime() > endtime.getTime()) {
                model.put("canvote", false);
                model.put("reason", "投票活动已结束,感谢您的参与!");
            } else {
                model.put("canvote", true);
                model.put("reason", "");
                if (recordlist.size() > 0) {
                    if (1 == vsv.getVoteType()) {
                        model.put("canvote", false);
                        model.put("reason", "你已参加过投票");
                    } else {
                        String today = sdf.format(nowdate);
                        for (VoteRecordVo vv : recordlist) {
                            if (today.equals(sdf.format(vv.getVotetime()))) {
                                model.put("canvote", false);
                                model.put("reason", "你今天已参加过投票,请明天再来!");
                                break;
                            }
                        }
                    }
                }
            }
            List<Object[]> list = voteInterface.getVoteResult(subjectId);
            int totle = 0;
            // 先计算 总数 数据[0-optionname 1-count 2-optionid]
            for (Object[] objs : list) {
                int count = Integer.valueOf(objs[1].toString());
                totle = totle + count;
            }
            model.put("subjectCount", totle);
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取投票主题选项异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }

    /**
     * 客户端获取投票选项结果
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getVoteResult(String requestBody, String userId) {
        logger.debug("获取投票选项结果,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String memid = requestJson.getString("memid");
        String subjectId = requestJson.getString("subjectId");
        logger.debug("获取投票选项结果(解析body),memid:{},subjectId:{}", memid, subjectId);
        /** 校验参数 */
        if (null == memid || "".equals(memid) || null == subjectId || "".equals(subjectId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1089, "");

        userIdTmp = memid;
        try {
            List<Object[]> list = voteInterface.getVoteResult(subjectId);

            List<Map<String, Object>> voteOptMap = new ArrayList<Map<String, Object>>();

            Map<String, String> map = new HashMap<String, String>();

            int totle = 0;
            // 先计算 总数 数据[0-optionname 1-count 2-optionid]
            for (Object[] objs : list) {
                int count = Integer.valueOf(objs[1].toString());
                totle = totle + count;
                // key值optionid value optionCount
                map.put(objs[0].toString(), objs[1].toString());
            }

            // 获取
            List<VoteOptionsVo> voteOptList = null;
            Map<String, Object> conditions2 = new HashMap<String, Object>();
            Map<String, Boolean> sortMap2 = new HashMap<String, Boolean>();

            conditions2.put("EQ_subjectid", subjectId);
            sortMap2.put("sort", false);
            voteOptList = voteInterface.findVoteOptionsByCondition(conditions2, sortMap2);
            if (null == voteOptList)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");

            for (VoteOptionsVo vv : voteOptList) {
                Map<String, Object> tempMap = new HashMap<String, Object>();
                tempMap.put("optionid", vv.getOptionid());
                int count = 0;
                if (!StringUtils.isEmpty(map.get(vv.getOptionid()))) {
                    count = Integer.valueOf(map.get(vv.getOptionid()));
                }
                tempMap.put("optionCount", count);
                tempMap.put("optionname", vv.getOptionname());
                String result = "0";
                if(0!=totle){
                    // 计算投票百分比
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    // 设置精确到小数点后2位
                    numberFormat.setMaximumFractionDigits(2);
                    result = numberFormat.format((float) Long.valueOf(count) / (float) totle * 100);
                }
                tempMap.put("optionPercent", result);
                voteOptMap.add(tempMap);

            }

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("voteOptList", voteOptMap);
            model.put("subjectCount", totle);

            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取主题投票结果异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }

    /**
     * 获取单个选项信息
     * 
     * @param requestBody
     * @param userId
     * @author wuyf 2016年5月26日
     */
    public String getVoteVptionsDetaill(String requestBody, String userId) {
        logger.debug("获取单个选项信息,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String optionid = requestJson.getString("optionid");
        logger.debug("获取单个选项信息(解析body),optionid:{}", optionid);
        /** 校验参数 */
        if (null == optionid || "".equals(optionid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1091, "");
        try {
            VoteOptionsVo vo = voteInterface.findVoteOptionsById(optionid);
            Map<String, Object> map1 = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (vo == null) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
            } else {
                String trackerAddr = getFastDFS();
                map1.put("optionid", vo.getOptionid());
                map1.put("optionname", vo.getOptionname());
                map1.put("subjectid", vo.getSubjectid());
                map1.put("subjectname", vo.getSubjectname());
                map1.put("optioncontent", vo.getOptioncontent());
                if (null == vo.getCoverimg() || "".equals(vo.getCoverimg())) {
                    map1.put("coverimg", null);
                } else {
                    map1.put("coverimg", trackerAddr + vo.getCoverimg());
                }
                if (null == vo.getContentfile() || "".equals(vo.getContentfile())) {
                    map1.put("contentfile", null);
                } else {
                    map1.put("contentfile", trackerAddr + vo.getContentfile());
                }
                map1.put("createdtime", sdf.format(vo.getCreatedtime()));
                map1.put("sort", vo.getSort());
                map1.put("contenttype", vo.getContenttype());
                map1.put("corpid", vo.getCorpid());
            }
            return ResponsePackUtil.buildPack("0000", map1);
        } catch (Exception e) {
            logger.error("获取单个选项信息异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }

    /**
     * 获取文件服务器地址
     */
    private String getFastDFS() {
        return ParamConfig.file_server_url;
    }

    /**
     * 获取主题总票数
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getVoteSubjectCount(String requestBody, String userId) {
        logger.debug("获取主题总票数,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String subjectId = requestJson.getString("subjectId");
        logger.debug("获取主题总票数(解析body),subjectId:{}", subjectId);
        /** 校验参数 */
        if (StringUtils.isEmpty(subjectId))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1089, "");

        try {
            List<Object[]> list = voteInterface.getVoteResult(subjectId);
            int totle = 0;
            // 先计算 总数 数据[0-optionname 1-count 2-optionid]
            for (Object[] objs : list) {
                int count = Integer.valueOf(objs[1].toString());
                totle = totle + count;
            }
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("subjectCount", totle);
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取主题总票数异常,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }

    /**
     * 获取其他选项输入信息
     * 
     * @param requestBody
     * @param userId
     * @return
     */
    public String getOtherVoteVptionsDetaill(String requestBody, String userId) {
        logger.debug("获取其他选项输入信息,requestBody:{},userId:{}", requestBody, userId);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String memid = requestJson.getString("memid");
        String optionId = requestJson.getString("optionId");
        logger.debug("获取其他选项输入信息(解析body),memid:{},optionId:{}", memid, optionId);
        /** 校验参数 */
        if (StringUtils.isEmpty(optionId) || StringUtils.isEmpty(memid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1089, "");

        userIdTmp = memid;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("EQ_voterid", memid);
            condition.put("EQ_optionid", optionId);
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("votetime", false);
            Map<String, Object> map = voteInterface.findVoteRecordOfPage(1, 1000, condition, sortMap);
            List<Map<String, String>> otherTextList = new ArrayList<Map<String, String>>();
            if (null != map) {
                @SuppressWarnings("unchecked")
                List<VoteRecordVo> list = (List<VoteRecordVo>) map.get("content");
                if (CollectionUtils.isNotEmpty(list)) {
                    for (VoteRecordVo vv : list) {
                        Map<String, String> map1 = new HashMap<String, String>();
                        map1.put("otherText", vv.getOtherText());
                        map1.put("voteTime", sdf.format(vv.getVotetime()));
                        otherTextList.add(map1);
                    }
                }
            }
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("otherTextList", otherTextList);
            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("获取其他选项输入信息,requestBody{},userId{},", requestBody, userId, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1086, "");
        }
    }
}
