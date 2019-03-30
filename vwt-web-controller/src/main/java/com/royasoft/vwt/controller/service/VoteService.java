/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
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
import com.royasoft.vwt.controller.util.upload.FileUploadUtil;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.vote.api.interfaces.VoteInterface;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteControlVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteCorpControlVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteOptionsVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteRecordVo;
import com.royasoft.vwt.soa.business.vote.api.vo.VoteSubjectVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
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
public class VoteService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private VoteInterface voteInterface;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DepartMentInterface departMentInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;
    @Autowired
    private ZkUtil zkUtil;

    @Autowired
    private CorpInterface corpInterface;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

                    String user_id = queue_packet.getUser_id(); // 获取用户ID

                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    String tel_number = queue_packet.getTel_number();
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        // 主题列表查询
                        case FunctionIdConstant.VOTESUBJECTQUERY:
                            resInfo = voteSubjectQuery(request_body);
                            break;
                        // 主题保存
                        case FunctionIdConstant.VOTESUBJECTSAVE:
                            resInfo = voteSubjectSave(request_body);
                            break;
                        // 主题删除
                        case FunctionIdConstant.VOTESUBJECTDEL:
                            resInfo = voteSubjectDel(request_body);
                            break;
                        // 单个主题
                        case FunctionIdConstant.VOTESUBJECT:
                            resInfo = voteSubjectView(request_body);
                            break;
                        // 主题发布
                        case FunctionIdConstant.VOTESUBJECTFB:
                            resInfo = voteSubjectViewFB(request_body);
                            break;
                        // 上传文件
                        case FunctionIdConstant.VOTEUPLOAD:
                            resInfo = voteUpload(request_body);
                            break;
                        /*
                         * 选项列表查询 case FunctionIdConstant.VOTEOPTIONSQUERY: resInfo = voteOptionsQuery(request_body); break;
                         */
                        /*
                         * 单个选项查询 case FunctionIdConstant.VOTEOPTIONVIEW: resInfo = voteOptionView(request_body); break;
                         */
                        /*
                         * 选项保存 case FunctionIdConstant.VOTEOPTIONSSAVE: resInfo = voteOptionsSave(request_body); break;
                         */
                        /*
                         * 选项删除 case FunctionIdConstant.VOTEOPTIONSDEL: resInfo = voteOptionsDel(request_body); break;
                         */
                        // 投票结果列表查询
                        case FunctionIdConstant.VOTERSQUERY:
                            resInfo = voteRecordQuery(request_body);
                            break;
                        // 获取人员列表
                        case FunctionIdConstant.VOTETREE:
                            resInfo = voteControllerList(request_body);
                            break;
                        // 获取企业列表（左侧）
                        case FunctionIdConstant.VOTETREECORP:
                            resInfo = voteControlCorpList(request_body);
                            break;
                        // 图文编辑器上传图片
                        case FunctionIdConstant.TWUPLOAD:
                            resInfo = twUpload();
                            break;
                        // 查询人员权限表
                        case FunctionIdConstant.VOTECONTROLLER:
                            resInfo = voteControllerQuery(request_body);
                            break;
                        // 查询企业权限表
                        case FunctionIdConstant.VOTECONTROLCORP:
                            resInfo = voteControlCorpQuery(request_body);
                            break;
                        // 导出投票记录
                        case FunctionIdConstant.VOTERECORDEXPORT:
                            resInfo = voteRecordExport(request_body);
                            break;
                        default:
                            break;
                    }
                    if (FunctionIdConstant.VOTEUPLOAD.equals(function_id)) {// 封面图片上传
                        ResponsePackUtil.cagHttpJspResponse(channel, resInfo);
                        if (!resInfo.isEmpty()) {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");
                        } else {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "-2005");
                        }
                    } else if (FunctionIdConstant.TWUPLOAD.equals(function_id)) {// 图文上传
                        ResponsePackUtil.cagHttpStringResponse(channel, resInfo);
                        if ("1".equals(JSONObject.parseObject(resInfo).getString("error"))) {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "-2005");
                        } else {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", "0000");
                        }
                    } else {
                        ResponsePackUtil.cagHttpResponse(channel, resInfo);
                        String responseStatus = ResponsePackUtil.getResCode(resInfo);
                        if (null != responseStatus && !"".equals(responseStatus)) {
                            operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                        }
                    }

                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("投票处理类异常", e);
            } finally {

            }

        }
    }

    /**
     * 查询投票主题列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    @SuppressWarnings("unchecked")
    public String voteSubjectQuery(String requestBody) {
        logger.debug("获取投票主题列表,requestBody:{}", requestBody);
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = trim(requestJson.getString("sessionid"));
            String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
            JSONObject sessionJson = null;
            String corpid = "", roleId = "", region = "", createId = "";
            int _roleId;
            try {
                sessionJson = JSONObject.parseObject(session);
                roleId = sessionJson.getString("roleId");// 1 系统管理员,2平台管理员,3企业管理员,4省公司管理员,5地市公司管理员,6区县管理员,7客户经理,8省直管理员
                _roleId = Integer.valueOf(roleId);
                // jiangft新增
                corpid = sessionJson.getString("corpId");
                // 如果是区县管理员，userCityArea字段存的是area（区县）的值
                region = sessionJson.getString("userCityArea");

                createId = sessionJson.getString("userId");

                logger.debug("corpid:{},roleId:{},region:{}", corpid, roleId, region);
            } catch (Exception e) {
                logger.error("获取session--------->", session);
                logger.error("获取corpid报错", e);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
            }

            String startTime = requestJson.getString("startTime");
            String endTime = requestJson.getString("endTime");
            String subjectName = requestJson.getString("subjectName");

            String page = trim(requestJson.getString("page"));
            String row = trim(requestJson.getString("row"));
            int pageIndex = 1;
            int pageSize = 10;
            pageIndex = Integer.parseInt(page);
            pageSize = Integer.parseInt(row);
            /** 校验参数 */
            if ("".equals(page) || "".equals(row)) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
            }

            Map<String, Object> conditions = new HashMap<String, Object>();
            // 业务逻辑：只比对投票开始时间的前后时间点
            if (StringUtils.isNotEmpty(startTime)) {
                conditions.put("start_time_starttime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime)) {
                conditions.put("end_time_starttime", endTime);
            }
            if (StringUtils.isNotEmpty(subjectName)) {
                conditions.put("LIKE_subjectname", subjectName);
            }

            conditions.put("EQ_createrId", createId); // 用创建者id（即管理员id，取该管理员创建的投票即可，不需要再根据地市省企业划分）
            conditions.put("EQ_createRole", _roleId);
            /*
             * if (_roleId == 3) { // 企业管理员 conditions.put("EQ_corpid", corpid); } else if (_roleId == 4) { // 省管理员 } else if (_roleId == 5) {// 地市管理员
             * conditions.put("EQ_cityCode", region); } else if (_roleId == 6) {// 区县管理员 conditions.put("EQ_areaCode", region); }
             */
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            sortMap.put("createdtime", false);

            Map<String, Object> m = voteInterface.findVoteSubjectOfPage(pageIndex, pageSize, conditions, sortMap);
            List<VoteSubjectVo> list = null;
            List<Map<String, Object>> maplist = new ArrayList<>();
            int total = 0, pageNum = 0;
            if (null != m) {
                list = (List<VoteSubjectVo>) m.get("content");
                total = Integer.parseInt(m.get("total").toString());
                pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
                if (null == list || list.isEmpty()) {
                    model.put("roleId", roleId);
                    model.put("items", maplist);
                    model.put("total", total);// 数据总数
                    model.put("page", pageIndex);
                    model.put("pageNum", pageNum);
                    return ResponsePackUtil.buildPack("0000", model);
                }
                // 封装map
                maplist = genMap(list);

                total = Integer.parseInt(m.get("total").toString());
                pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
                model.put("roleId", roleId);
                model.put("items", maplist);
                model.put("total", total);// 数据总数
                model.put("page", Integer.valueOf(page));
                model.put("pageNum", pageNum);
            }
        } catch (Exception e) {
            logger.error("获取主题列表异常，e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2022, "");
        }
        return ResponsePackUtil.buildPack("0000", model);
    }

    /**
     * 封装map
     * 
     * @param list
     * @return
     * @author Jiangft 2016年9月13日
     */
    public List<Map<String, Object>> genMap(List<VoteSubjectVo> list) {
        List<Map<String, Object>> maplist = new ArrayList<>();
        for (VoteSubjectVo vo : list) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("subjectid", vo.getSubjectid());
            map1.put("subjectname", vo.getSubjectname());
            map1.put("maxoptions", vo.getMaxoptions());
            map1.put("starttime", vo.getStarttime() == null ? "" : sdf.format(vo.getStarttime()));
            map1.put("endtime", vo.getEndtime() == null ? "" : sdf.format(vo.getEndtime()));
            map1.put("subjectcontent", vo.getSubjectcontent());
            map1.put("coverimg", vo.getCoverimg());
            // map1.put("contentfile", vo.getContentfile());
            map1.put("createdtime", vo.getCreatedtime() == null ? "" : sdf.format(vo.getCreatedtime()));
            map1.put("subjectlimit", vo.getSubjectlimit());
            map1.put("sort", vo.getSort());
            // map1.put("contenttype", vo.getContenttype());
            map1.put("corpid", vo.getCorpid());
            map1.put("subjectopen", vo.getSubjectopen());
            map1.put("isopen", vo.getIsopen());
            map1.put("permission", vo.getPermission());

            // jiangft 修改 20160913
            map1.put("voteType", vo.getVoteType());
            map1.put("createrId", vo.getCreaterId());
            map1.put("createRole", vo.getCreateRole());
            map1.put("openTime", vo.getOpenTime());
            map1.put("cityCode", vo.getCityCode());
            map1.put("areaCode", vo.getAreaCode());
            map1.put("queryType", vo.getQueryType());
            map1.put("optionType", vo.getOptionType());
            map1.put("optionSet", vo.getOptionSet());
            map1.put("ticketNum", vo.getTicketNum());

            maplist.add(map1);
        }
        return maplist;
    }

    /**
     * 投票主题保存
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月19日
     */
    public String voteSubjectSave(String requestBody) {
        logger.debug("投票主题保存,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpid = "", roleId = "", userId = "", region = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            corpid = sessionJson.getString("corpId");
            roleId = sessionJson.getString("roleId");// 1 系统管理员,2平台管理员,3企业管理员,4省公司管理员,5地市公司管理员,6区县管理员,7客户经理,8省直管理员
            userId = sessionJson.getString("userId");
            // 如果是区县管理员，userCityArea字段存的是area（区县）的值
            region = sessionJson.getString("userCityArea");
            logger.debug("corpid:{},roleId:{},userId:{}", corpid, roleId, userId);
        } catch (Exception e) {
            logger.error("获取session--------->", session);
            logger.error("获取corpid报错", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
        }

        String subjectid = trim(requestJson.getString("subjectid"));
        String subjectname = trim(requestJson.getString("subjectname"));
        // String maxoptions = trim(requestJson.getString("maxoptions")); 删除字段
        String starttime = trim(requestJson.getString("starttime"));
        String endtime = trim(requestJson.getString("endtime"));
        String subjectcontent = trim(requestJson.getString("subjectcontent"));
        String coverimg = trim(requestJson.getString("coverimg"));
        // String subjectlimit = trim(requestJson.getString("subjectlimit")); 删除字段
        String isopen = trim(requestJson.getString("isopen"));
        String subjectopen = trim(requestJson.getString("subjectopen"));
        String permission = trim(requestJson.getString("permission"));
        // jiangft 新增20160914
        String voteType = trim(requestJson.getString("voteType"));
        // String openTime = trim(requestJson.getString("openTime"));因开放时间，不是新增修改时改变的，所以不用入库
        // String cityCode = trim(requestJson.getString("cityCode")); 根据登陆角色获取
        // String areaCode = trim(requestJson.getString("areaCode"));根据登陆角色获取
        String queryType = trim(requestJson.getString("queryType"));
        String optionType = trim(requestJson.getString("optionType"));
        String optionSet = trim(requestJson.getString("optionSet"));
        String ticketNum = trim(requestJson.getString("ticketNum"));

        // 投票选项
        String option_Type = trim(requestJson.getString("option_Type"));

        if (starttime.isEmpty() || endtime.isEmpty() || subjectopen.isEmpty() || StringUtils.isEmpty(voteType) || StringUtils.isEmpty(queryType) || StringUtils.isEmpty(optionType)
                || StringUtils.isEmpty(optionSet) || StringUtils.isEmpty(ticketNum)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2019, "");
        }

        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(starttime);
            end = sdf.parse(endtime);
        } catch (ParseException e) {
            logger.error("开始日期结束日期出错", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2019, "");
        }
        long time = new Date().getTime();

        if (end.getTime() < time) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2020, "");
        }
        String newId = "";
        VoteSubjectVo vo = null;
        if (!"".equals(subjectid)) {// 修改
            vo = voteInterface.findVoteSubjectById(subjectid);
        } else {// 新增
            vo = new VoteSubjectVo();
            newId = UUID.randomUUID().toString().replace("-", "");
            vo.setSubjectid(newId);
        }

        vo.setSubjectname(subjectname);
        vo.setStarttime(start);
        vo.setEndtime(end);
        vo.setSubjectcontent(subjectcontent);
        if (!"".equals(coverimg)) {
            vo.setCoverimg(coverimg);
        }
        if ("".equals(subjectid)) {// 新增
            vo.setCreatedtime(new Date());
        }
        vo.setIsopen("".equals(isopen) ? 0 : Integer.valueOf(isopen));
        vo.setSubjectopen("".equals(subjectopen) ? 0 : Integer.valueOf(subjectopen));
        if ("3".equals(roleId)) {
            vo.setCorpid(corpid);
        }
        vo.setPermission("".equals(permission) ? 0 : Integer.valueOf(permission));

        vo.setVoteType("".equals(voteType) ? 0 : Integer.valueOf(voteType));
        vo.setCreaterId(userId);
        vo.setCreateRole(Integer.valueOf(roleId));
        // vo.setOpenTime(openTime); 新增时候不用塞值，修改也是取消发布后才能修改，修改后再发布，所以也不用塞值
        if ("5".equals(roleId)) {
            vo.setCityCode(region);
        }
        if ("6".equals(roleId)) {
            vo.setAreaCode(region);
        }
        vo.setQueryType("".equals(queryType) ? 0 : Integer.valueOf(queryType));
        vo.setOptionType("".equals(optionType) ? 0 : Integer.valueOf(optionType));
        vo.setOptionSet("".equals(optionSet) ? 0 : Integer.valueOf(optionSet));
        vo.setTicketNum("".equals(ticketNum) ? 0 : Integer.valueOf(ticketNum));

        VoteSubjectVo vote = voteInterface.saveVoteSubject(vo);

        if (null == vote) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2003, "");
        }

        // 保存投票权限
        boolean flag = false;
        if ("1".equals(subjectopen)) {// 非公开
            JSONArray voterArray = requestJson.getJSONArray("voterArray");
            if (voterArray != null) {
                if ("3".equals(roleId)) {
                    flag = saveVoteControl(subjectid, corpid, newId, voterArray);
                } else {
                    flag = saveVoteCorpControl(subjectid, newId, voterArray);
                }
            }
            if (!flag) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2013, "");
            }
        }

        // 修改时，删除主题下现有的选项
        if (!"".equals(subjectid)) {// 修改
            flag = delExistingOptions(subjectid);
            if (!flag) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2009, "");
            }
            // 删除完选项后，不用删除投票记录，因为业务分析：开始投票（即有投票记录后，从主题编辑页面进入不允许删除修改选项）
        }
        // 保存投票选项
        JSONArray optionsArray = requestJson.getJSONArray("optionsArray");
        if ("3".equals(roleId)) {// 企业管理员保存企业id
            flag = voteOptionsSave(optionsArray, StringUtils.isEmpty(subjectid) ? newId : subjectid, subjectname, corpid);
        } else {
            flag = voteOptionsSave(optionsArray, StringUtils.isEmpty(subjectid) ? newId : subjectid, subjectname, "");
        }
        if (!flag) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2008, "");
        }

        // 其他选项的保存
        if ("9".equals(option_Type)) {
            if ("3".equals(roleId)) {// 企业管理员保存企业id
                flag = voteOtherOptionSave(StringUtils.isEmpty(subjectid) ? newId : subjectid, subjectname, corpid);
            } else {
                flag = voteOtherOptionSave(StringUtils.isEmpty(subjectid) ? newId : subjectid, subjectname, "");
            }
            if (!flag) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2021, "");
            }
        }

        /*
         * 修改主题更新投票选项的主题名称（20160919 不需要了，现在一个主题统一保存，选项先删后加，因开始有投票记录后，不允许修改，所以可以先删后加） if (!"".equals(subjectid)) { Map<String, Object> conditions1 =
         * new HashMap<String, Object>(); conditions1.put("EQ_subjectid", subjectid); logger.debug("修改对应投票选项的主题名称,subjectid:{}", subjectid); //
         * 修改对应投票选项的主题名称 List<VoteOptionsVo> optionList = voteInterface.findVoteOptionsByCondition(conditions1, null); if (null != optionList &&
         * !optionList.isEmpty()) { for (VoteOptionsVo optionsvo : optionList) { optionsvo.setSubjectname(subjectname); String optionid =
         * optionsvo.getOptionid(); optionsvo = voteInterface.saveVoteOptions(optionsvo); if (null == optionsvo) {
         * logger.debug("修改对应投票选项主题名称出错选项id:{}", optionid); return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2003, ""); } } } }
         */

        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");

    }

    /**
     * 批量 投票选项保存
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年9月19日
     */
    public boolean voteOptionsSave(JSONArray optionsArray, String subjectId, String subjectName, String corpId) {
        logger.debug("投票选项保存,optionsArray:{},subjectId:{},subjectName:{},corpId:{}", optionsArray, subjectId, subjectName, corpId);
        try {
            for (int i = 0; i < optionsArray.size(); i++) {
                JSONObject jso = (JSONObject) optionsArray.get(i);
                String optionName = jso.getString("optionName");
                String optionContent = jso.getString("optionContent");
                String coverImg = jso.getString("coverImg");
                String optionId = UUID.randomUUID().toString().replace("-", "");

                VoteOptionsVo vo = new VoteOptionsVo();
                vo.setOptionid(optionId);
                vo.setOptionname(optionName);

                vo.setSubjectid(subjectId);
                vo.setSubjectname(subjectName);

                vo.setOptioncontent(optionContent);
                vo.setCoverimg(coverImg);

                vo.setCreatedtime(new Date());
                vo.setCorpid(corpId);

                // 选项类型 1-自定义，9-其他
                vo.setOptionType(1);
                vo.setSort(i + 1);

                vo = voteInterface.saveVoteOptions(vo);
                if (null == vo) {
                    logger.error("投票选项保存服务异常,optionId:{}", optionId);
                    return false;
                }

            }
        } catch (Exception e) {
            logger.error("投票选项保存异常,e:{}", e);
            return false;
        }

        return true;
    }

    /**
     * 其他选项保存
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年9月19日
     */
    public boolean voteOtherOptionSave(String subjectId, String subjectName, String corpId) {
        logger.debug("投票选项保存,subjectId:{},subjectName:{},corpId:{}", subjectId, subjectName, corpId);
        try {
            String optionName = "其他";
            String optionId = UUID.randomUUID().toString().replace("-", "");

            VoteOptionsVo vo = new VoteOptionsVo();
            vo.setOptionid(optionId);
            vo.setOptionname(optionName);

            vo.setSubjectid(subjectId);
            vo.setSubjectname(subjectName);

            vo.setCreatedtime(new Date());
            vo.setCorpid(corpId);

            // 选项类型 1-自定义，9-其他
            vo.setOptionType(9);

            // 查询数据库中主题下当前选项数
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("EQ_subjectid", subjectId);
            List<VoteOptionsVo> list = voteInterface.findVoteOptionsByCondition(conditions, null);
            if (list == null) {
                logger.error("查询投票选项服务异常,subjectId:{}", subjectId);
                return false;
            }
            int sort = list.size() + 1;
            vo.setSort(sort);

            vo = voteInterface.saveVoteOptions(vo);
            if (null == vo) {
                logger.error("投票选项保存服务异常,optionId:{}", optionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("投票选项保存异常,e:{}", e);
            return false;
        }

        return true;
    }

    /**
     * 删除主题下现有选项
     * 
     * @param subjectId
     * @return
     * @author Jiangft 2016年9月19日
     */
    public boolean delExistingOptions(String subjectId) {
        logger.debug("删除主题下现有选项，subjectId:{}", subjectId);
        boolean flag = true;
        try {
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("EQ_subjectid", subjectId);
            List<VoteOptionsVo> list = voteInterface.findVoteOptionsByCondition(conditions, null);
            if (CollectionUtils.isEmpty(list)) {
                logger.error("获取现有主题下选项服务返回null");
                return false;
            }
            logger.debug("主题下现有选项list.size:{}", list.size());
            for (VoteOptionsVo vo : list) {
                flag = voteInterface.deleteVoteOptions(vo);
                if (!flag) {
                    logger.error("删除选项失败，optionId:{}", vo.getOptionid());
                    return false;
                }
            }

        } catch (Exception e) {
            logger.error("删除主题下现有选项异常e:{}", e);
            return false;
        }
        return true;
    }

    /**
     * 保存人员投票权限
     * 
     * @param subjectid
     * @param corpid
     * @param newId
     * @param requestJson
     * @return
     * @author Jiangft 2016年5月27日
     */
    public boolean saveVoteControl(String subjectid, String corpid, String newId, JSONArray voterArray) {
        logger.debug("投票权限保存,subjectid:{},corpid:{},newId:{},voterArray:{}", subjectid, corpid, newId, voterArray);
        // 保存投票权限
        if (!subjectid.isEmpty()) {// 保存状态先删后加
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_subjectid", subjectid);
            if (!corpid.isEmpty()) {
                conditions.put("EQ_corpid", corpid);
            }
            List<VoteControlVo> list = null;
            list = voteInterface.findControlByCondition(conditions, null);
            if (list != null && list.size() > 0) {
                // 先删后新增
                for (VoteControlVo controlVo : list) {
                    boolean flag = voteInterface.deleteVoteControl(controlVo);
                    if (!flag) {
                        return false;
                    }
                }
            } else {
                logger.debug("人员数右侧列表无数据subjectid{}", subjectid);
            }
        }
        if (voterArray.size() > 0) {
            Map<String, String> deptMap = new HashMap<String, String>();
            List<DepartMentVO> deptlist = departMentInterface.findByCorpId(corpid);
            if (deptlist == null || deptlist.isEmpty()) {
                logger.error("当前企业无部门信息corpid{}", corpid);
            } else {
                for (DepartMentVO deptVo : deptlist) {
                    deptMap.put(deptVo.getDeptId(), deptVo.getPartName());
                }
            }

            for (int i = 0; i < voterArray.size(); i++) {
                String voterid = (String) voterArray.get(i);
                ClientUserVO clientVo = clientUserInterface.findById(voterid);
                VoteControlVo voteControlVo = new VoteControlVo();
                voteControlVo.setControlid(UUID.randomUUID().toString().replace("-", ""));
                voteControlVo.setCorpid(corpid);
                voteControlVo.setCreatedtime(new Date());
                voteControlVo.setDeptid(clientVo.getDeptId());
                voteControlVo.setDeptment(deptMap.get(clientVo.getDeptId()));
                voteControlVo.setSubjectid(subjectid.isEmpty() ? newId : subjectid);
                voteControlVo.setTelnum(clientVo.getTelNum());
                voteControlVo.setVoterid(clientVo.getUserId());
                voteControlVo.setVotername(clientVo.getUserName());

                voteControlVo = voteInterface.saveVoteControl(voteControlVo);
                if (voteControlVo == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 保存企业投票权限
     * 
     * @param subjectid
     * @param corpid
     * @param newId
     * @param requestJson
     * @return
     * @author Jiangft 2016年5月27日
     */
    public boolean saveVoteCorpControl(String subjectid, String newId, JSONArray voterArray) {
        logger.debug("企业投票权限保存,subjectid:{},newId:{},voterArray:{}", subjectid, newId, voterArray);
        // 保存企业投票权限
        if (!subjectid.isEmpty()) {// 保存状态先删后加
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("EQ_subjectId", subjectid);
            List<VoteCorpControlVo> list = null;
            list = voteInterface.findCorpControlByCondition(conditions, null);
            if (list != null && list.size() > 0) {
                // 先删后新增
                for (VoteCorpControlVo controlCorpVo : list) {
                    boolean flag = voteInterface.deleteVoteCorpControl(controlCorpVo);
                    if (!flag) {
                        return false;
                    }
                }
            } else {
                logger.error("企业树右侧列表无数据subjectid{}", subjectid);
            }
        }
        if (voterArray.size() > 0) {

            for (int i = 0; i < voterArray.size(); i++) {
                String corpId = (String) voterArray.get(i);
                CorpVO corpVo = corpInterface.findById(corpId);
                if (corpVo == null) {
                    logger.error("根据corpId获取corpVo异常，corpId:{}", corpId);
                    return false;
                }
                String corpName = corpVo.getCorpName();
                VoteCorpControlVo voteCorpControlVo = new VoteCorpControlVo();
                voteCorpControlVo.setControlId(UUID.randomUUID().toString().replace("-", ""));
                voteCorpControlVo.setCorpId(corpId);
                voteCorpControlVo.setCreatedTime(new Date());
                voteCorpControlVo.setCorpName(corpName);
                voteCorpControlVo.setSubjectId(subjectid.isEmpty() ? newId : subjectid);

                voteCorpControlVo = voteInterface.saveVoteCorpControl(voteCorpControlVo);
                if (voteCorpControlVo == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 投票主题删除
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月19日
     */
    public String voteSubjectDel(String requestBody) {
        logger.debug("投票主题删除,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String subjectid = trim(requestJson.getString("subjectid"));

        Boolean flag = voteInterface.deleteVoteSubject(subjectid);
        if (!flag) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2004, "投票主题删除失败");
        }
        // 删除选项
        flag = delExistingOptions(subjectid);
        if (!flag) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2009, "");
        }

        // 删除人员权限
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_subjectid", subjectid);

        List<VoteControlVo> controllist = voteInterface.findControlByCondition(conditions, null);
        if (controllist == null || controllist.isEmpty()) {
            logger.debug("删除主题后删除权限，无主题对应权限");
        } else {
            for (VoteControlVo controlVo : controllist) {
                flag = voteInterface.deleteVoteControl(controlVo);
                if (!flag) {
                    logger.error("删除个人权限失败controlId:{}", controlVo.getControlid());
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2017, "");
                }
            }
        }

        // 删除企业权限
        Map<String, Object> corpConditions = new HashMap<String, Object>();
        corpConditions.put("EQ_subjectId", subjectid);
        List<VoteCorpControlVo> controlCorplist = voteInterface.findCorpControlByCondition(corpConditions, null);
        if (controlCorplist == null || controlCorplist.isEmpty()) {
            logger.debug("删除主题后删除权限，无主题对应企业权限");
        } else {
            for (VoteCorpControlVo controlCorpVo : controlCorplist) {
                flag = voteInterface.deleteVoteCorpControl(controlCorpVo);
                if (!flag) {
                    logger.error("删除企业权限失败controlId:{}", controlCorpVo.getControlId());
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2017, "");
                }
            }
        }

        // 删除投票记录
        List<VoteRecordVo> recordList = voteInterface.findRecordBySubjectId(subjectid);
        for (VoteRecordVo recordVo : recordList) {
            flag = voteInterface.deleteVoteRecord(recordVo);
            if (!flag) {
                logger.error("删除投票记录失败controlId:{}", recordVo.getRecordid());
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2017, "");
            }
        }

        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 获取单个主题
     * 
     * @param requestBody
     * @param userId
     * @author Jiangft 2016年5月20日
     */
    public String voteSubjectView(String requestBody) {
        logger.debug("获取单个主题,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String subjectid = trim(requestJson.getString("subjectid"));
        VoteSubjectVo vo = voteInterface.findVoteSubjectById(subjectid);
        Map<String, Object> map1 = new HashMap<>();
        if (vo == null) {
            logger.error("获取单个主题为空，subjectid:{}", subjectid);
        } else {
            String fastDFSNode = BaseConstant.fastDFSNode;
            String trackerAddr = "";
            try {
                trackerAddr = zkUtil.findData(fastDFSNode);
            } catch (Exception e) {

            }
            map1.put("subjectid", vo.getSubjectid());
            map1.put("subjectname", vo.getSubjectname());
            map1.put("maxoptions", vo.getMaxoptions());
            map1.put("starttime", vo.getStarttime() == null ? "" : sdf.format(vo.getStarttime()));
            map1.put("endtime", vo.getEndtime() == null ? "" : sdf.format(vo.getEndtime()));
            map1.put("subjectcontent", vo.getSubjectcontent());
            map1.put("coverimg", vo.getCoverimg() == null ? "" : trackerAddr + vo.getCoverimg());
            map1.put("createdtime", sdf.format(vo.getCreatedtime()));
            map1.put("subjectlimit", vo.getSubjectlimit());
            map1.put("sort", vo.getSort());
            map1.put("corpid", vo.getCorpid());
            map1.put("subjectopen", vo.getSubjectopen());
            map1.put("isopen", vo.getIsopen());
            map1.put("permission", vo.getPermission());
            map1.put("voteType", vo.getVoteType());
            map1.put("createrId", vo.getCreaterId());
            map1.put("createRole", vo.getCreateRole());
            map1.put("openTime", vo.getOpenTime());
            map1.put("cityCode", vo.getCityCode());
            map1.put("areaCode", vo.getAreaCode());
            map1.put("queryType", vo.getQueryType());
            map1.put("optionType", vo.getOptionType());
            map1.put("optionSet", vo.getOptionSet());
            map1.put("ticketNum", vo.getTicketNum());

            // jiangft新增20160920
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("EQ_subjectid", vo.getSubjectid());
            List<VoteOptionsVo> optionList = voteInterface.findVoteOptionsByCondition(conditions, null);
            if (CollectionUtils.isNotEmpty(optionList)) {
                for (VoteOptionsVo optVo : optionList) {
                    if (StringUtils.isNotEmpty(optVo.getCoverimg())) {
                        optVo.setCoverimg(trackerAddr + optVo.getCoverimg());
                    }
                }
            }
            map1.put("options", optionList);

        }
        return ResponsePackUtil.buildPack("0000", map1);
    }

    /**
     * 上传文件
     * 
     * @param msg
     * @return
     * @author Jiangft 2016年5月19日
     */
    public String voteUpload(String requestBody) {
        logger.debug("上传图片");
        String filePath = "", fileSize = "";
        Map<String, Object> map = new HashMap<>();
        try {
            map = FileUploadUtil.uploadFile1(msg);
            if (map.isEmpty()) {
                return "";
            }
            filePath = map.get("path").toString();
            fileSize = map.get("size").toString();

            if (null == filePath || "".equals(filePath)) {
                return "";
            }
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return "";
        }

        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {

        }
        filePath = filePath.replace("\\", "/");

        String xxx = "<script>window.parent.ipurl='" + (trackerAddr + filePath) + "';window.parent.xdurl='" + filePath + "';window.parent.filesize='" + fileSize + "';</script>";

        return xxx;
    }

    /**
     * 查询投票选项列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月19日
     */
    /*
     * @SuppressWarnings("unchecked") public String voteOptionsQuery(String requestBody) { logger.debug("获取投票选项列表,requestBody:{}", requestBody);
     * JSONObject requestJson = JSONObject.parseObject(requestBody); String sessionid = trim(requestJson.getString("sessionid")); String session =
     * redisInterface.getString(BaseConstant.nameSpace + sessionid); JSONObject sessionJson = null; String corpid = ""; try { sessionJson =
     * JSONObject.parseObject(session); corpid = sessionJson.getString("corpId"); logger.debug("corpid:{}", corpid); } catch (Exception e) {
     * logger.error("获取session--------->", session); logger.error("获取corpid报错", e); return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001,
     * ""); } String subjectid = trim(requestJson.getString("subjectid")); String page = trim(requestJson.getString("page")); String row =
     * trim(requestJson.getString("row")); Map<String, Object> model = new HashMap<String, Object>(); int pageIndex = 1; int pageSize = 10; try {
     * pageIndex = Integer.parseInt(page); } catch (Exception e) { } try { pageSize = Integer.parseInt(row); } catch (Exception e) { } if
     * ("".equals(subjectid) || "".equals(page) || "".equals(row)) { return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2006, ""); }
     * Map<String, Object> conditions = new HashMap<String, Object>(); conditions.put("EQ_corpid", corpid); if (!subjectid.isEmpty()) {
     * conditions.put("EQ_subjectid", subjectid); } conditions.put("LIKE_subjectname", subjectName); conditions.put("EQ_sort", sort); Map<String,
     * Boolean> sortMap = new HashMap<String, Boolean>(); sortMap.put("sort", false); Map<String, Object> m =
     * voteInterface.findVoteOptionsOfPage(pageIndex, pageSize, conditions, sortMap); List<VoteOptionsVo> list = null; int total = 0, pageNum = 0; if
     * (null != m) { list = (List<VoteOptionsVo>) m.get("content"); List<Map<String, Object>> maplist = new ArrayList<>(); total =
     * Integer.parseInt(m.get("total").toString()); pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1); if (null == list
     * || list.isEmpty()) { logger.debug("主题下无选项数据subjectid:", subjectid); model.put("items", maplist); model.put("total", total);// 数据总数
     * model.put("page", pageIndex); model.put("pageNum", pageNum); return ResponsePackUtil.buildPack("0000", model); } for (VoteOptionsVo vo : list)
     * { Map<String, Object> map1 = new HashMap<>(); map1.put("optionid", vo.getOptionid()); map1.put("optionname", vo.getOptionname());
     * map1.put("subjectid", vo.getSubjectid()); map1.put("subjectname", vo.getSubjectname()); map1.put("optioncontent", vo.getOptioncontent());
     * map1.put("coverimg", vo.getCoverimg()); // map1.put("contentfile", vo.getContentfile()); map1.put("createdtime",
     * sdf.format(vo.getCreatedtime())); map1.put("sort", vo.getSort()); // map1.put("contenttype", vo.getContenttype()); map1.put("corpid",
     * vo.getCorpid()); maplist.add(map1); } total = Integer.parseInt(m.get("total").toString()); model.put("items", maplist); model.put("total",
     * total);// 数据总数 model.put("page", Integer.valueOf(page)); pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
     * model.put("pageNum", pageNum); } // String resBody = JSONObject.toJSONString(model); return ResponsePackUtil.buildPack("0000", model); }
     */

    /**
     * 投票选项删除
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月19日
     */
    // public String voteOptionsDel(String requestBody) {
    // logger.debug("投票选项删除,requestBody:{}", requestBody);
    // JSONObject requestJson = JSONObject.parseObject(requestBody);
    // String optionid = trim(requestJson.getString("optionid"));
    //
    // boolean flag = voteInterface.deleteVoteOptions(optionid);
    // if (!flag) {
    // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2009, "");
    // }
    //
    // // 删除选项相关的投票记录
    // flag = voteRecordDel(optionid);
    //
    // if (!flag) {
    // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2009, "");
    // }
    //
    // return ResponsePackUtil.buildPack("0000", "");
    // }

    /**
     * 删除投票记录
     * 
     * @param optionid
     * @return
     * @author Jiangft 2016年5月31日
     */
    public boolean voteRecordDel(String optionid) {
        logger.debug("投票选项删除,optionid:{}", optionid);
        List<VoteRecordVo> volist = voteInterface.findRecordByOptionId(optionid);
        logger.debug("该选项的投票记录size:{}", CollectionUtils.isEmpty(volist) ? 0 : volist.size());
        boolean flag = true;
        if (volist != null && volist.size() > 0) {
            for (VoteRecordVo vo : volist) {
                flag = voteInterface.deleteVoteRecord(vo);
                if (!flag) {
                    return false;
                }
            }
        } else {
            logger.debug("该选项无投票记录,optionid:{}", optionid);
        }
        return flag;
    }

    /**
     * 获取单个选项
     * 
     * @param requestBody
     * @param userId
     * @author Jiangft 2016年5月20日
     */
    /*
     * public String voteOptionView(String requestBody) { logger.debug("获取单个选项,requestBody:{}", requestBody); JSONObject requestJson =
     * JSONObject.parseObject(requestBody); String optionid = trim(requestJson.getString("optionid")); VoteOptionsVo vo =
     * voteInterface.findVoteOptionsById(optionid); Map<String, Object> map1 = new HashMap<>(); if (vo == null) { logger.error("获取单个选项为空，optionid:{}",
     * optionid); } else { String fastDFSNode = BaseConstant.fastDFSNode; String trackerAddr = ""; try { trackerAddr = zkUtil.findData(fastDFSNode); }
     * catch (Exception e) { } map1.put("optionid", vo.getOptionid()); map1.put("optionname", vo.getOptionname()); map1.put("subjectid",
     * vo.getSubjectid()); map1.put("subjectname", vo.getSubjectname()); map1.put("optioncontent", vo.getOptioncontent()); map1.put("coverimg",
     * vo.getCoverimg() == null ? "" : trackerAddr + vo.getCoverimg()); // map1.put("contentfile", "http://" + trackerAddr + vo.getContentfile());
     * map1.put("createdtime", sdf.format(vo.getCreatedtime())); map1.put("sort", vo.getSort()); // map1.put("contenttype", vo.getContenttype());
     * map1.put("corpid", vo.getCorpid()); } return ResponsePackUtil.buildPack("0000", map1); }
     */

    /**
     * 查询投票结果列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月19日
     */
    @SuppressWarnings("unchecked")
    public String voteRecordQuery(String requestBody) {
        logger.debug("获取投票结果列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String subjectid = trim(requestJson.getString("subjectid"));
        String page = trim(requestJson.getString("page"));
        String rows = trim(requestJson.getString("rows"));

        String kssj = trim(requestJson.getString("kssj"));
        String jssj = trim(requestJson.getString("jssj"));

        int pageIndex = 1;
        int pageSize = 10;
        try {
            pageIndex = Integer.parseInt(page);
        } catch (Exception e) {
        }
        try {
            pageSize = Integer.parseInt(rows);
        } catch (Exception e) {
        }
        /** 校验参数 */
        if ("".equals(page) || "".equals(rows) || subjectid.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2012, "");
        }
        Map<String, Object> conditions = new HashMap<String, Object>();

        conditions.put("EQ_subjectid", subjectid);
        if (!kssj.isEmpty())
            conditions.put("start_time_votetime", kssj + " 00:00:00");
        if (!jssj.isEmpty())
            conditions.put("end_time_votetime", jssj + " 23:59:59");
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        sortMap.put("createdtime", false);
        sortMap.put("recordid", false);
        Map<String, Object> m = voteInterface.findVoteRecordOfPage(pageIndex, pageSize, conditions, sortMap);
        List<VoteRecordVo> list = null;
        List<Map<String, Object>> maplist = new ArrayList<>();
        Map<String, Object> model = new HashMap<String, Object>();
        int total = 0, pageNum = 0;
        if (null != m) {
            list = (List<VoteRecordVo>) m.get("content");
            total = Integer.parseInt(m.get("total").toString());
            pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
            if (null == list || list.isEmpty()) {
                model.put("items", maplist);
                model.put("total", total);// 数据总数
                model.put("page", pageIndex);
                model.put("pageNum", pageNum);
                return ResponsePackUtil.buildPack("0000", model);
            }

            // 分页每次条数少，循环里掉服务问题不大
            for (VoteRecordVo vo : list) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("voterid", vo.getVoterid());
                map1.put("subjectid", vo.getSubjectid());
                VoteSubjectVo subjectVo = voteInterface.findVoteSubjectById(vo.getSubjectid());
                map1.put("subjectname", subjectVo.getSubjectname());
                map1.put("optionid", vo.getOptionid());
                VoteOptionsVo optionsVo = voteInterface.findVoteOptionById(vo.getOptionid());
                map1.put("optionname", optionsVo.getOptionname());
                map1.put("votername", vo.getVotername());
                map1.put("telnum", vo.getTelnum());
                map1.put("deptment", vo.getDeptment());
                map1.put("votetime", sdf.format(vo.getVotetime()));
                map1.put("createdtime", sdf.format(vo.getCreatedtime()));
                map1.put("corpid", vo.getCorpid());
                map1.put("deptid", vo.getDeptid());
                map1.put("recordid", vo.getRecordid());
                maplist.add(map1);
            }
            total = Integer.parseInt(m.get("total").toString());
            model.put("items", maplist);
            model.put("total", total);// 数据总数
            model.put("page", Integer.valueOf(page));
            pageNum = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
            model.put("pageNum", pageNum);
        }

        return ResponsePackUtil.buildPack("0000", model);
    }

    /**
     * 获取人员左侧列表
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年9月20日
     */

    public String voteControllerList(String requestBody) {
        logger.debug("获取人员左侧列表,requestBody:{}", requestBody);
        Map<String, Object> model = new HashMap<>();
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String sessionid = requestJson.getString("sessionid");
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpId = "", page = "", rows = "", condition = "";
        try {
            try {
                sessionJson = JSONObject.parseObject(session);
                corpId = sessionJson.getString("corpId");
                logger.debug("corpId:{}", corpId);
            } catch (Exception e) {
                logger.error("获取session--------->", session);
                logger.error("获取corpid报错", e);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
            }
            condition = requestJson.getString("condition");
            page = requestJson.getString("page");
            rows = requestJson.getString("rows");

            model = voteInterface.findClientUsersOfPage(Integer.valueOf(page), Integer.valueOf(rows), condition, corpId);
        } catch (Exception e) {
            logger.error("获取人员列表异常e:{}", e);
        }
        return ResponsePackUtil.buildPack("0000", model);

    }

    /**
     * 企业权限左侧列表
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年9月20日
     */

    @SuppressWarnings("unchecked")
    public String voteControlCorpList(String requestBody) {
        logger.debug("企业权限左侧列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String sessionid = requestJson.getString("sessionid");
            String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
            JSONObject sessionJson = null;
            String roleId = "", region = "";
            String page = requestJson.getString("page");
            String rows = requestJson.getString("rows");
            Map<String, Object> model = new HashMap<>();
            try {
                sessionJson = JSONObject.parseObject(session);
                roleId = sessionJson.getString("roleId");// 1 系统管理员,2平台管理员,3企业管理员,4省公司管理员,5地市公司管理员,6区县管理员,7客户经理,8省直管理员
                // 如果是区县管理员，userCityArea字段存的是area（区县）的值
                region = sessionJson.getString("userCityArea");
            } catch (Exception e) {
                logger.error("获取session--------->", session);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
            }

            String corpName = trim(requestJson.getString("corpName"));
            Map<String, Object> conditions = new HashMap<String, Object>();
            if ("5".equals(roleId)) {
                conditions.put("LIKE_corpRegion", region);
            }
            if ("6".equals(roleId)) {
                conditions.put("LIKE_corpArea", region);
            }
            if (StringUtils.isNotEmpty(corpName)) {
                conditions.put("LIKE_corpName", corpName);
            }
            logger.debug("企业权限左侧列表,conditions:{}", conditions);
            Map<String, Object> m = new HashMap<>();
            List<CorpVO> list = new ArrayList<>();
            int total = 0, pageNum = 0;
            m = corpInterface.findAllByPage(StringUtils.isEmpty(page) ? 1 : Integer.valueOf(page), StringUtils.isEmpty(rows) ? 10 : Integer.valueOf(rows), conditions, null);
            if (null != m) {
                list = (List<CorpVO>) m.get("content");
                total = Integer.parseInt(m.get("total").toString());
                pageNum = (total % Integer.valueOf(rows) == 0) ? (total / Integer.valueOf(rows)) : (total / Integer.valueOf(rows) + 1);
                if (null == list || list.isEmpty()) {
                    model.put("items", list);
                    model.put("total", total);// 数据总数
                    model.put("page", Integer.valueOf(page));
                    model.put("pageNum", pageNum);
                    return ResponsePackUtil.buildPack("0000", model);
                }

                total = Integer.parseInt(m.get("total").toString());
                pageNum = (total % Integer.valueOf(rows) == 0) ? (total / Integer.valueOf(rows)) : (total / Integer.valueOf(rows) + 1);
                model.put("roleId", roleId);
                model.put("items", list);
                model.put("total", total);// 数据总数
                model.put("page", Integer.valueOf(page));
                model.put("pageNum", pageNum);
            }

            return ResponsePackUtil.buildPack("0000", model);
        } catch (Exception e) {
            logger.error("企业权限左侧列表异常e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }
    }

    /**
     * 主题发布
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月23日
     */
    public String voteSubjectViewFB(String requestBody) {
        logger.debug("投票主题发布,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);
            String subjectid = trim(requestJson.getString("subjectid"));
            String isopen = trim(requestJson.getString("isopen"));

            VoteSubjectVo vo = voteInterface.findVoteSubjectById(subjectid);
            if (vo == null) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2010, "");
            }
            vo.setIsopen("".equals(isopen) ? 0 : Integer.valueOf(isopen));
            if ("1".equals(isopen)) {
                vo.setOpenTime(new Date());
            }
            voteInterface.saveVoteSubject(vo);
        } catch (Exception e) {
            logger.error("投票主题发布异常e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2010, "");
        }

        return ResponsePackUtil.buildPack("0000", "");
    }

    /**
     * 人员树右侧展示列表
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月24日
     */
    public String voteControllerQuery(String requestBody) {
        logger.debug("人员树右侧展示列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String sessionid = trim(requestJson.getString("sessionid"));
        String session = redisInterface.getString(BaseConstant.nameSpace + sessionid);
        JSONObject sessionJson = null;
        String corpid = "";
        try {
            sessionJson = JSONObject.parseObject(session);
            corpid = sessionJson.getString("corpId");
            logger.debug("corpid:{}", corpid);
        } catch (Exception e) {
            logger.error("获取session--------->", session);
            logger.error("获取corpid报错", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2001, "");
        }

        String subjectid = trim(requestJson.getString("subjectid"));
        Map<String, Object> conditions = new HashMap<String, Object>();
        if (!subjectid.isEmpty()) {
            conditions.put("EQ_subjectid", subjectid);
        }
        if (!corpid.isEmpty())
            conditions.put("EQ_corpid", corpid);
        List<VoteControlVo> list = null;
        list = voteInterface.findControlByCondition(conditions, null);
        if (list == null || list.isEmpty()) {
            logger.debug("人员数右侧列表无数据");
            list = new ArrayList<>();
        }

        return ResponsePackUtil.buildPack("0000", list);

    }

    /**
     * 企业权限右侧展示列表
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月24日
     */
    public String voteControlCorpQuery(String requestBody) {
        logger.debug("企业权限右侧展示列表,requestBody:{}", requestBody);
        try {
            JSONObject requestJson = JSONObject.parseObject(requestBody);

            String subjectid = trim(requestJson.getString("subjectid"));
            Map<String, Object> conditions = new HashMap<String, Object>();
            if (!subjectid.isEmpty()) {
                conditions.put("EQ_subjectId", subjectid);
            }
            List<VoteCorpControlVo> list = null;
            list = voteInterface.findCorpControlByCondition(conditions, null);
            if (list == null || list.isEmpty()) {
                logger.debug("企业权限右侧展示列表无数据");
                list = new ArrayList<>();
            }
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("企业权限右侧展示列表,e:{}", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }

    }

    /**
     * 投票结果导出
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年5月25日
     */
    @SuppressWarnings("unchecked")
    public String voteRecordExport(String requestBody) {

        logger.debug("导出获取投票结果列表,requestBody:{}", requestBody);
        Date ksDate = new Date();
        logger.debug("ksDate-------->{}", ksDate.getTime());

        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String subjectid = trim(requestJson.getString("subjectid"));
        if (StringUtils.isEmpty(subjectid)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        String kssj = trim(requestJson.getString("kssj"));
        String jssj = trim(requestJson.getString("jssj"));
        Map<String, Object> conditions = new HashMap<String, Object>();
        if (!subjectid.isEmpty())
            conditions.put("EQ_subjectid", subjectid);
        if (kssj != null)
            conditions.put("start_time_votetime", kssj);
        if (jssj != null)
            conditions.put("end_time_votetime", jssj);

        int pageIndex = 1;
        int pageSize = 10000;
        Map<String, Object> mCount = voteInterface.findVoteRecordOfPage(pageIndex, 1, conditions, null);
        int total = Integer.parseInt(mCount.get("total").toString());
        List<VoteRecordVo> _list = new ArrayList<VoteRecordVo>();
        List<VoteRecordVo> list = null;
        int xx = total % pageSize == 0 ? (total / pageSize) : (total / pageSize + 1);
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        sortMap.put("createdtime", false);
        sortMap.put("recordid", false);
        for (int i = 1; i <= xx; i++) { // 循环调用分页获取total / pageSize + 1次，一次pageSize条
            Map<String, Object> m = voteInterface.findVoteRecordOfPage(i, pageSize, conditions, sortMap);
            if (m != null) {
                list = (List<VoteRecordVo>) m.get("content");
                _list.addAll(list);
            }
        }
        VoteSubjectVo subjectVo = voteInterface.findVoteSubjectById(subjectid);
        String subjectName = subjectVo.getSubjectname();

        Map<String, Object> optConditions = new HashMap<String, Object>();
        optConditions.put("EQ_subjectid", subjectid);

        List<VoteOptionsVo> optionsList = voteInterface.findVoteOptionsByCondition(optConditions, null);
        Map<String, String> optsMap = new HashMap<>();
        if (optionsList != null) {
            for (VoteOptionsVo optVo : optionsList) {
                String optName = optVo.getOptionname();
                String optId = optVo.getOptionid();
                optsMap.put(optId, optName);
            }
        }

        if (null == _list || _list.isEmpty()) {
            _list = new ArrayList<>();
        }
        logger.debug("遍历_list前-------->{}", new Date().getTime());

        for (VoteRecordVo vo : _list) {
            String optid = vo.getOptionid();
            vo.setSubjectname(subjectName);
            vo.setOptionname(optsMap.get(optid));
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
            b = writeExcel(_list);
            url = FastDFSUtil.uploadFile(b, "xls");
        } catch (Exception e) {
            logger.error("导出excel报错", e);
            ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
        }

        Date jsDate = new Date();
        logger.debug("投票导出耗时，{}", jsDate.getTime() - ksDate.getTime());

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
     */
    public byte[] writeExcel(List<VoteRecordVo> list) throws IOException, RowsExceededException, WriteException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("sheet1", 0);
        sheet.mergeCells(0, 0, 5, 0);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
        WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
        WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
        titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
        titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
        Label title = new Label(0, 0, "投票结果", titleFormate);
        sheet.setRowView(0, 600, false);// 设置第一行的高度
        sheet.addCell(title);

        sheet.addCell(new Label(0, 1, "编号"));
        sheet.addCell(new Label(1, 1, "评选主题"));
        sheet.addCell(new Label(2, 1, "评选对象"));
        sheet.addCell(new Label(3, 1, "投票人手机号码"));
        sheet.addCell(new Label(4, 1, "投票时间"));
        sheet.addCell(new Label(5, 1, "用户输入内容"));

        for (int i = 0; i < list.size(); i++) {
            sheet.addCell(new Label(0, i + 2, i + 1 + ""));
            sheet.addCell(new Label(1, i + 2, trim(list.get(i).getSubjectname())));
            sheet.addCell(new Label(2, i + 2, trim(list.get(i).getOptionname())));
            sheet.addCell(new Label(3, i + 2, trim(list.get(i).getTelnum())));
            sheet.addCell(new Label(4, i + 2, (list.get(i).getVotetime() == null) ? "" : trim(sdf.format(list.get(i).getVotetime()))));
            sheet.addCell(new Label(5, i + 2, trim(list.get(i).getOtherText())));
        }

        wwb.write();
        wwb.close();
        byte[] b = os.toByteArray();
        os.close();
        return b;

    }

    /**
     * 图文编辑器上传图片
     * 
     * @return
     * @author Jiangft 2016年5月26日
     */
    public String twUpload() {
        logger.debug("图文编辑器上传图片");
        String filePath = "";
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            filePath = FileUploadUtil.uploadFile(msg);
            if (null == filePath || "".equals(filePath)) {
                model.put("error", 1);
                model.put("url", filePath);
                return JSONObject.toJSONString(model);
            }
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            model.put("error", 1);
            model.put("url", filePath);
            return JSONObject.toJSONString(model);
        }

        String fastDFSNode = BaseConstant.fastDFSNode;
        String trackerAddr = "";
        try {
            trackerAddr = zkUtil.findData(fastDFSNode);
        } catch (Exception e) {
        }

        model.put("error", 0);
        model.put("url", trackerAddr + filePath);

        return JSONObject.toJSONString(model);
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
