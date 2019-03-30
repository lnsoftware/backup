/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.util.mq.MsgPushUtil;
import com.royasoft.vwt.cag.util.upload.FastDFSUtil;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.dictionary.api.vo.DictionaryVo;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceInfoInterface;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceAnnexVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceContentVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceInfoVo;
import com.royasoft.vwt.soa.business.announce.api.vo.AnnounceVo;
import com.royasoft.vwt.soa.business.invitationSystem.api.interfaces.QuestionFeedBackInterface;
import com.royasoft.vwt.soa.business.invitationSystem.api.vo.FAQVo;
import com.royasoft.vwt.soa.business.invitationSystem.api.vo.QuestionFeedBackVO;
import com.royasoft.vwt.soa.business.square.api.interfaces.GraphicPushInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicSourceInterface;
import com.royasoft.vwt.soa.graphicpush.api.utils.Response;

/**
 * 邀请体系 处理类
 *
 * @Author:daizl
 */
@Scope("prototype")
@Service
public class InviteSystemService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(InviteSystemService.class);

    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private DictionaryInterface dictionaryInterface;

    @Autowired
    private QuestionFeedBackInterface questionFeedBackInterface;

    @Autowired
    private MsgPushUtil msgPushUtil;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private AnnounceInfoInterface announceInfoInterface;
    
    @Autowired
    private GraphicSourceInterface graphicSourceInterface;
    
    @Autowired
    private GraphicPushInterface graphicPushInterface;


    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.inviteSystem_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id(); // 获取功能ID
                    String user_id = queue_packet.getUser_id(); // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("签到积分处理类(入口),function_id:{},user_id:{},request_body:{},tel_number:{}", function_id, user_id, request_body, tel_number);
                    /***************************** 业务逻辑处理 *********************************************/

                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1005, "");// 响应结果
                    switch (function_id) {
                        case FunctionIdConstant.INVITATIONSYSTEM_HOTFAQLIST:// 邀请体系，未登陆的情况下获取帮助首页
                            resInfo = getHelpWithoutLogin(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.INVITATIONSYSTEM_FAQLIST:// 邀请体系，根据功能号加载问题详情
                            resInfo = getInviteFAQ(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.INVITATIONSYSTEM_PROBLEMSCENE:// 邀请体系，获取问题场景
                            resInfo = getInviteProblemScene(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.INVITATIONSYSTEM_FEEDBACK:// 邀请体系，问题反馈
                            resInfo = feedbackInvite(user_id, tel_number, request_body, msg);
                            break;
                        case FunctionIdConstant.INVITATIONSYSTEM_MYPEROBLEM:// 邀请体系，查询自己的反馈
                            resInfo = feedbackofmine(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.INVITATIONSYSTEM_MODULES:// 邀请体系，加载首页信息(获取模块，功能)
                            resInfo = getInviteModules(user_id, tel_number, request_body);
                            break;
                        case FunctionIdConstant.ANNOUNCEDETAILFORH5:
                            resInfo = getAnnounceInfoForH5(request_body);
                            break;
                        case FunctionIdConstant.ANNOUNCERECORDH5:
                            resInfo = getAnnounceRecordForH5(request_body);
                            break;
                        case FunctionIdConstant.GETGRAPHICSOURCEPREVIEW:
                            resInfo = getGraphicSourcePreview(request_body);
                            break;
                        case FunctionIdConstant.GETGRAPHICSOURCECONTENT:
                            resInfo = getGraphicSourceContent(request_body);
                            break;
                        case FunctionIdConstant.GETGRAPHICSOURCECOUNTBYID:
                            resInfo = getGraphicSourceCountById(request_body);
                            break;
                        default:
                            break;
                    }
                    logger.debug("邀请体系处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus))
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    continue;
                }
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("邀请体系业务逻辑处理异常", e);
                // 响应客户端异常
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
     * 未登陆的情况下获取帮助首页
     * 
     * @return
     */
    public String getHelpWithoutLogin(String user_id, String tel_number, String request_body) {
        logger.debug("邀请体系-未登陆的情况下获取帮助首页,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            // 获取登录模块下的热点问题
            Map<String, Object> conditions = new HashMap<String, Object>();
            Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
            conditions.put("EQ_isHot", 1);
            conditions.put("EQ_FAQType", 1);
            conditions.put("EQ_moduleCode", 11);
            sortMap.put("indexId", true);
            List<FAQVo> faqList = questionFeedBackInterface.findFAQByConditions(conditions, sortMap);
            // 获取登录模块下的子节点
            List<DictionaryVo> dicList = dictionaryInterface.findDictionaryByDictIdAndDictValue(106L, "11");
            for (DictionaryVo dictionaryVo : dicList) {
                String path = dictionaryVo.getDictValueDesc();
                if (!org.springframework.util.StringUtils.isEmpty(path))
                    path = ParamConfig.file_server_url + path;
                dictionaryVo.setDictValueDesc(path);
            }
            conditions.clear();
            conditions.put("moduleList", dicList);
            conditions.put("faqList", faqList);
            return ResponsePackUtil.buildPack("0000", conditions);
        } catch (Exception e) {
            logger.error("邀请体系-未登陆的情况下获取帮助首页异常,userid:{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "");
        }

    }

    /**
     * 邀请体系 登录后获取模块数据 首页
     * 
     * @return
     */
    public String getInviteModules(String user_id, String tel_number, String request_body) {
        logger.debug("邀请体系-获取模块数据,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            List<DictionaryVo> list = dictionaryInterface.findDictionaryByDictId(106L);
            for (DictionaryVo dictionaryVo : list) {
                String path = dictionaryVo.getDictValueDesc();
                if (!org.springframework.util.StringUtils.isEmpty(path))
                    path = ParamConfig.file_server_url + path;
                dictionaryVo.setDictValueDesc(path);
            }
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("邀请体系-获取模块数据异常,userid:{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1068, "");
        }
    }

    /**
     * 邀请体系 获取常见问题(包含热点和非热点问题) 登录前后登录后可共用此接口
     * 
     * @return
     */
    public String getInviteFAQ(String user_id, String tel_number, String request_body) {
        logger.debug("邀请体系-获取常见问题,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        List<FAQVo> list = new ArrayList<FAQVo>();
        try {
            Map<String, Object> conditions = new HashMap<String, Object>();
            JSONObject requestJson = JSONObject.parseObject(request_body);
            String moduleCode = requestJson.getString("module_code");
            String functionCode = requestJson.getString("function_code");
            if (!StringUtils.checkParam(moduleCode, true, 4) || !StringUtils.checkParam(functionCode, true, 4))
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1067, "");

            conditions.put("EQ_FAQType", 1);
            conditions.put("EQ_moduleCode", moduleCode);
            conditions.put("EQ_functionCode", functionCode);
            list = questionFeedBackInterface.findFAQByConditions(conditions, null);
            return ResponsePackUtil.buildPack("0000", list);
        } catch (Exception e) {
            logger.error("邀请体系-获取常见问题异常,userid:{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1068, "");
        }

    }

    /**
     * 邀请体系 获取问题场景
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     */
    public String getInviteProblemScene(String user_id, String tel_number, String request_body) {
        logger.debug("邀请体系-获取问题场景,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        Map<String, Object> conditions = new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String moduleCode = requestJson.getString("module_code");
        String functionCode = requestJson.getString("function_code");
        if (!StringUtils.checkParam(moduleCode, true, 4) || !StringUtils.checkParam(functionCode, true, 4))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1067, "");

        conditions.put("EQ_FAQType", 2);
        conditions.put("EQ_moduleCode", moduleCode);
        conditions.put("EQ_functionCode", functionCode);
        sortMap.put("indexId", true);
        List<FAQVo> list = questionFeedBackInterface.findFAQByConditions(conditions, sortMap);

        return ResponsePackUtil.buildPack("0000", list);
    }

    /**
     * 邀请体系，问题反馈
     * 
     * @return
     */
    public String feedbackInvite(String user_id, String tel_number, String request_body, Object msg) {
        logger.debug("邀请体系-问题反馈,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        try {
            JSONObject requestJson = JSONObject.parseObject(request_body);
            Integer isLogin = requestJson.getInteger("is_login");// 必选 是否登陆 0 1
            String moduleCode = requestJson.getString("module_code");// 必选 模块编码
            String functionCode = requestJson.getString("function_code");// 必选 功能编码
            String problemsceneID = requestJson.getString("problemscene_id");// 可选 问题场景
            String FBquestion = requestJson.getString("fb_question");// 可选 反馈问题
            String opinionType = requestJson.getString("opinionType");
            if("1".equals(opinionType)){
            	isLogin = 1;
            	moduleCode = "-1";
            	functionCode = "-1";
            	
            }

            /** 校验参数 */
            if(!"1".equals(opinionType)){
            	if (!StringUtils.checkParam(moduleCode, true, 4) || !StringUtils.checkParam(functionCode, true, 4) || !StringUtils.checkParam(FBquestion, false, 200))
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1067, "");
            }            
            QuestionFeedBackVO questionFeedBackVO = new QuestionFeedBackVO();

            String file = requestJson.getString("data");
            if (!org.springframework.util.StringUtils.isEmpty(file)) {
                questionFeedBackVO = savePic(questionFeedBackVO, file);
                if (null == questionFeedBackVO)
                    return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1069, "");
            }

            questionFeedBackVO.setFBID(UUID.randomUUID().toString());
            questionFeedBackVO.setIsLogin(isLogin);
            questionFeedBackVO.setModuleCode(moduleCode);
            questionFeedBackVO.setFunctionCode(functionCode);
            questionFeedBackVO.setProblemsceneID(problemsceneID);
            questionFeedBackVO.setFBCode(UUID.randomUUID().toString());
            questionFeedBackVO.setUserId(user_id);
            questionFeedBackVO.setTelNum(tel_number);
            questionFeedBackVO.setFBquestion(FBquestion);
            questionFeedBackVO.setQuestionDate(new Date());
            questionFeedBackVO.setFBFlag(0);
            questionFeedBackVO.setDelFlag(0);
            Date now = new Date();
            questionFeedBackVO.setCreateTime(now);
            questionFeedBackVO.setQuestionDate(now);
            questionFeedBackVO.setCreateId(user_id);
            questionFeedBackVO.setOpinionType(opinionType);
            questionFeedBackVO = questionFeedBackInterface.saveFeedBack(questionFeedBackVO);
            if (questionFeedBackVO == null)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1070, "");

            return ResponsePackUtil.buildPack("0000", questionFeedBackVO);
        } catch (Exception e) {
            logger.error("邀请体系-问题反馈异常,userid:{},telnum:{},requestbody:{},e:{}", user_id, tel_number, request_body, e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1068, "");

        }
    }
    
    /**
     * 邀请体系 获取自己反馈的内容
     * 
     * @param user_id
     * @param tel_number
     * @param request_body
     * @return
     */
    public String feedbackofmine(String user_id, String tel_number, String request_body) {
        logger.debug("邀请体系-获取问题场景,userid:{},telnum:{},requestbody:{}", user_id, tel_number, request_body);
        Map<String, Object> conditions = new HashMap<String, Object>();
        Map<String, Object> model=new HashMap<String, Object>();
        Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
        JSONObject requestJson = JSONObject.parseObject(request_body);
        String telNum = requestJson.getString("telNum");
        String functionCode = requestJson.getString("function_code");
        String page = requestJson.getString("page");// 前台传递的页面位置请求
        String limit = requestJson.getString("limit");// 前台传递的每页显示数
        if ("".equals(telNum))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1067, "");
        
        int pageIndex = 1;
        int pageSize = 10;
        if (null != page && !"".equals(page)) {
            pageIndex = Integer.parseInt(page);
        }
        if (null != limit && !"".equals(limit)) {
            pageSize = Integer.parseInt(limit);
        }
        conditions.put("EQ_telNum", telNum);
      //  conditions.put("EQ_moduleCode", moduleCode);
        if(null!=functionCode&&!"".equals(functionCode))
            conditions.put("EQ_functionCode", functionCode);
        
        sortMap.put("indexId", true);
        model = questionFeedBackInterface.findAllByPage(pageIndex, pageSize,conditions,null);

        return ResponsePackUtil.buildPack("0000", model);
    }
    
    /**
     * 保存图片
     * 
     * @param questionFeedBackVO
     * @param file
     * @return
     */
    public QuestionFeedBackVO savePic(QuestionFeedBackVO questionFeedBackVO, String file) {
        String filePath;
        JSONArray jsonArray = JSON.parseArray(file);
        for (int i = 0; i < jsonArray.size(); i++) {
            String files = jsonArray.getString(i);
            byte[] fileByte = Base64.decodeBase64(files);
            filePath = FastDFSUtil.uploadFile(fileByte, "jpg");
            if (org.springframework.util.StringUtils.isEmpty(filePath))
                return null;

            switch (i) {
                case 0:
                    questionFeedBackVO.setImg1(filePath.replace("\\", "/"));
                    break;
                case 1:
                    questionFeedBackVO.setImg2(filePath.replace("\\", "/"));
                    break;
                case 2:
                    questionFeedBackVO.setImg3(filePath.replace("\\", "/"));
                    break;
                case 3:
                    questionFeedBackVO.setImg4(filePath.replace("\\", "/"));
                    break;
                case 4:
                    questionFeedBackVO.setImg5(filePath.replace("\\", "/"));
                    break;
                default:
                    break;
            }
        }
        return questionFeedBackVO;
    }

    /**
     * 获取公告详情 2.1版本后，由于管理平台支持预览效果，该接口提供数据给H5(该接口不加密)，手机客户端直接访问H5展示
     * 
     * @param requestBody 请求内容
     * @param userKey AES加密密钥
     * @return
     * @Description:
     */
    public String getAnnounceInfoForH5(String requestBody) throws Exception {
        logger.debug("获取公告详情H5,requestBody:{},telNum:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");
        String userId = requestJson.getString("userid");
        logger.debug("获取公告详情H5(解析body),id:{}", id);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(userId);
        valicateList.add(id);
        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1007, "");

        /** 查询详情 */
        AnnounceVo announceVo = announceInfoInterface.findAnnounceById(Long.valueOf(id));

        List<AnnounceContentVo> contentList = announceVo.getAnnounceContentVoList();
        StringBuffer contentStr = new StringBuffer();
        String pic = "";
        if (contentList != null && !contentList.isEmpty()) {
            /** 改造前，公告有多个段落，改造后编辑页只有一个段落，需要处理旧数据中的多个段落合并到一个段落中 */
            for (AnnounceContentVo announceContentVo : contentList) {
                String imgpath = announceContentVo.getAnnouncePic();
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(announceContentVo.getAnnouncePic())) {
					
                	pic = announceContentVo.getAnnouncePic();
				}
                String content = announceContentVo.getAnnounceContent();
//                if (!org.springframework.util.StringUtils.isEmpty(imgpath)) {
//                    contentStr.append("<img src='" + getFileUrl(imgpath) + imgpath + "'>");
//                }
                if (!org.springframework.util.StringUtils.isEmpty(content))
                    contentStr.append(content);
            }
        }
        /** 处理附件 */
        List<AnnounceAnnexVo> annexList = announceVo.getAnnounceAnnexVoList();
        if (annexList != null && annexList.size() > 0) {
            for (AnnounceAnnexVo announceAnnexVo : annexList) {
                announceAnnexVo.setAnnexUrlAbsolute(getFileUrl(announceAnnexVo.getAnnexUrl()) + announceAnnexVo.getAnnexUrl());
            }
        }

        AnnounceInfoVo announceInfoVo = announceVo.getAnnounceInfoVo();
        if (announceInfoVo != null) {
        	
            announceInfoVo.setSinglePic(pic);
            announceInfoVo.setAnnounceContent(contentStr.toString());
            announceInfoVo.setAnnounceCoverAbsolute(getFileUrl(announceInfoVo.getAnnounceCover()) + announceInfoVo.getAnnounceCover());
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("info", announceInfoVo);// 公告基本内容
        bodyJson.put("content", contentStr);// 正文内容
        bodyJson.put("annex", annexList);// 公告附件信息
        SerializeConfig ser = new SerializeConfig();
        ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        return ResponsePackUtil.buildPack("0000", bodyJson, ser);
    }

    /**
     * 获取公告阅读数，该接口提供给H5客户端
     * 
     * @param requestBody
     * @return
     * @throws Exception
     */
    public String getAnnounceRecordForH5(String requestBody) throws Exception {
        logger.debug("获取公告详情阅读数,requestBody:{},telNum:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        long id = requestJson.getLongValue("id");
        String userId = requestJson.getString("userid");
        /** 公告被阅读数 */
        int recordOfAnnounce = announceInfoInterface.getRecordCountByAnnounceId(id, userId);
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("record", recordOfAnnounce);
        return ResponsePackUtil.buildPack("0000", bodyJson);
    }

    /**
     * 根据文件路径判断文件服务器地址
     * 
     * @param filepath
     * @return
     */
    public String getFileUrl(String filepath) {
        if (org.springframework.util.StringUtils.isEmpty(filepath))
            return null;
        return filepath.startsWith("/group") ? ParamConfig.file_server_url : ParamConfig.nginx_address;
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
     * 根据图文预览id获取预览内容
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年8月10日
     */
    public String getGraphicSourcePreview(String requestBody) {
        logger.debug("根据图文预览id获取预览内容,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String id = requestJson.getString("id");
        if (org.apache.commons.lang3.StringUtils.isEmpty(id))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1116, "");
        try {
            logger.debug("根据图文预览id获取预览内容,id:{}", id);
            Response o = graphicSourceInterface.getGraphicsourcePreview(id);
            logger.debug("根据图文预览id获取预览内容请求服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1117, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("根据图文预览id获取预览内容请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1118, "");
        }
    }

    /**
     * 根据素材id获取素材内容
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Wuyf 2016年8月10日
     */
    public String getGraphicSourceContent(String requestBody) {
        logger.debug("根据素材id获取素材内容,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String gid = requestJson.getString("gid");
        if (org.apache.commons.lang3.StringUtils.isEmpty(gid))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1116, "");
        Long graphicId = 0L;
        try {
            graphicId = Long.parseLong(gid);
        } catch (Exception e) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1116, "");
        }
        try {
            logger.debug("根据素材id获取素材内容,id:{}", gid);
            Response o = graphicSourceInterface.getGraphicsourceContent(graphicId);
            logger.debug("根据素材id获取素材内容请求服务返回结果,response:{}", JSON.toJSONString(o));
            if (null == o)
                return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1117, "");
            return ResponsePackUtil.buildPack(o);
        } catch (Exception e) {
            logger.error("根据素材id获取素材内容请求服务异常", e);
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1118, "");
        }
    }

    public String getGraphicSourceCountById(String requestBody) {
        logger.debug("获取图文阅读次数,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String serviceId = requestJson.getString("serviceID");
        String newsId = requestJson.getString("newsId");
        logger.debug("获取图文阅读次数(解析body),serviceId:{},newsId{}", serviceId, newsId);
        List<String> valicateList = new ArrayList<String>();
        valicateList.add(serviceId);
        valicateList.add(newsId);
        /** 校验参数 */
        if (!valicateParams(valicateList))
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1021, "");

        /** 图文被阅读数 */

        int recordOfGraphic = graphicPushInterface.getRecordCountOfGraphic(serviceId, Long.parseLong(newsId));
        if (-1 == recordOfGraphic)
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1022, "");

        logger.debug("获取图文阅读次数,recordOfGraphic:{}", recordOfGraphic);
        return ResponsePackUtil.buildPack("0000", recordOfGraphic);
    }
}
