/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.common.security.MD5;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.PageUtils;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.util.RocketMqUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.HlwCorpAuthInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.SmsSwitchInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.vo.HlwCorpAuthVO;
import com.royasoft.vwt.soa.business.hlwAuth.api.vo.SmsSwitchVO;
import com.royasoft.vwt.soa.business.industry.api.interfaces.IndustryManagerInterface;
import com.royasoft.vwt.soa.business.industry.api.vo.IndustryManagerVo;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.AccountManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.vo.AccountManegerVo;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.customer.api.vo.CustomerVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

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
public class InternetAuthService implements Runnable {

    @Autowired
    private HlwCorpAuthInterface hlwCorpAuthInterface;

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private ClientUserInterface clientUserInterface;

    @Autowired
    private DepartMentInterface departMentInterface;

    @Autowired
    private HLWMemberInfoInterface hLWMemberInfoInterface;

    @Autowired
    private DatabaseInterface databaseInterface;

    @Autowired
    private AccountManagerInterface accountManagerInterface;

    @Autowired
    private SendProvinceSmsInterface sendProvinceSmsInterface;

    @Autowired
    private SmsSwitchInterface smsSwitchInterface;

    @Autowired
    private ZkUtil zkUtil;

    @Autowired
    private IndustryManagerInterface industryManagerInterface;// 行业管理服务化

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    private final Logger logger = LoggerFactory.getLogger(InternetAuthService.class);

