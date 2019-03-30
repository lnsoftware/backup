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
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;
import com.royasoft.vwt.controller.vo.QuestionFeedBackDTO;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.invitationSystem.api.interfaces.QuestionFeedBackInterface;
import com.royasoft.vwt.soa.business.invitationSystem.api.vo.QuestionFeedBackVO;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.customer.api.vo.CustomerVo;
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

@Scope("prototype")
@Service
public class UserFeedbackService implements Runnable {

    @Autowired
    private QuestionFeedBackInterface questionFeedBackService;

    @Autowired
    private MemberInfoInterface memberInfoInterface;

    @Autowired
    private CorpInterface corpInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private RedisInterface redisInterface;

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

    private final Logger logger = LoggerFactory.getLogger(UserFeedbackService.class);

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.useFeedback_queue.take();// 获取队列处理数据
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
                    logger.debug("用户反馈处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /***************************** 业务逻辑处理 *********************************************/

                    String res = "";// 响应结果
                    if (function_id == null || function_id.length() <= 0) {
                        ResponsePackUtil.CalibrationParametersFailure(channel, "任务业务请求参数校验失败！");
                    } else {
                        res = sendTaskBusinessLayer(function_id, user_id, request_body, request);
                    }
                    ResponsePackUtil.responseStatusOK(channel, res); // 响应成功
                    // 加入操作日志
                    String responseStatus = ResponsePackUtil.getResCode(res);
                    if (null != responseStatus && !"".equals(responseStatus))
                        ResponsePackUtil.cagHttpResponse(channel, res);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponse(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("用户反馈业务逻辑处理异常", e);
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
     */
    private String sendTaskBusinessLayer(String function_id, String user_id, String request_body, Object request) {

        String res = "";
        switch (function_id) {

            case FunctionIdConstant.getUserFeedbackList:// 创建任务
                res = getUserFeedbackList(request_body, user_id);
                break;
            case FunctionIdConstant.getUserFeedbackInfo:
                res = getuserFeedbackInfo(request_body, user_id);
                break;
            case FunctionIdConstant.beginUserFeedback:
                res = beginUserFeedback(request_body, user_id);
                break;
            case FunctionIdConstant.deleteUserFeedbackById:
                res = deleteUserFeedbackById(request_body, user_id);
                break;
            case FunctionIdConstant.exportUserFeedback:
                res = ExportUserFeedbackList(request_body, user_id);
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
    public String getUserFeedbackList(String request_body, String user_id) {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        logger.debug("获取公告列表,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        // String page = request.getParameter("page");// 前台传递的页面位置请求
        // String limit = request.getParameter("limit");// 前台传递的每页显示数
        //
        // String startTime=request.getParameter("startTime");//开始时间
        // String endTime=request.getParameter("endTime");//结束时间
        // String telNum=request.getParameter("telNum");//电话号码
        // String feedBackStatus=request.getParameter("feedBackStatus");//反馈状态
        int pageIndex = 1;
        int pageSize = 10;

        // 地市区域条件查询
        Map<String, Object> condition = new HashMap<String, Object>();

        if (null != requestJson && !"".equals(requestJson)) {
            String page = requestJson.getString("page");// 前台传递的页面位置请求
            String limit = requestJson.getString("limit");// 前台传递的每页显示数
            String startTime = requestJson.getString("startTime");// 开始时间
            String endTime = requestJson.getString("endTime");// 结束时间
            String telNum = requestJson.getString("telNum");// 电话号码
            String feedBackStatus = requestJson.getString("fBFlag");// 反馈状态
            String opinionType = requestJson.getString("opinionType"); 

            if (null != page && !"".equals(page)) {
                pageIndex = Integer.parseInt(page);
            }
            if (null != limit && !"".equals(limit)) {
                pageSize = Integer.parseInt(limit);
            }
            if (null != startTime && !"".equals(startTime)) {
                condition.put("start_time_questionDate", startTime.trim() + " 00:00:00");
            }
            if (null != endTime && !"".equals(endTime)) {
                condition.put("end_time_questionDate", endTime.trim() + " 00:00:00");
            }
            // conditions.put("end_time_planPushTime", dateFormat.format(new Date()));

            if (null != telNum && !"".equals(telNum)) {
                condition.put("LIKE_telNum", telNum.trim());
            }
            if (null != feedBackStatus && !"".equals(feedBackStatus)) {
                condition.put("EQ_FBFlag", feedBackStatus.trim());
            }
            if (null != opinionType && !"".equals(opinionType)) {
                condition.put("EQ_opinionType", opinionType.trim());
            }           
        }
        condition.put("EQ_delFlag", "0");
        sortMap.put("questionDate", false);
        int total = 0;
        List<QuestionFeedBackVO> list = null;
        Map<String, Object> m = questionFeedBackService.findAllByPage(pageIndex, pageSize, condition, sortMap);
        if (null != m) {
            list = (List<QuestionFeedBackVO>) m.get("content");
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
     * 用户反馈列表查询
     * 
     * @param list
     * @return
     */
    public List<Map<String, Object>> transeferTotable(List<QuestionFeedBackVO> list) {
        List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
        try {
            logger.debug("反馈问题数据封装List:{}", list);
            for (QuestionFeedBackVO cv : list) {
                Map<String, Object> corpMap = new HashMap<String, Object>();
                // 数据封装
                corpMap.put("id", cv.getFBID());
                corpMap.put("FBCode", cv.getFBCode());
                corpMap.put("isLogin", cv.getIsLogin());
                corpMap.put("opinionType", cv.getOpinionType());
                corpMap.put("FBContent", cv.getFBContent());
                corpMap.put("FBquestion", cv.getFBquestion());
                corpMap.put("telNum", cv.getTelNum());
                corpMap.put("FBFlag", cv.getFBFlag());
                // 如果处在登录的状态时才显示客户经理的名称、客户经理电话号码、所属地市以及区县
                if (cv.getIsLogin() == 1) {
                    MemberInfoVO memberInfoVO = memberInfoInterface.findById(cv.getUserId());
                    if (memberInfoVO != null) {
                        CorpVO corpVO = corpInterface.findById(memberInfoVO.getCorpId());
                        if (corpVO != null) {
                            CustomerVo customerVo = customerInterface.findCustomerById(corpVO.getCustomerId());
                            if (customerVo != null) {
                                // 客户经理名称
                                corpMap.put("customerName", customerVo.getName());
                                // 客户经理电话号码
                                corpMap.put("customerTelnum", customerVo.getTelNum());
                                DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, customerVo.getRegion());
                                if (dictionaryVo1 != null) {
                                    // 地市
                                    corpMap.put("customerRegion", dictionaryVo1.getDictKeyDesc());
                                }
                                DictionaryVo dictionaryVo2 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, customerVo.getArea());
                                if (dictionaryVo2 != null) {
                                    // 区县
                                    corpMap.put("customerArea", dictionaryVo2.getDictKeyDesc());
                                }
                            }
                        }
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String str = sdf.format(cv.getQuestionDate());
                corpMap.put("questionDate", str);
                
                tableList.add(corpMap);
            }
        } catch (Exception e) {
            logger.error("反馈问题数据封装异常", e);
        }
        return tableList;
    }

    @SuppressWarnings("unused")
    public String getuserFeedbackInfo(String request_body, String user_id) {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String id = requestJson.getString("id");

        QuestionFeedBackVO questionFeedBackVO = null;
        QuestionFeedBackDTO questionFeedBackDTO = new QuestionFeedBackDTO();
        try {
            questionFeedBackVO = questionFeedBackService.findFeedBackById(id);
            MemberInfoVO memberInfoVO = memberInfoInterface.findById(questionFeedBackVO.getUserId());
            if (null != questionFeedBackVO) {
                // uuid
                questionFeedBackDTO.setFBID(questionFeedBackVO.getFBID());
                // 提出时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                questionFeedBackDTO.setQuestionDate(sdf.format(questionFeedBackVO.getQuestionDate()));
                // 是否登录
                questionFeedBackDTO.setIsLogin(questionFeedBackVO.getIsLogin() == 0 ? "未登录" : "已登录");
                // 手机号码
                questionFeedBackDTO.setTelNum(questionFeedBackVO.getTelNum());
                // 用户名字
                if (memberInfoVO != null) {
                    questionFeedBackDTO.setUserName(memberInfoVO.getMemberName());
                    if (questionFeedBackVO.getIsLogin() == 1) {
                        CorpVO corpVO = corpInterface.findById(memberInfoVO.getCorpId());
                        if (corpVO != null) {
                            CustomerVo customerVo = customerInterface.findCustomerById(corpVO.getCustomerId());
                            if (customerVo != null) {
                                // 客户经理名称
                                questionFeedBackDTO.setCustomerName(customerVo.getName());
                                // 客户经理电话号码
                                questionFeedBackDTO.setCustomerTelnum(customerVo.getTelNum());
                                DictionaryVo dictionaryVo1 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, customerVo.getRegion());
                                if (dictionaryVo1 != null) {
                                    // 地市
                                    questionFeedBackDTO.setCustomerRegion(dictionaryVo1.getDictKeyDesc());
                                }
                                DictionaryVo dictionaryVo2 = dictionaryInterface.findDictionaryByDictIdAndKey(Constants.DICTIONARYID, customerVo.getArea());
                                if (dictionaryVo2 != null) {
                                    // 区县
                                    questionFeedBackDTO.setCustomerArea(dictionaryVo2.getDictKeyDesc());
                                }
                            }
                        }
                    }
                }
                // 模块大类
                questionFeedBackDTO.setModuleName(questionFeedBackVO.getModuleName());
                // 功能分类
                questionFeedBackDTO.setFunctionName(questionFeedBackVO.getFunctionName());
                // 问题编码
                questionFeedBackDTO.setFBCode(questionFeedBackVO.getFBCode());
                // 用户反馈问题
                questionFeedBackDTO.setFBquestion(questionFeedBackVO.getFBquestion());
                // 反馈内容
                questionFeedBackDTO.setFBContent(questionFeedBackVO.getFBContent());
                // 问题场景ID
                questionFeedBackDTO.setProblemsceneID(questionFeedBackVO.getProblemsceneID());
                // 问题场景
                questionFeedBackDTO.setProblemSceneList(questionFeedBackVO.getProblemSceneList());
                // 5张图片
                
                questionFeedBackDTO.setOpinionType(questionFeedBackVO.getOpinionType());

                String fastDFSNode = Constants.fastDFSNode;
                String trackerAddr = "";
                try {
                    trackerAddr = zkUtil.findData(fastDFSNode);
                    logger.debug("获取图片fast地址fastDFSNode:{}", fastDFSNode);
                } catch (Exception e) {
                    logger.error("获取图片fast地址失败", e);
                }
                if (null != questionFeedBackVO.getImg1() && !"".equals(questionFeedBackVO.getImg1())) {
                    questionFeedBackDTO.setImg1(trackerAddr + questionFeedBackVO.getImg1());
                } else {
                    questionFeedBackDTO.setImg1("");
                }

                if (null != questionFeedBackVO.getImg2() && !"".equals(questionFeedBackVO.getImg2())) {
                    questionFeedBackDTO.setImg2(trackerAddr + questionFeedBackVO.getImg2());
                } else {
                    questionFeedBackDTO.setImg2("");
                }
                
                if (null != questionFeedBackVO.getImg3() && !"".equals(questionFeedBackVO.getImg3())) {
                    questionFeedBackDTO.setImg3(trackerAddr + questionFeedBackVO.getImg3());
                } else {
                    questionFeedBackDTO.setImg3("");
                }
                
                if (null != questionFeedBackVO.getImg4() && !"".equals(questionFeedBackVO.getImg4())) {
                    questionFeedBackDTO.setImg4(trackerAddr + questionFeedBackVO.getImg4());
                } else {
                    questionFeedBackDTO.setImg4("");
                }
                
                if (null != questionFeedBackVO.getImg5() && !"".equals(questionFeedBackVO.getImg5())) {
                    questionFeedBackDTO.setImg5(trackerAddr + questionFeedBackVO.getImg5());
                } else {
                    questionFeedBackDTO.setImg5("");
                }
                
                model.put("success", true);
                model.put("questionFeedBack", questionFeedBackDTO);
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
     * 对反馈问题进行反馈
     * 
     * @param request
     * @param response
     * @return
     */
    public String beginUserFeedback(String request_body, String user_id) {
        Map<String, Object> model = new HashMap<String, Object>();
        QuestionFeedBackVO questionFeedBackVO = null;
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String id = requestJson.getString("id");
        String FBContent = requestJson.getString("fBContent");
        String sessionId = requestJson.getString("sessionid");
        try {
            questionFeedBackVO = questionFeedBackService.findFeedBackById(id);
        } catch (Exception e) {
            logger.error("进行反馈问题时查询异常", e);
        }
        if (null != questionFeedBackVO) {
            questionFeedBackVO.setFBContent(FBContent);
        }
        questionFeedBackVO.setFBFlag(1);
        questionFeedBackVO.setFBDate(new Date());
        questionFeedBackVO.setUpdateTime(new Date());
        if (null != sessionId && !"".equals(sessionId)) {
            // 用户的基本信息
            String josonUserObject = redisInterface.getString(Constants.nameSpace + sessionId);
            if (null != josonUserObject && !"".equals(josonUserObject)) {
                JSONObject js = JSONObject.parseObject(josonUserObject);
                questionFeedBackVO.setFBUserid(js.getString("userId"));
                questionFeedBackVO.setUpdateId(js.getString("userId"));
            }
        }
        QuestionFeedBackVO questionFeedBackresult = null;
        try {
            questionFeedBackresult = questionFeedBackService.saveFeedBack(questionFeedBackVO);
        } catch (Exception e) {
            logger.error("进行反馈问题时异常", e);
        }

        if (null != questionFeedBackresult) {
            model.put("success", true);
            model.put("resultMsg", Constants.ACTION_SUCCESS);
        } else {
            model.put("success", false);
            model.put("resultMsg", Constants.ACTION_FAIL);
        }
        return JSONObject.toJSONString(model);
    }

    /**
     * 删除用户反馈
     * 
     * @param request_body
     * @param user_id
     * @return
     */
    public String deleteUserFeedbackById(String request_body, String user_id) {
        Map<String, Object> model = new HashMap<String, Object>();
        boolean flag = false;
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String id = requestJson.getString("id");
        try {
            flag = questionFeedBackService.deleteFeedBackById(id);
        } catch (Exception e) {
            logger.error("删除用户提出问题异常", e);
        }
        if (flag) {
            model.put("success", true);
            model.put("resultMsg", Constants.ACTION_SUCCESS);
        } else {
            model.put("success", false);
            model.put("resultMsg", Constants.ACTION_FAIL);
        }
        return JSONObject.toJSONString(model);
    }

    /**
     * 导出用户反馈列表查询
     * 
     * @param request_body
     * @param user_id
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public String ExportUserFeedbackList(String request_body, String user_id) {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        logger.debug("获取公告列表,requestBody:{},userId:{}", request_body, user_id);
        JSONObject requestJson = JSONObject.parseObject(request_body);
        int pageIndex = 1;
        int pageSize = 100000;

        // 地市区域条件查询
        Map<String, Object> condition = new HashMap<String, Object>();

        if (null != requestJson && !"".equals(requestJson)) {
            String startTime = requestJson.getString("startTime");// 开始时间
            String endTime = requestJson.getString("endTime");// 结束时间
            String telNum = requestJson.getString("telNum");// 电话号码
            String feedBackStatus = requestJson.getString("fBFlag");// 反馈状态
            String opinionType = requestJson.getString("opinionType"); 

            if (null != startTime && !"".equals(startTime)) {
                condition.put("start_time_questionDate", startTime.trim() + " 00:00:00");
            }
            if (null != endTime && !"".equals(endTime)) {
                condition.put("end_time_questionDate", endTime.trim() + " 00:00:00");
            }
            // conditions.put("end_time_planPushTime", dateFormat.format(new Date()));

            if (null != telNum && !"".equals(telNum)) {
                condition.put("LIKE_telNum", telNum.trim());
            }
            if (null != feedBackStatus && !"".equals(feedBackStatus)) {
                condition.put("EQ_FBFlag", feedBackStatus.trim());
            }
            if (null != opinionType && !"".equals(opinionType)) {
                condition.put("EQ_opinionType", opinionType.trim());
            }           
        }
        condition.put("EQ_delFlag", "0");
        sortMap.put("questionDate", false);
        int total = 0;
        List<QuestionFeedBackVO> list = null;
        Map<String, Object> m = questionFeedBackService.findAllByPage(pageIndex, pageSize, condition, sortMap);
        if (null != m) {
            list = (List<QuestionFeedBackVO>) m.get("content");
            total = PageUtils.getPageCount(Integer.parseInt(m.get("total").toString()), pageSize);
            if (total > 0) {
                // 封装后的数据
                List<Map<String, Object>> list1 = this.transeferTotable(list);
                String path = writeExcel(list1);
                model.put("success", true);
                model.put("path", path);
            } else {
                // 数据不存在时返回一条无对应数据提示
                Map<String, Object> corpMap = new HashMap<String, Object>();
                corpMap.put("errorMessage", "数据不存在");
                List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
                tableList.add(corpMap);
                model.put("success", false);
            }
        } else {
            // 数据查询异常返回异常提示
            Map<String, Object> corpMap = new HashMap<String, Object>();
            corpMap.put("errorMessage", "查询异常");
            List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
            tableList.add(corpMap);
            model.put("success", false);
        }
        
        return JSONObject.toJSONString(model);
    }
    
    /**
     * 生成excel
     * 
     * @param list
     * @param corpId
     * @param request
     * @param response
     * @author Jiangft 2016年4月14日
     * @throws IOException
     */
    public String writeExcel(List<Map<String, Object>> list) {
    	logger.debug("进入生成Excel方法");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String filePath = "";
        byte[] b = null;
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("sheet1", 0);
            sheet.mergeCells(0, 0, 8, 0);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
            WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
            WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
            titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
            titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
            Label title = new Label(0, 0, "反馈问题查询", titleFormate);
            sheet.setRowView(0, 600, false);// 设置第一行的高度
            sheet.addCell(title);
            sheet.addCell(new Label(0, 1, "序号"));
            sheet.addCell(new Label(1, 1, "是否登录"));
            sheet.addCell(new Label(2, 1, "反馈问题"));
            sheet.addCell(new Label(3, 1, "管理员回复内容"));
            sheet.addCell(new Label(4, 1, "手机号码"));
            sheet.addCell(new Label(5, 1, "提出问题时间"));
            sheet.addCell(new Label(6, 1, "反馈状态"));
            sheet.addCell(new Label(7, 1, "地市"));
            sheet.addCell(new Label(8, 1, "类型"));

            sheet.setColumnView(0, 10);
            sheet.setColumnView(1, 10);
            sheet.setColumnView(2, 30);
            sheet.setColumnView(3, 30);
            sheet.setColumnView(4, 10);
            sheet.setColumnView(5, 10);
            sheet.setColumnView(6, 10);
            sheet.setColumnView(7, 10);
            sheet.setColumnView(8, 10);
            sheet.setColumnView(9, 10);
        
            for (int i = 0; i < list.size(); i++) {
            	logger.debug("opinionType的值为:",list.get(i).get("opinionType"));
            	sheet.addCell(new Label(0, i + 2, i + 1 + ""));     
                sheet.addCell(new Label(1, i + 2, (int)list.get(i).get("isLogin") == 0?"未登录":"已登录"));
                sheet.addCell(new Label(2, i + 2, list.get(i).get("FBquestion")+""));               
                sheet.addCell(new Label(3, i + 2, list.get(i).get("FBContent")+""));
                sheet.addCell(new Label(4, i + 2, list.get(i).get("telNum")+""));
                sheet.addCell(new Label(5, i + 2, list.get(i).get("questionDate")+""));
                sheet.addCell(new Label(6, i + 2, (int)list.get(i).get("FBFlag") == 0?"未反馈":"已反馈"));
                sheet.addCell(new Label(7, i + 2, list.get(i).get("customerRegion")+""));
                sheet.addCell(new Label(8, i + 2, "0".equals(list.get(i).get("opinionType"))?"意见反馈":"需求收集"));                            
            }
            logger.debug("开始写入");
            wwb.write();
            logger.debug("开始关闭");
            wwb.close();
            b = os.toByteArray();
            filePath = FastDFSUtil.uploadFile(b, "xls");
            logger.debug("上传excel至文件服务器相对地址filePath:{}", filePath);
            filePath = zkUtil.findData(Constants.FILE_URL) + filePath;
            logger.debug("上传excel至文件服务器绝对地址filePath:{}", filePath);
        } catch (Exception e) {
            logger.error("导出异常e:{}", e);
        } finally {
            if (null != os) {
                try {
					os.close();
				} catch (IOException e) {
					logger.error("流关闭异常e:{}", e);
				}
            }
        }
        return filePath;
    }
    
}