    @Override
    public void run() {
        while (true) {
            try {
                // 获取InternetAuth的队列处理数据
                queue_packet = ServicesQueue.internetAuth_queue.take();
                long t1 = System.currentTimeMillis();
                logger.info("==============开始时间:{}", t1);
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体

                    logger.debug("互联网认证处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String res = "";// 响应结果
                    if (function_id == null || function_id.length() <= 0) {
                        ResponsePackUtil.CalibrationParametersFailure(channel, "任务业务请求参数校验失败！");
                    } else {
                        res = sendTaskBusinessLayer(function_id, user_id, request_body, request);
                    }
                    ResponsePackUtil.responseStatusOK(channel, res); // 响应成功
                    // String responseStatus = ResponsePackUtil.getResCode(res);
                    // if (null != responseStatus && !"".equals(responseStatus))
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("互联网认证业务逻辑处理异常", e);
                // 响应客户端异常
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
                // channel.close();
            }
        }

    }

    /**
     * 任务分模块请求
     * 
     * @param function_id
     * @param user_id
     * @param request_body
     * @param msg
     * @return
     * @author liujm
     */
    private String sendTaskBusinessLayer(String function_id, String user_id, String request_body, Object request) {

        String res = "";
        switch (function_id) {

            case FunctionIdConstant.getInternetAuthList:// 创建任务
                res = getInternetAuthList(request_body);
                break;
            case FunctionIdConstant.getInterAuthInfoFromCity:
                res = getInterAuthInfoFromCity(request_body);
                break;
            case FunctionIdConstant.getInterAuthInfoFromArea:
                res = getInterAuthInfoFromArea(request_body);
                break;
            case FunctionIdConstant.getInterAuthInfoOpen:
                res = getInterAuthInfoOpen(request_body);
                break;
            case FunctionIdConstant.getInterAuthInfoFromCustome:
                res = getInterAuthInfoFromCustome(request_body);
                break;
            case FunctionIdConstant.examineInterAuth:
                res = examineInterAuth(request_body);
                break;
            case FunctionIdConstant.getIsSendMessage:
                res = getIsSendMessage(request_body);
                break;
            case FunctionIdConstant.getInterCustomer:
                res = getInterCustomer(request_body);
                break;
            case FunctionIdConstant.getIndustryMsg:
                res = getIndustryMsg(request_body);
                break;
            case FunctionIdConstant.updateSendMessage:
                res = updateSendMessage(request_body);
                break;
            case FunctionIdConstant.hlwAuthListExport:
                res = hlwAuthListExport(request_body);
                break;
            default:
                res = ResponsePackUtil.returnFaileInfo(); // 未知请求
        }
        return res;
    }

    /**
     * 互联网认证列表查询
     * 
     * @param request_body
     * @param user_id
     * @return
     * @author liujm
     */
    @SuppressWarnings("unchecked")
    public String getInternetAuthList(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        logger.debug("获取互联网认证列表,requestBody:{},userId:{}", request_body);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String roleId = "";
        int pageIndex = 1;
        int pageSize = 10;
        // 地市区域条件查询
        Map<String, Object> condition = new HashMap<String, Object>();
        try {
            if (null != requestJson && !"".equals(requestJson)) {
                String page = requestJson.getString("page");// 前台传递的页面位置请求
                String limit = requestJson.getString("limit");// 前台传递的每页显示数
                String startTime = requestJson.getString("startTime");// 开始时间
                String endTime = requestJson.getString("endTime");// 结束时间
                String telNum = requestJson.getString("linkTel");// 电话号码
                String corpName = requestJson.getString("corpName");// 企业名称
                // 缓存id
                String sessionId = requestJson.getString("sessionid");

                if (null != page && !"".equals(page)) {
                    pageIndex = Integer.parseInt(page);
                }
                if (null != limit && !"".equals(limit)) {
                    pageSize = Integer.parseInt(limit);
                }
                if (null != startTime && !"".equals(startTime)) {
                    condition.put("start_time_registDate", startTime.trim() + " 00:00:00");
                }
                if (null != endTime && !"".equals(endTime)) {
                    condition.put("end_time_registDate", endTime.trim() + " 00:00:00");
                }

                if (null != telNum && !"".equals(telNum)) {
                    condition.put("LIKE_linkTel", telNum.trim());
                }
                if (null != corpName && !"".equals(corpName)) {
                    condition.put("LIKE_corpName", corpName.trim());
                }
                // 从缓存里面获取SessionId
                if (null != sessionId && !"".equals(sessionId)) {
                    String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionId);
                    if (null != josonUserObject && !"".equals(josonUserObject)) {
                        JSONObject js = JSONObject.parseObject(josonUserObject);
                        roleId = js.getString("roleId");
                        // 地市管理员只能看到2未下发、3已下发
                        if (Constants.DISHIADMIN.equals(roleId)) {
                            condition.put("EQ_corpCity", js.getString("userCityArea"));
                            condition.put("GTE_dealFlag", "2");
                            condition.put("LTE_dealFlag", "3");
                            // 区县管理员只能看到3未分配，4已分配，5未开户，7已开户
                        } else if (Constants.QUXIANADMIN.equals(roleId)) {
                            condition.put("EQ_corpArea", js.getString("userCityArea"));
                            condition.put("GTE_dealFlag", "3");
                            condition.put("LTE_dealFlag", "7");
                            // String dealFlags = "3,4,5,6,7";
                            // condition.put("IN_dealFlag", dealFlags);
                            // 客户经理只能看到4已经分配下来,5通过，6不通过
                        } else if (Constants.CUSTOMADMIN.equals(roleId)) {
                            Map<String, Object> conditions = new HashMap<String, Object>();
                            conditions.put("EQ_telNum", js.getString("telNum"));
                            List<CustomerVo> customerList = customerInterface.findCustomerByCondition(conditions, null);
                            CustomerVo customerVo = null;
                            if (null != customerList && customerList.size() > 0) {
                                customerVo = customerList.get(0);
                            }
                            if (null != customerVo) {
                                condition.put("EQ_customerId", customerVo.getId());
                            }
                            condition.put("GTE_dealFlag", "4");
                            condition.put("LTE_dealFlag", "6");
                        } else {
                            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3004, "");
                        }
                    } else {
                        return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3004, "");
                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3004, "");
                }
            }
            condition.put("EQ_deleteFlag", "0");
            sortMap.put("registDate", false);
            int total = 0;
            List<HlwCorpAuthVO> list = null;
            Map<String, Object> m = hlwCorpAuthInterface.findAllByPage(pageIndex, pageSize, condition, sortMap);
            if (null != m) {
                list = (List<HlwCorpAuthVO>) m.get("content");
                total = PageUtils.getPageCount(Integer.parseInt(m.get("total").toString()), pageSize);
                if (total > 0) {
                    // 封装后的数据
                    List<Map<String, Object>> list1 = this.transeferTotable(list, roleId);
                    // model.put("success", true);
                    model.put("items", list1);
                    model.put("pageNum", total);// 数据总数
                    model.put("page", pageIndex);
                } else {
                    // 数据不存在时返回一条无对应数据提示
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
                }
            } else {
                // 数据查询异常返回异常提示
                ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
            }
        } catch (Exception e) {
            logger.error("分页查询互联网认证信息异常", e);
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * 互联网认证列表查询的数据封装
     * 
     * @param list
     * @return
     */
    public List<Map<String, Object>> transeferTotable(List<HlwCorpAuthVO> list, String roleId) {
        List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
        try {
            logger.debug("互联网认证数据封装List:{}", list);
            for (HlwCorpAuthVO cv : list) {
                Map<String, Object> corpMap = new HashMap<String, Object>();

                // 数据封装
                // id
                corpMap.put("authId", cv.getAuthId());
                // 企业名称
                corpMap.put("corpName", cv.getCorpName());
                // 联系人
                corpMap.put("linkName", cv.getLinkName());
                // 手机号码
                corpMap.put("linkTel", cv.getLinkTel());
                // 角色id
                corpMap.put("roleId", roleId);
                // 地市
                DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, cv.getCorpCity());
                if (dictionaryVo1 != null) {
                    corpMap.put("corpCityId", dictionaryVo1.getDictKey());
                    corpMap.put("corpCityName", dictionaryVo1.getDictKeyDesc());
                }
                // 申请时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String str = "";
                if (null != cv.getRegistDate()) {
                    str = sdf.format(cv.getRegistDate());
                }
                corpMap.put("registDate", str);
                // 审批状态
                corpMap.put("dealFlag", cv.getDealFlag());
                tableList.add(corpMap);
            }
        } catch (Exception e) {
            logger.error("反馈问题数据封装异常", e);
        }
        return tableList;
    }

    /**
     * 查询指定区域下的客户经理（分页）
     * 
     * @param request_body
     * @param user_id
     * @return
     * @author liujm
     */
    @SuppressWarnings("unchecked")
    public String getInterCustomer(String request_body) {
        logger.debug("互联网认证获取当前区县的所有客户经理");
        JSONObject requestJson = JSONObject.parseObject(request_body);
        List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
        Map<String, Object> model = new HashMap<String, Object>();
        int pageIndex = 1;
        int pageSize = 10;
        if (null != requestJson) {
            String corpArea = requestJson.getString("corpArea");
            String customerName = requestJson.getString("customerName"); // 名字(支持模糊查询)
            String customerTelnum = requestJson.getString("customerTelnum"); // 手机号码(支持模糊查询)
            String page = requestJson.getString("page");// 前台传递的页面位置请求
            String limit = requestJson.getString("limit");// 前台传递的每页显示数
            Map<String, Object> conditions = new HashMap<String, Object>();
            try {
                logger.debug("查询指定区域下的客户经理（分页）,areaCode:{},customerName:{},customerTelnum:{},page:{},limit:{},", corpArea, customerName, customerTelnum, page, limit);

                if (null != page && !"".equals(page)) {
                    pageIndex = Integer.parseInt(page);
                }
                if (null != limit && !"".equals(limit)) {
                    pageSize = Integer.parseInt(limit);
                }
                if (customerName != null && !"".equals(customerName)) {
                    conditions.put("LIKE_name", customerName.trim());
                }

                if (customerTelnum != null && !"".equals(customerTelnum)) {
                    conditions.put("LIKE_telNum", customerTelnum.trim());
                }

                if (null != corpArea && !"".equals(corpArea)) {
                    conditions.put("EQ_area", corpArea);
                    Map<String, Object> map = customerInterface.findCustomerOfPage(pageIndex, pageSize, conditions, null);

                    if (map != null && !map.isEmpty()) {
                        List<CustomerVo> customerVoList = (List<CustomerVo>) map.get("content");
                        String total = map.get("total").toString();
                        int pagenum = PageUtils.getPageCount(Integer.parseInt(total), pageSize);
                        for (CustomerVo cv : customerVoList) {
                            Map<String, Object> corpCustomerMap = new HashMap<String, Object>();
                            corpCustomerMap.put("customerId", cv.getId());
                            corpCustomerMap.put("customeNamer", cv.getName());
                            corpCustomerMap.put("customerTel", cv.getTelNum());
                            tableList.add(corpCustomerMap);
                        }
                        model.put("items", tableList);
                        model.put("pageNum", pagenum);// 数据总数
                        model.put("page", pageIndex);
                    }
                }
            } catch (Exception e) {
                logger.error("获取区县客户经理失败", e);
            }
        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * 地市查看查看互联网认证信息
     * 
     * @param request_body
     * @param user_id
     * @return
     * @author liujm
     */
    public String getInterAuthInfoFromCity(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        HlwCorpAuthVO hlwCorpAuthVO = null;
        if (null != requestJson) {
            String id = requestJson.getString("id");
            String sessionid = requestJson.getString("sessionid");
            try {
                if (null != id && !"".equals(id)) {
                    hlwCorpAuthVO = hlwCorpAuthInterface.findByAuthId(id);
                    if (hlwCorpAuthVO != null) {
                        // 主键id
                        model.put("authId", hlwCorpAuthVO.getAuthId());
                        // 企业名称
                        model.put("corpName", hlwCorpAuthVO.getCorpName());
                        // 联系人
                        model.put("linkName", hlwCorpAuthVO.getLinkName());
                        // 手机号码
                        model.put("linkTel", hlwCorpAuthVO.getLinkTel());

                        String fastDFSNode = Constants.fastDFSNode;
                        String trackerAddr = "";
                        try {
                            trackerAddr = zkUtil.findData(fastDFSNode);
                            logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
                        } catch (Exception e) {
                            logger.error("获取图片fast地址失败", e);
                        }

                        // 企业认证函
                        model.put("official", trackerAddr + hlwCorpAuthVO.getOfficial());
                        // 企业地址
                        model.put("corpAddress", hlwCorpAuthVO.getCorpAddress());
                        model.put("corpAreaId", hlwCorpAuthVO.getCorpArea());
                        // 企业人数
                        DictionaryVo dictionaryVo3 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYCORPCOUNT, hlwCorpAuthVO.getCorpMemberNumber());
                        if (null != dictionaryVo3) {
                            model.put("corpMemberNumber", dictionaryVo3.getDictValue());
                        }
                        if (null != sessionid && !"".equals(sessionid)) {
                            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                            if (null != josonUserObject && !"".equals(josonUserObject)) {
                                JSONObject js = JSONObject.parseObject(josonUserObject);
                                String roleId = js.getString("roleId");
                                // 角色
                                model.put("roleId", roleId);
                                // 地市编码
                                model.put("corpCityId", hlwCorpAuthVO.getCorpCity());
                                if (null != hlwCorpAuthVO.getCorpCity() && !"".equals(hlwCorpAuthVO.getCorpCity())) {
                                    DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpCity());
                                    if (null != dictionaryVo1) {
                                        // 地市名称
                                        model.put("corpCityName", dictionaryVo1.getDictKeyDesc());
                                    }
                                }

                            }
                        }

                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } catch (Exception e) {
                logger.debug("预览互联网认证信息详情失败", e);
            }
            // 地市编码
            String corpCity = hlwCorpAuthVO.getCorpCity();
            List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
            try {
                if (null != corpCity && !"".equals(corpCity)) {
                    List<DictionaryVo> dictionaryVoList = dictionaryInterface.findDictionaryByDictIdAndDictValue(Constants.DICTIONARYID, corpCity);
                    for (DictionaryVo cv : dictionaryVoList) {
                        Map<String, Object> corpCityMap = new HashMap<String, Object>();
                        corpCityMap.put("areaId", cv.getDictKey());
                        corpCityMap.put("keyDesc", cv.getDictKeyDesc());
                        tableList.add(corpCityMap);
                    }
                    // 当前地市下所有区县
                    model.put("allArea", tableList);
                } else {
                    ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3001, "");
                }
            } catch (Exception e) {
                logger.error("获取地市下面的所有区县异常", e);
            }

        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * 区县管理员查看互联网认证信息
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    public String getInterAuthInfoFromArea(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        HlwCorpAuthVO hlwCorpAuthVO = null;
        if (null != requestJson) {
            String id = requestJson.getString("id");
            String sessionid = requestJson.getString("sessionid");
            try {
                if (null != id && !"".equals(id)) {
                    hlwCorpAuthVO = hlwCorpAuthInterface.findByAuthId(id);
                    if (hlwCorpAuthVO != null) {
                        // 主键id
                        model.put("authId", hlwCorpAuthVO.getAuthId());
                        // 企业名称
                        model.put("corpName", hlwCorpAuthVO.getCorpName());
                        // 联系人
                        model.put("linkName", hlwCorpAuthVO.getLinkName());
                        // 手机号码
                        model.put("linkTel", hlwCorpAuthVO.getLinkTel());
                        String fastDFSNode = Constants.fastDFSNode;
                        String trackerAddr = "";
                        try {
                            trackerAddr = zkUtil.findData(fastDFSNode);
                            logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
                        } catch (Exception e) {
                            logger.error("获取图片fast地址失败", e);
                        }
                        // 企业认证函
                        model.put("official", trackerAddr + hlwCorpAuthVO.getOfficial());
                        // 企业地址
                        model.put("corpAddress", hlwCorpAuthVO.getCorpAddress());
                        // 企业人数
                        DictionaryVo dictionaryVo3 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYCORPCOUNT, hlwCorpAuthVO.getCorpMemberNumber());
                        if (null != dictionaryVo3) {
                            model.put("corpMemberNumber", dictionaryVo3.getDictValue());
                        }

                        if (null != sessionid && !"".equals(sessionid)) {
                            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                            if (null != josonUserObject && !"".equals(josonUserObject)) {
                                JSONObject js = JSONObject.parseObject(josonUserObject);
                                String roleId = js.getString("roleId");
                                // 角色
                                model.put("roleId", roleId);
                                // 地市
                                model.put("corpCityId", hlwCorpAuthVO.getCorpCity());
                                if (null != hlwCorpAuthVO.getCorpCity() && !"".equals(hlwCorpAuthVO.getCorpCity())) {
                                    DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpCity());
                                    if (null != dictionaryVo1) {
                                        // 地市名称
                                        model.put("corpCityName", dictionaryVo1.getDictKeyDesc());
                                    }
                                }
                                // 区县
                                model.put("corpAreaId", hlwCorpAuthVO.getCorpArea());
                                if (null != hlwCorpAuthVO.getCorpArea() && !"".equals(hlwCorpAuthVO.getCorpArea())) {
                                    DictionaryVo dictionaryVo2 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpArea());
                                    if (null != dictionaryVo2) {
                                        // 区县名称
                                        model.put("corpAreaName", dictionaryVo2.getDictKeyDesc());
                                    }
                                }
                            }
                        }
                        CustomerVo customerVo = customerInterface.findCustomerById(hlwCorpAuthVO.getCustomerId());
                        if (null != customerVo) {
                            model.put("customerTelnum", customerVo.getTelNum());
                            model.put("customerName", customerVo.getName());
                        }
                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } catch (Exception e) {
                logger.debug("预览互联网认证信息详情失败", e);
            }

            // String corpArea = hlwCorpAuthVO.getCorpArea();
            // Map<String, Object> conditions = new HashMap<String, Object>();
            // List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
            // try {
            // if (null != corpArea && !"".equals(corpArea)) {
            // conditions.put("EQ_area", corpArea);
            // List<CustomerVo> customerVoList = customerInterface.findCustomerByCondition(conditions, null);
            // for (CustomerVo cv : customerVoList) {
            // Map<String, Object> corpCustomerMap = new HashMap<String, Object>();
            // corpCustomerMap.put("customeId", cv.getId());
            // corpCustomerMap.put("customeName", cv.getName());
            // tableList.add(corpCustomerMap);
            // }
            // model.put("allCustome", tableList);
            // } else {
            // ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3002, "");
            // }
            // } catch (Exception e) {
            // logger.error("获取区县客户经理失败", e);
            // }

        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * 获取所有的行业信息
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    public String getIndustryMsg(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> conditions = new HashMap<String, Object>();
        try {
            conditions.put("EQ_isVaild", 1);
            conditions.put("NE_industryId", 10);
            List<IndustryManagerVo> list = industryManagerInterface.findAllIndustryManager(conditions);
            model.put("data", list);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
        } catch (Exception e) {
            logger.error("获取所有的行业信息异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3003, "");
        }
    }

    /**
     * 客户经理查看互联网认证信息
     * 
     * @param request_body
     * @param user_id
     * @return
     * @author liujm
     */
    public String getInterAuthInfoFromCustome(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        if (null != requestJson) {
            String id = requestJson.getString("id");
            String sessionid = requestJson.getString("sessionid");
            try {
                if (null != id && !"".equals(id)) {
                    HlwCorpAuthVO hlwCorpAuthVO = hlwCorpAuthInterface.findByAuthId(id);
                    if (hlwCorpAuthVO != null) {
                        // 主键id
                        model.put("authId", hlwCorpAuthVO.getAuthId());
                        // 企业名称
                        model.put("corpName", hlwCorpAuthVO.getCorpName());
                        // 联系人
                        model.put("linkName", hlwCorpAuthVO.getLinkName());
                        // 手机号码
                        model.put("linkTel", hlwCorpAuthVO.getLinkTel());
                        // 企业认证函
                        String fastDFSNode = Constants.fastDFSNode;
                        String trackerAddr = "";
                        try {
                            trackerAddr = zkUtil.findData(fastDFSNode);
                            logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
                        } catch (Exception e) {
                            logger.error("获取图片fast地址失败", e);
                        }
                        model.put("official", trackerAddr + hlwCorpAuthVO.getOfficial());
                        // 企业地址
                        model.put("corpAddress", hlwCorpAuthVO.getCorpAddress());
                        // 企业人数
                        DictionaryVo dictionaryVo3 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYCORPCOUNT, hlwCorpAuthVO.getCorpMemberNumber());
                        if (null != dictionaryVo3) {
                            model.put("corpMemberNumber", dictionaryVo3.getDictValue());
                        }

                        if (null != sessionid && !"".equals(sessionid)) {
                            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                            if (null != josonUserObject && !"".equals(josonUserObject)) {
                                JSONObject js = JSONObject.parseObject(josonUserObject);
                                String roleId = js.getString("roleId");
                                // 角色
                                model.put("roleId", roleId);
                                // 地市
                                model.put("corpCityId", hlwCorpAuthVO.getCorpCity());
                                if (null != hlwCorpAuthVO.getCorpCity() && !"".equals(hlwCorpAuthVO.getCorpCity())) {
                                    DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpCity());
                                    if (null != dictionaryVo1) {
                                        // 地市名称
                                        model.put("corpCityName", dictionaryVo1.getDictKeyDesc());
                                    }
                                }
                                // 区县
                                model.put("corpAreaId", hlwCorpAuthVO.getCorpArea());
                                if (null != hlwCorpAuthVO.getCorpArea() && !"".equals(hlwCorpAuthVO.getCorpArea())) {
                                    DictionaryVo dictionaryVo2 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpArea());
                                    if (null != dictionaryVo2) {
                                        // 区县名称
                                        model.put("corpAreaName", dictionaryVo2.getDictKeyDesc());
                                    }
                                }
                            }
                        }
                        // 客户经理
                        if (null != hlwCorpAuthVO.getCustomerId() && !"".equals(hlwCorpAuthVO.getCustomerId())) {
                            CustomerVo customerVo = customerInterface.findCustomerById(hlwCorpAuthVO.getCustomerId());
                            if (null != customerVo) {
                                model.put("customerId", hlwCorpAuthVO.getCustomerId());
                                model.put("customerName", customerVo.getName());
                            }
                        }
                        model.put("dealFlag", hlwCorpAuthVO.getDealFlag());
                        model.put("authComment", hlwCorpAuthVO.getAuthComment());
                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } catch (Exception e) {
                logger.debug("预览互联网认证信息详情失败", e);
            }

        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
    }

    /**
     * 区县管理员开户
     * 
     * @param request_body
     * @param user_id
     * @return
     * @author liujm
     */
    public String getInterAuthInfoOpen(String request_body) {

        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        if (null != requestJson) {
            String id = requestJson.getString("id");
            String sessionid = requestJson.getString("sessionid");
            try {
                if (null != id && !"".equals(id)) {
                    HlwCorpAuthVO hlwCorpAuthVO = hlwCorpAuthInterface.findByAuthId(id);
                    if (hlwCorpAuthVO != null) {
                        // 主键id
                        model.put("authId", hlwCorpAuthVO.getAuthId());
                        // 企业名称
                        model.put("corpName", hlwCorpAuthVO.getCorpName());
                        // 联系人
                        model.put("linkName", hlwCorpAuthVO.getLinkName());
                        // 手机号码
                        model.put("linkTel", hlwCorpAuthVO.getLinkTel());
                        // 企业认证函
                        String fastDFSNode = Constants.fastDFSNode;
                        String trackerAddr = "";
                        try {
                            trackerAddr = zkUtil.findData(fastDFSNode);
                            logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
                        } catch (Exception e) {
                            logger.error("获取图片fast地址失败", e);
                        }
                        model.put("official", trackerAddr + hlwCorpAuthVO.getOfficial());
                        // 企业地址
                        model.put("corpAddress", hlwCorpAuthVO.getCorpAddress());
                        // 企业人数
                        DictionaryVo dictionaryVo3 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYCORPCOUNT, hlwCorpAuthVO.getCorpMemberNumber());
                        if (null != dictionaryVo3) {
                            model.put("corpMemberNumber", dictionaryVo3.getDictValue());
                        }
                        if (null != sessionid && !"".equals(sessionid)) {
                            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                            if (null != josonUserObject && !"".equals(josonUserObject)) {
                                JSONObject js = JSONObject.parseObject(josonUserObject);
                                String roleId = js.getString("roleId");
                                // 角色
                                model.put("roleId", roleId);
                                // 地市
                                model.put("corpCityId", hlwCorpAuthVO.getCorpCity());
                                if (null != hlwCorpAuthVO.getCorpCity() && !"".equals(hlwCorpAuthVO.getCorpCity())) {
                                    DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpCity());
                                    if (null != dictionaryVo1) {
                                        // 地市名称
                                        model.put("corpCityName", dictionaryVo1.getDictKeyDesc());
                                    }
                                }
                                // 区县
                                model.put("corpAreaId", hlwCorpAuthVO.getCorpArea());
                                if (null != hlwCorpAuthVO.getCorpArea() && !"".equals(hlwCorpAuthVO.getCorpArea())) {
                                    DictionaryVo dictionaryVo2 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, hlwCorpAuthVO.getCorpArea());
                                    if (null != dictionaryVo2) {
                                        // 区县名称
                                        model.put("corpAreaName", dictionaryVo2.getDictKeyDesc());
                                    }
                                }
                            }
                        }
                        // 客户经理
                        if (null != hlwCorpAuthVO.getCustomerId() && !"".equals(hlwCorpAuthVO.getCustomerId())) {
                            CustomerVo customerVo = customerInterface.findCustomerById(hlwCorpAuthVO.getCustomerId());
                            if (null != customerVo) {
                                model.put("customerId", hlwCorpAuthVO.getCustomerId());
                                model.put("customerName", customerVo.getName());
                            }
                        }
                        model.put("dealFlag", hlwCorpAuthVO.getDealFlag());
                        model.put("authComment", hlwCorpAuthVO.getAuthComment());
                        AccountManegerVo accountManeger3 = accountManagerInterface.findByCorpid(hlwCorpAuthVO.getCorpId());
                        if (null != accountManeger3) {
                            model.put("loginName", accountManeger3.getAccountlogginname());
                        }

                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("EQ_corpId", hlwCorpAuthVO.getCorpId());
                        List<CorpVO> CorpVOL = corpInterface.findAllByConditions(conditions);
                        if (null != CorpVOL && CorpVOL.size() > 0) {
//                            if (null != CorpVOL.get(0).getCustomerId() && !"".equals(CorpVOL.get(0).getCustomerId())) {
//                                CustomerVo customerVo = customerInterface.findCustomerById(CorpVOL.get(0).getCustomerId());
//                                if (null != customerVo) {
//                                    model.put("customerName", customerVo.getName());
//                                }
//                            }
                            if (null != CorpVOL.get(0).getCorpIndustry() && !"".equals(CorpVOL.get(0).getCorpIndustry())) {
                                IndustryManagerVo industryManagerVo = industryManagerInterface.findByIndustryId(Long.parseLong(CorpVOL.get(0).getCorpIndustry()));
                                if (null != industryManagerVo) {
                                    model.put("industryName", industryManagerVo.getIndustryName());
                                }
                            }
                        }
                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } catch (Exception e) {
                logger.debug("预览互联网认证信息详情失败", e);
            }

        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);

    }

    /**
     * 互联网认证信息分配、审核和开户
     * 
     * @param request
     * @param response
     * @return
     */
    public String examineInterAuth(String request_body) {
        HlwCorpAuthVO hlwCorpAuthVORul = null;
        JSONObject requestJson = JSONObject.parseObject(request_body);
        if (null != requestJson) {
            String id = requestJson.getString("id");
            String sessionid = requestJson.getString("sessionid");
            String roleId = null;
            try {
                if (null != id && !"".equals(id)) {
                    HlwCorpAuthVO hlwCorpAuthVO = hlwCorpAuthInterface.findByAuthId(id);
                    if (null != hlwCorpAuthVO) {
                        if (null != sessionid && !"".equals(sessionid)) {
                            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                            if (null != josonUserObject && !"".equals(josonUserObject)) {
                                JSONObject js = JSONObject.parseObject(josonUserObject);
                                roleId = js.getString("roleId");
                                // 地市
                                if (Constants.DISHIADMIN.equals(roleId)) {
                                    // 分配区县
                                    hlwCorpAuthVO.setCorpArea(requestJson.getString("corpArea"));
                                    hlwCorpAuthVO.setDealFlag("3");
                                    String sendmail = requestJson.getString("sendmail");
                                    if (null != sendmail && !"".equals(sendmail)) {
                                        try {
                                            List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(requestJson.getString("corpArea"));

                                            if ("1".equals(sendmail) && (null == smsSwitchVOList || smsSwitchVOList.size() == 0)) {

                                                if (null != roleId && !"".equals(roleId)) {
                                                    try {
                                                        List<AccountManegerVo> accountManegerVoList = accountManagerInterface.findByRegionidAndRoleid(requestJson.getString("corpArea"),
                                                                Integer.parseInt(Constants.QUXIANADMIN));
                                                        if (null != accountManegerVoList && accountManegerVoList.size() > 0) {
                                                            for (int i = 0; i < accountManegerVoList.size(); i++) {
                                                                AccountManegerVo accountManegerVo = accountManegerVoList.get(i);
                                                                if (accountManegerVo != null) {
                                                                    sendProvinceSmsInterface.sendCommonSms(accountManegerVo.getTelnum(), Constants.sendCityContent);
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("查询区县管理员电话失败", e);
                                                    }

                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error("地市分配区县短信发送短信失败", e);
                                        }
                                    }
                                }
                                // 区县
                                else if (Constants.QUXIANADMIN.equals(roleId)) {
                                    if ("5".equals(hlwCorpAuthVO.getDealFlag())) {
                                        try {
                                            AccountManegerVo accountManeger = accountManagerInterface.findAccountManagerByLoginName(requestJson.getString("loginName"));
                                            if (null != accountManeger) {
                                                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3005, "");
                                            } else {
                                                // 区县开户
                                                hlwCorpAuthVO.setDealFlag("7");
                                                // Map<String, Object> conditions = new HashMap<String, Object>();
                                                // conditions.put("EQ_", hlwCorpAuthVO.getCorpId());
                                                // 企业
                                                CorpVO corp = corpInterface.findById(hlwCorpAuthVO.getCorpId());
                                                if (null != corp) {
                                                    try {
                                                        corp.setFromchannel(1L);
                                                        // 行业
                                                        corp.setCorpIndustry(requestJson.getString("industryid"));
                                                        // 区县
                                                        corp.setCorpArea(hlwCorpAuthVO.getCorpArea());
                                                        // 地市
                                                        corp.setCorpRegion(hlwCorpAuthVO.getCorpCity());
                                                        // 客户经理
                                                        corp.setCustomerId(requestJson.getString("customerId"));
                                                        // 地市名称
                                                        corp.setCorpName(hlwCorpAuthVO.getCorpName());
                                                        // 联系人
                                                        corp.setCorpPersonname(hlwCorpAuthVO.getLinkName());
                                                        // 联系人电话
                                                        corp.setCorpMobilephone(hlwCorpAuthVO.getLinkTel());
                                                        corpInterface.saveCorp(corp);
                                                    } catch (Exception e) {
                                                        logger.error("企业信息修改异常", e);
                                                    }
                                                }
                                                // 企业下所有用户
                                                List<ClientUserVO> ClientUserVOList = clientUserInterface.findByCorpId(hlwCorpAuthVO.getCorpId());
                                                if (null != ClientUserVOList && ClientUserVOList.size() > 0) {
                                                    try {
                                                        for (int i = 0; i < ClientUserVOList.size(); i++) {
                                                            ClientUserVOList.get(i).setFromChannel(1L);
                                                            clientUserInterface.saveUser(ClientUserVOList.get(i));
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("client_user企业下所有员工fromchannal修改异常", e);
                                                    }
                                                }

                                                // 企业下所有部门
                                                List<DepartMentVO> DepartMentVOList = departMentInterface.findByCorpId(hlwCorpAuthVO.getCorpId());
                                                String rootDeptId = null;
                                                String departname = "";
                                                if (null != DepartMentVOList && DepartMentVOList.size() > 0) {
                                                    try {
                                                        for (int i = 0; i < DepartMentVOList.size(); i++) {
                                                            if (DepartMentVOList.get(i).getParentDeptNum().equals("1")) {
                                                                rootDeptId = DepartMentVOList.get(i).getDeptId();
                                                                DepartMentVOList.get(i).setPartName(hlwCorpAuthVO.getCorpName());
                                                            } else {
                                                                if (null != DepartMentVOList.get(i).getPartFullName() && !"".equals(DepartMentVOList.get(i).getPartFullName())) {
                                                                    String[] departNames = DepartMentVOList.get(i).getPartFullName().split("/");
                                                                    departNames[0] = hlwCorpAuthVO.getCorpName();
                                                                    for (int j = 0; j < departNames.length; j++) {
                                                                        departname += departNames[j] + "/";
                                                                    }
                                                                    DepartMentVOList.get(i).setPartFullName(departname.substring(0, departname.length() - 1));
                                                                }
                                                            }
                                                            DepartMentVOList.get(i).setActTime(new Date());
                                                            DepartMentVOList.get(i).setFromChannel("1");
                                                            departMentInterface.save(DepartMentVOList.get(i));
                                                            //变量清空
                                                            departname="";
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("企业下所有所在部门员工fromchannal修改异常", e);
                                                    }
                                                }

                                                // 企业下所有员工迁移表
                                                Map<String, Object> conditions = new HashMap<String, Object>();
                                                conditions.put("EQ_corpId", hlwCorpAuthVO.getCorpId());
                                                List<MemberInfoVO> MemberInfoVOList = hLWMemberInfoInterface.findHLWMemberInfoByCondition(conditions, null);
                                                if (null != MemberInfoVOList && MemberInfoVOList.size() > 0) {
                                                    try {
                                                        for (int i = 0; i < MemberInfoVOList.size(); i++) {

                                                            if (rootDeptId.equals(MemberInfoVOList.get(i).getDeptId())) {
                                                                MemberInfoVOList.get(i).setPartName(hlwCorpAuthVO.getCorpName());
                                                            }
                                                            MemberInfoVOList.get(i).setFromChannel(1L);
                                                            MemberInfoVOList.get(i).setSort(i + 1L);
                                                            // 加入排序字段
                                                            MemberInfoVOList.get(i).setOperationTime(new Date());
                                                            memberInfoInterface.save(MemberInfoVOList.get(i));
                                                        }
                                                        hLWMemberInfoInterface.deleteByCorpId(hlwCorpAuthVO.getCorpId());
                                                    } catch (Exception e) {
                                                        logger.error("企业下所有员工迁移异常", e);
                                                    }
                                                }
                                                // 新建企业的东西
                                                AccountManegerVo accountManegerVo = new AccountManegerVo();
                                                accountManegerVo.setAccountid(Integer.valueOf(databaseInterface.generateId("sys_user", "user_id") + ""));
                                                accountManegerVo.setAccountlogginname(requestJson.getString("loginName"));
                                                accountManegerVo.setAccountusername(hlwCorpAuthVO.getLinkName());
                                                accountManegerVo.setCorpid(hlwCorpAuthVO.getCorpId());
                                                accountManegerVo.setIseffective("Y");
                                                accountManegerVo.setPassword(MD5.encodeMD5(requestJson.getString("password")));
                                                if (hlwCorpAuthVO.getCorpArea() != null && !"".equals(hlwCorpAuthVO.getCorpArea())) {
                                                    accountManegerVo.setRegionid(hlwCorpAuthVO.getCorpArea());
                                                } else {
                                                    accountManegerVo.setRegionid(hlwCorpAuthVO.getCorpCity());
                                                }
                                                accountManegerVo.setRoleid(3);
                                                accountManegerVo.setTelnum(hlwCorpAuthVO.getLinkTel());
                                                logger.debug("新增企业管理员信息,accountManegerVo{}", JSON.toJSON(accountManegerVo));
                                                accountManagerInterface.save(accountManegerVo);
                                                // String sendmail = requestJson.getString("sendmail");
                                                String sendCorpmail = requestJson.getString("sendCorpmail");
                                                String sendcustomermail = requestJson.getString("sendcustomermail");
                                                if (null != sendcustomermail && !"".equals(sendcustomermail)) {
                                                    try {
                                                        //开户给客户经理发送短信
                                                        List<SmsSwitchVO> smsSwitchVOList1 = smsSwitchInterface.findByAttr(requestJson.getString("customerId"));
                                                        if ("1".equals(sendcustomermail)&&(null == smsSwitchVOList1 || smsSwitchVOList1.size() == 0)) {
                                                            try {
                                                                CustomerVo customerVo = customerInterface.findCustomerById(requestJson.getString("customerId"));
                                                                if (null != customerVo) {
                                                                    // 短信内容待定
                                                                    sendProvinceSmsInterface.sendCommonSms(customerVo.getTelNum(), hlwCorpAuthVO.getCorpName() + "已开户");
                                                                }
                                                            } catch (Exception e) {
                                                                logger.error("区县开户发送客户经理短息异常", e);
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("区县开户发送客户经理短息异常", e);
                                                    }
                                                }

                                                if (null != sendCorpmail && !"".equals(sendCorpmail)) {
                                                    if("1".equals(sendCorpmail)){
                                                        try {
                                                            sendProvinceSmsInterface.sendCommonSms(hlwCorpAuthVO.getLinkTel(), Constants.sendOpenContent1 + requestJson.getString("loginName")
                                                            + Constants.sendOpenContent2 + requestJson.getString("password") + Constants.sendOpenContent3);
                                                        } catch (Exception e) {
                                                            logger.error("区县开户发送企业管理员短息异常", e);
                                                        }
                                                    }
                                                }

                                            }

                                        } catch (Exception e) {
                                            logger.error("区县开户异常", e);
                                        }
                                        // 审核认证时间
                                        // hlwCorpAuthVO.setAuthDate(new Date());
                                    } else {
                                        try {
                                            hlwCorpAuthVO.setCustomerId(requestJson.getString("customerId"));
                                            // requestJson.getString("customerTel")只给电话发短信
                                            String sendmail = requestJson.getString("sendmail");
                                            if (null != sendmail && !"".equals(sendmail)) {
                                                try {
                                                    List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(requestJson.getString("customerId"));
                                                    if ("1".equals(sendmail) && (null == smsSwitchVOList || smsSwitchVOList.size() == 0)) {
                                                        CustomerVo customerVo = customerInterface.findCustomerById(requestJson.getString("customerId"));
                                                        if (null != customerVo) {
                                                            sendProvinceSmsInterface.sendCommonSms(customerVo.getTelNum(), Constants.sendCityContent);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    logger.error("区县分配给客户经理发送短信失败", e);
                                                }
                                            }
                                            hlwCorpAuthVO.setDealFlag("4");
                                        } catch (Exception e) {
                                            logger.error("区县分配给客户经理失败", e);
                                        }

                                    }
                                }
                                // 客户经理
                                else if (Constants.CUSTOMADMIN.equals(roleId)) {
                                    hlwCorpAuthVO.setDealFlag(requestJson.getString("dealFlag"));
                                    hlwCorpAuthVO.setAuthComment(requestJson.getString("authComment"));// 审核意见
                                    hlwCorpAuthVO.getCorpArea();
                                    String sendmail = requestJson.getString("sendmail");
                                    if (null != sendmail && !"".equals(sendmail)) {
                                        List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(hlwCorpAuthVO.getCorpArea());
                                        if ("1".equals(sendmail)) {
                                            //不通过给申请人发
                                            if ("6".equals(requestJson.getString("dealFlag"))) {
                                                sendProvinceSmsInterface.sendCommonSms(hlwCorpAuthVO.getLinkTel(), Constants.sendNoPassContent);
                                            } else {//给区县管理员发
                                                if(null == smsSwitchVOList || smsSwitchVOList.size() == 0){
                                                    try {
                                                        List<AccountManegerVo> accountManegerVoList = accountManagerInterface.findByRegionidAndRoleid(hlwCorpAuthVO.getCorpArea(),
                                                                Integer.parseInt(Constants.QUXIANADMIN));
                                                        if (null != accountManegerVoList && accountManegerVoList.size() > 0) {
                                                            for (int i = 0; i < accountManegerVoList.size(); i++) {
                                                                AccountManegerVo accountManegerVo = accountManegerVoList.get(i);
                                                                if (null != accountManegerVo) {
                                                                    sendProvinceSmsInterface.sendCommonSms(accountManegerVo.getTelnum(), Constants.sendCityContent);
                                                                }
                                                            }
                                                        } else {
                                                            logger.debug("区县管理员账户为空");
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("客户经理给区县发送短信异常", e);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // 审核认证时间
                                    hlwCorpAuthVO.setAuthDate(new Date());
                                }
                            }
                        }
                        // 审核认证时间
                        // hlwCorpAuthVO.setAuthDate(new Date());
                    }
                    // 保存数据
                    hlwCorpAuthVORul = hlwCorpAuthInterface.save(hlwCorpAuthVO);

                    /** 推送企业开户提醒至该企业所有注册用户 */
                    sendNoticeToCorpManager(roleId, hlwCorpAuthVO);
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } catch (Exception e) {
                logger.error("进行反馈问题时异常", e);
            }
        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
        if (null != hlwCorpAuthVORul) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
    }

    /**
     * 推送企业开户提醒至该企业所有注册用户
     * 
     * @param roleId
     * @param hlwCorpAuthVO
     */
    private void sendNoticeToCorpManager(String roleId, HlwCorpAuthVO hlwCorpAuthVO) {
        logger.debug("推送企业开户提醒至该企业所有注册用户,roleId:{},hlwCorpAuthVO:{}", roleId, JSON.toJSONString(hlwCorpAuthVO));
        if (!Constants.QUXIANADMIN.equals(roleId) || !"7".equals(hlwCorpAuthVO.getDealFlag()))
            return;

        List<ClientUserVO> clientUserVOs = clientUserInterface.findByCorpId(hlwCorpAuthVO.getCorpId());
        logger.debug("推送企业开户提醒至该企业所有注册用户(获取所有注册用户),clientUserVOs:{}", null == clientUserVOs ? 0 : clientUserVOs.size());
        if (null == clientUserVOs || clientUserVOs.isEmpty())
            return;

        for (ClientUserVO clientUserVO : clientUserVOs) {
            if (null == clientUserVO || null == clientUserVO.getUserId() || "".equals(clientUserVO.getUserId()))
                continue;
            JSONObject imJson = new JSONObject();
            imJson.put("content", "");
            imJson.put("type", "3");
            imJson.put("requestId", UUID.randomUUID().toString());
            imJson.put("roleId", clientUserVO.getUserId());
            imJson.put("needOffLine", false);
            RocketMqUtil.send(RocketMqUtil.BuinessPushQueue, imJson.toJSONString());
        }
    }

    /**
     * 删除互联网认证信息
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    // public String deleteInterAuthById(String request_body, String user_id) {
    //
    // boolean flag = false;
    // JSONObject requestJson = JSONObject.parseObject(request_body);
    // if (null != requestJson) {
    // String id = requestJson.getString("id");
    // try {
    // flag = hlwCorpAuthInterface.deleteByAuthId(id);
    // } catch (Exception e) {
    // logger.error("删除互联网认证信息异常", e);
    // }
    // if (flag) {
    // return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
    // } else {
    // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3003, "");
    // }
    // } else {
    // return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
    // }
    // }

    /**
     * 获取是否发送短信
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    public String getIsSendMessage(String request_body) {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        if (null != requestJson) {
            String sessionid = requestJson.getString("sessionid");
            if (null != sessionid && !"".equals(sessionid)) {
                String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                if (null != josonUserObject && !"".equals(josonUserObject)) {
                    JSONObject js = JSONObject.parseObject(josonUserObject);
                    String roleId = js.getString("roleId");
                    String userCityArea = js.getString("userCityArea");
                    if (null != roleId && !"".equals(roleId)) {
                        if (Constants.CUSTOMADMIN.equals(roleId)) {
                            try {
                                Map<String, Object> conditions = new HashMap<String, Object>();
                                conditions.put("EQ_telNum", js.getString("telNum"));
                                List<CustomerVo> customerList = customerInterface.findCustomerByCondition(conditions, null);
                                CustomerVo customerVo = null;
                                if (null != customerList && customerList.size() > 0) {
                                    customerVo = customerList.get(0);
                                }
                                if (null != customerVo) {
                                    List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(customerVo.getId());
                                    if (null != smsSwitchVOList && smsSwitchVOList.size() > 0) {
                                        // 库里面有值则不发短信
                                        model.put("isSendMessage", "0");

                                    } else {
                                        model.put("isSendMessage", "1");
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("客户经理获取发不发送短信数据异常", e);
                            }
                        } else {
                            try {
                                List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(userCityArea);
                                if (null != smsSwitchVOList && smsSwitchVOList.size() > 0) {
                                    // 库里面有值则不发短信
                                    model.put("isSendMessage", "0");
                                } else {
                                    model.put("isSendMessage", "1");
                                }
                            } catch (Exception e) {
                                logger.error("地市区县获取发不发送短信数据异常", e);
                            }
                        }
                    }
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
                }
            } else {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            }
        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
    }

    /**
     * 修改是否发送短信
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    public String updateSendMessage(String request_body) {
        JSONObject requestJson = JSONObject.parseObject(request_body);
        boolean flag = false;
        if (null != requestJson) {
            String sessionid = requestJson.getString("sessionid");
            String isSend = requestJson.getString("isSend");
            if (null != sessionid && !"".equals(sessionid)) {
                String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionid);
                if (null != josonUserObject && !"".equals(josonUserObject)) {
                    JSONObject js = JSONObject.parseObject(josonUserObject);
                    String roleId = js.getString("roleId");

                    if (null != roleId && !"".equals(roleId)) {
                        // 客户经理是否接受短信
                        if (Constants.CUSTOMADMIN.equals(roleId)) {
                            try {
                                Map<String, Object> conditions = new HashMap<String, Object>();
                                conditions.put("EQ_telNum", js.getString("telNum"));
                                List<CustomerVo> customerList = customerInterface.findCustomerByCondition(conditions, null);
                                CustomerVo customerVo = null;
                                if (null != customerList && customerList.size() > 0) {
                                    customerVo = customerList.get(0);
                                }
                                if (null != customerVo) {

                                    if ("0".equals(isSend)) {
                                        SmsSwitchVO smsSwitchVO = new SmsSwitchVO();
                                        smsSwitchVO.setId(UUID.randomUUID().toString());
                                        smsSwitchVO.setAttr(customerVo.getId());
                                        smsSwitchVO.setType("3");
                                        smsSwitchVO.setInsertDate(new Date());
                                        try {
                                            smsSwitchInterface.save(smsSwitchVO);
                                            flag = true;
                                        } catch (Exception e) {
                                            logger.error("保存是否发送短信失败", e);
                                        }
                                    } else if ("1".equals(isSend)) {
                                        List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(customerVo.getId());
                                        if (null != smsSwitchVOList && smsSwitchVOList.size() > 0) {
                                            try {
                                                for (int i = 0; i < smsSwitchVOList.size(); i++) {
                                                    smsSwitchInterface.deleteById(smsSwitchVOList.get(i).getId());
                                                }
                                            } catch (Exception e) {
                                                logger.error("删除是否发送短信失败", e);
                                            }
                                        }
                                        flag = true;
                                    }

                                }
                            } catch (Exception e) {
                                logger.error("客户经理是否接受短信修改异常", e);
                            }
                        } else {
                            try {
                                // 地市、区县是否接受短信
                                if ("0".equals(isSend)) {
                                    SmsSwitchVO smsSwitchVO = new SmsSwitchVO();
                                    smsSwitchVO.setId(UUID.randomUUID().toString());
                                    smsSwitchVO.setAttr(js.getString("userCityArea"));
                                    if(Constants.DISHIADMIN.equals(roleId)){
                                        smsSwitchVO.setType("1");
                                    }else{
                                        smsSwitchVO.setType("2");
                                    }
                                    smsSwitchVO.setInsertDate(new Date());
                                    try {
                                        smsSwitchInterface.save(smsSwitchVO);
                                        flag = true;
                                    } catch (Exception e) {
                                        logger.error("保存是否发送短信失败", e);
                                    }
                                } else if ("1".equals(isSend)) {
                                    List<SmsSwitchVO> smsSwitchVOList = smsSwitchInterface.findByAttr(js.getString("userCityArea"));
                                    if (null != smsSwitchVOList && smsSwitchVOList.size() > 0) {
                                        try {
                                            for (int i = 0; i < smsSwitchVOList.size(); i++) {
                                                smsSwitchInterface.deleteById(smsSwitchVOList.get(i).getId());
                                            }
                                        } catch (Exception e) {
                                            logger.error("删除是否发送短信失败", e);
                                        }

                                    }
                                    flag = true;
                                }
                            } catch (Exception e) {
                                logger.error("区县是否接受短信修改异常", e);
                            }

                        }
                    }
                }
            }

            if (flag) {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "");
            } else {
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
            }
        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1001, "");
        }
    }

    /**
     * 互联网认证的导出读写方法
     * 
     * @param list
     * @param userId
     * @param request
     * @param response
     */
    @SuppressWarnings("unchecked")
    public String hlwAuthListExport(String request_body) {
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        logger.debug("获取互联网认证列表进行导出,requestBody:{},userId:{}", request_body);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String roleId = "";
        // 地市区域条件查询
        Map<String, Object> condition = new HashMap<String, Object>();

        if (null != requestJson && !"".equals(requestJson)) {
            String startTime = requestJson.getString("startTime");// 开始时间
            String endTime = requestJson.getString("endTime");// 结束时间
            String telNum = requestJson.getString("linkTel");// 电话号码
            String corpName = requestJson.getString("corpName");// 企业名称
            // 缓存id
            String sessionId = requestJson.getString("sessionid");

            if (null != startTime && !"".equals(startTime)) {
                condition.put("start_time_registDate", startTime.trim() + " 00:00:00");
            }
            if (null != endTime && !"".equals(endTime)) {
                condition.put("end_time_registDate", endTime.trim() + " 00:00:00");
            }
            if (null != telNum && !"".equals(telNum)) {
                condition.put("LIKE_linkTel", telNum.trim());
            }
            if (null != corpName && !"".equals(corpName)) {
                condition.put("LIKE_corpName", corpName.trim());
            }
            // 从缓存里面获取SessionId
            if (null != sessionId && !"".equals(sessionId)) {
                String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionId);
                if (null != josonUserObject && !"".equals(josonUserObject)) {
                    JSONObject js = JSONObject.parseObject(josonUserObject);
                    roleId = js.getString("roleId");
                    // 地市管理员只能看到2未下发、3已下发
                    if (Constants.DISHIADMIN.equals(roleId)) {
                        condition.put("EQ_corpCity", js.getString("userCityArea"));
                        condition.put("GTE_dealFlag", "2");
                        condition.put("LTE_dealFlag", "3");
                        // 区县管理员只能看到3未分配，4已分配，5未开户，7已开户
                    } else if (Constants.QUXIANADMIN.equals(roleId)) {
                        condition.put("EQ_corpArea", js.getString("userCityArea"));
                        condition.put("GTE_dealFlag", "3");
                        condition.put("LTE_dealFlag", "7");
                        // String dealFlags = "3,4,5,6,7";
                        // condition.put("IN_dealFlag", dealFlags);
                        // 客户经理只能看到4已经分配下来,5通过，6不通过
                    } else if (Constants.CUSTOMADMIN.equals(roleId)) {
                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("EQ_telNum", js.getString("telNum"));
                        List<CustomerVo> customerList = customerInterface.findCustomerByCondition(conditions, null);
                        CustomerVo customerVo = null;
                        if (null != customerList && customerList.size() > 0) {
                            customerVo = customerList.get(0);
                        }
                        if (null != customerVo) {
                            condition.put("EQ_customerId", customerVo.getId());
                        }
                        condition.put("GTE_dealFlag", "4");
                        condition.put("LTE_dealFlag", "6");
                    } else {
                        return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3006, "");
                    }
                } else {
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL3006, "");
                }
            }
        }
        condition.put("EQ_deleteFlag", "0");
        int pageIndex = 1;
        int pageSize = 1000;
        int total = 0;
        List<HlwCorpAuthVO> list = null;
        List<HlwCorpAuthVO> _list = new ArrayList<HlwCorpAuthVO>();
        Map<String, Object> m = hlwCorpAuthInterface.findAllByPage(pageIndex, 1, condition, null);
        if (null != m) {
            if (null != m.get("total")) {
                total = Integer.parseInt(m.get("total").toString());
            }
            sortMap.put("registDate", false);
            int xx = total % pageSize == 0 ? (total / pageSize) : (total / pageSize + 1);
            for (int i = 1; i <= xx; i++) { // 循环调用分页获取total / pageSize + 1次，一次pageSize条
                Map<String, Object> mList = hlwCorpAuthInterface.findAllByPage(i, pageSize, condition, sortMap);
                if (mList != null) {
                    list = (List<HlwCorpAuthVO>) mList.get("content");
                    _list.addAll(list);
                }
            }

            if (null == _list || _list.isEmpty()) {
                _list = new ArrayList<>();
            }
            byte[] b = null;
            String url = "";
            String fastDFSNode = BaseConstant.fastDFSNode;
            String nginxAddr = "";
            try {
                nginxAddr = zkUtil.findData(fastDFSNode);
            } catch (Exception e) {
                logger.error("获取导出地址报错", e);
            }
            try {
                b = writeExcel(_list, roleId);
                url = FastDFSUtil.uploadFile(b, "xls");
            } catch (Exception e) {
                logger.error("导出excel报错", e);
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
            }
            return ResponsePackUtil.buildPack("0000", nginxAddr + url);

        } else {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2018, "");
        }
    }

    /**
     * 导出excel
     * 
     * @param list
     * @return
     * @throws IOException
     * @throws RowsExceededException
     * @throws WriteException
     */
    public byte[] writeExcel(List<HlwCorpAuthVO> list, String roleId) throws IOException, RowsExceededException, WriteException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("sheet1", 0);
        sheet.mergeCells(0, 0, 8, 0);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
        WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
        WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
        titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
        titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
        Label title = new Label(0, 0, "互联网认证信息", titleFormate);
        sheet.setRowView(0, 600, false);// 设置第一行的高度
        sheet.addCell(title);

        sheet.addCell(new Label(0, 1, "序列号"));
        sheet.addCell(new Label(1, 1, "认证编号"));
        sheet.addCell(new Label(2, 1, "企业名称"));
        sheet.addCell(new Label(3, 1, "联系人"));
        sheet.addCell(new Label(4, 1, "手机号码"));
        sheet.addCell(new Label(5, 1, "所属地市"));
        sheet.addCell(new Label(6, 1, "所属区县"));
        sheet.addCell(new Label(7, 1, "申请时间"));
        sheet.addCell(new Label(8, 1, "审核状态"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            sheet.addCell(new Label(0, i + 2, i + 1 + ""));
            sheet.addCell(new Label(1, i + 2, trim(list.get(i).getAuthId())));
            sheet.addCell(new Label(2, i + 2, trim(list.get(i).getCorpName())));
            sheet.addCell(new Label(3, i + 2, trim(list.get(i).getLinkName())));
            sheet.addCell(new Label(4, i + 2, trim(list.get(i).getLinkTel())));
            if (null != list.get(i).getCorpCity() && !"".equals(list.get(i).getCorpCity())) {
                try {
                    DictionaryVo dictionaryVo = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, list.get(i).getCorpCity());
                    if (null != dictionaryVo) {
                        sheet.addCell(new Label(5, i + 2, trim(dictionaryVo.getDictKeyDesc())));
                    }
                } catch (Exception e) {
                    logger.error("导出excel查询字典地市失败", e);
                }
            } else {
                sheet.addCell(new Label(5, i + 2, ""));
            }

            if (null != list.get(i).getCorpArea() && !"".equals(list.get(i).getCorpArea())) {
                try {
                    DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, list.get(i).getCorpArea());
                    if (null != dictionaryVo1) {
                        sheet.addCell(new Label(6, i + 2, trim(dictionaryVo1.getDictKeyDesc())));
                    }
                } catch (Exception e) {
                    logger.error("导出excel查询字典区县失败", e);
                }
            } else {
                sheet.addCell(new Label(6, i + 2, ""));
            }

            if (null != list.get(i).getRegistDate() && !"".equals(list.get(i).getRegistDate())) {
                String str = sdf.format(list.get(i).getRegistDate());
                sheet.addCell(new Label(7, i + 2, str));
            } else {
                sheet.addCell(new Label(7, i + 2, ""));
            }

            String dealFlag = "";
            String getDealFlag = list.get(i).getDealFlag();
            if (null != getDealFlag && !"".equals(getDealFlag)) {
                if (Constants.DISHIADMIN.equals(roleId)) {
                    if ("2".equals(getDealFlag)) {
                        dealFlag = "未下发";
                    } else if ("3".equals(getDealFlag)) {
                        dealFlag = "已下发";
                    }
                } else if (Constants.QUXIANADMIN.equals(roleId)) {
                    if ("3".equals(getDealFlag)) {
                        dealFlag = "待分配";
                    } else if ("4".equals(getDealFlag)) {
                        dealFlag = "待反馈";
                    } else if ("5".equals(getDealFlag)) {
                        dealFlag = "待开通";
                    } else if ("6".equals(getDealFlag)) {
                        dealFlag = "不开通";
                    } else if ("7".equals(getDealFlag)) {
                        dealFlag = "已开通";
                    }
                } else if (Constants.CUSTOMADMIN.equals(roleId)) {
                    if ("4".equals(getDealFlag)) {
                        dealFlag = "待反馈";
                    } else if ("5".equals(getDealFlag)) {
                        dealFlag = "待开通";
                    } else if ("6".equals(getDealFlag)) {
                        dealFlag = "不开通";
                    }
                }

            }
            sheet.addCell(new Label(8, i + 2, dealFlag));
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
     * @author liujm
     */
    public static String trim(Object obj) {
        return (obj == null) ? "" : obj.toString().trim();
    }
}
