package com.royasoft.vwt.cag.service;

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
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.AffiliationInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.AffiliationVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartmentsInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartmentsVo;
import com.royasoft.vwt.soa.uic.member.api.interfaces.PersonInfoLeaderSearchInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.PersonInfoLeaderSearchVo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * ims通讯录模块
 *
 * @Author:jiangft
 * @Since:2016年6月04日
 */
@Scope("prototype")
@Service
public class ImsAdressBookService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ImsAdressBookService.class);
    /** 包含链接信息与报文信息的packet **/
    private QueuePacket queue_packet = null;

    /** 包含请求以及头信息报文内容 **/
    private Object msg = null;

    /** 客户端链接 **/
    private Channel channel = null;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private AffiliationInterface affiliationInterface;

    @Autowired
    private DepartmentsInterface departmentsInterface;

    @Autowired
    private PersonInfoLeaderSearchInterface personInfoLeaderSearchInterface;

    @Override
    public void run() {
        while (true) {
            try {
                queue_packet = ServicesQueue.ims_queue.take();// 获取队列处理数据
                msg = queue_packet.getMsg();// 获取请求信息
                channel = queue_packet.getChannel();// 获取连接
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    String function_id = queue_packet.getFunction_id();
                    String user_id = ""; // 获取用户ID
                    String request_body = queue_packet.getRequest_body();// 获取参数实体
                    String tel_number = queue_packet.getTel_number();
                    logger.debug("IMS通讯录业务模块(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id, request_body);
                    /**************************** 业务逻辑处理 *****************************************/
                    String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果

                    switch (function_id) {
                        case FunctionIdConstant.GETAFFILIATION:
                            resInfo = getAffiliationList(request_body);
                            break;
                        case FunctionIdConstant.GETDEPARTMENTS:
                            resInfo = getDepartmentsList(request_body);
                            break;
                        case FunctionIdConstant.GETPERSONINFOLEADERSEARCHLIST:
                            resInfo = queryPersoninfoleaderSearch(request_body);
                            break;
                        case FunctionIdConstant.GETPERSONINFOLEADERSEARCH:
                            resInfo = getPersoninfoleadersearch(request_body);
                            break;

                        default:
                            break;
                    }
                    logger.debug("IMS通讯录业务模块(响应),function_id:{},user_id:{},request_body:{},resInfo:{}", function_id, user_id, request_body, resInfo);
                    ResponsePackUtil.cagHttpResponseH5(channel, resInfo);
                    String responseStatus = ResponsePackUtil.getResCode(resInfo);
                    if (null != responseStatus && !"".equals(responseStatus)) {
                        operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id, request_body, "", responseStatus);
                    }
                    continue;
                }
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } catch (Exception e) {
                logger.error("IMS通讯录业务模块异常", e);
                ResponsePackUtil.cagHttpResponseH5(channel, ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
            } finally {
            	//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
            }

        }

    }

    /**
     * 查询IMS通讯录集团列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getAffiliationList(String requestBody) {
        logger.debug("获取集团列表,requestBody:{}", requestBody);

        Map<String, Object> conditions = new HashMap<String, Object>();

        List<AffiliationVo> list = affiliationInterface.findAllAffiliation(conditions, null);

        if (null == list) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2801, "");
        }
        logger.debug("集团列表list.size:{}", list.size());

        return ResponsePackUtil.buildPack("0000", list);
    }

    /**
     * 查询IMS通讯录部门列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public String getDepartmentsList(String requestBody) {
        logger.debug("获取部门列表,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String affiliationUri = trim(requestJson.getString("affiliationUri"));
        logger.debug("集团affiliationUri:{}", affiliationUri);
        String departmentsUri = trim(requestJson.getString("departmentsUri"));
        logger.debug("departmentsUri:{}", departmentsUri);
        /** 校验参数 */
        if (affiliationUri.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2802, "");
        }

        Map<String, Object> conditions = new HashMap<String, Object>();
        if (!affiliationUri.isEmpty()) {
            conditions.put("EQ_affiliationUri", affiliationUri);
        }

        List<DepartmentsVo> list = departmentsInterface.findAllDepartments(conditions, null);

        if (null == list) {
            list = new ArrayList<DepartmentsVo>();
        }
        logger.debug("部门列表list.size:{}", list.size());
        List<PersonInfoLeaderSearchVo> personList = new ArrayList<PersonInfoLeaderSearchVo>();
        if (!departmentsUri.isEmpty()) {
            personList = getPersoninfoleadersearchList(departmentsUri);
        }
        logger.debug("人员列表list.size:{}", personList.size());

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("department", list);
        model.put("person", personList);
        return ResponsePackUtil.buildPack("0000", model);
    }

    /**
     * 查询IMS通讯录人员列表
     * 
     * @param requestBody
     * @param userId
     * @return
     * @author Jiangft 2016年5月4日
     */
    public List<PersonInfoLeaderSearchVo> getPersoninfoleadersearchList(String departmentsUri) {

        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_departmentsUri", departmentsUri);
        List<PersonInfoLeaderSearchVo> list = personInfoLeaderSearchInterface.findAllPersonInfoLeaderSearch(conditions, null);
        if (list == null) {
            return new ArrayList<PersonInfoLeaderSearchVo>();
        }
        return list;

    }

    /**
     * 根据姓名查询人员
     * 
     * @param requestBody
     * @return
     * @author Jiangft 2016年6月23日
     */
    public String queryPersoninfoleaderSearch(String requestBody) {
        logger.debug("根据姓名查询人员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);
        String affiliationUri = trim(requestJson.getString("affiliationUri"));
        String personName = trim(requestJson.getString("personName"));
        Map<String, Object> conditions = new HashMap<String, Object>();
        logger.debug("根据姓名查询人员,affiliationUri:{},personName:{}", affiliationUri, personName);
        if (affiliationUri.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2803, "");
        }
        conditions.put("EQ_affiliationUri", affiliationUri);
        conditions.put("LIKE_personName", personName);
        List<PersonInfoLeaderSearchVo> list = personInfoLeaderSearchInterface.findAllPersonInfoLeaderSearch(conditions, null);
        if (list == null) {
            list = new ArrayList<PersonInfoLeaderSearchVo>();
        }
        return ResponsePackUtil.buildPack("0000", list);

    }

    /**
     * 获取单个人员
     * 
     * @param requestBody
     * @param userId
     * @author Jiangft 2016年5月20日
     */
    public String getPersoninfoleadersearch(String requestBody) {
        logger.debug("获取单个人员,requestBody:{}", requestBody);
        JSONObject requestJson = JSONObject.parseObject(requestBody);

        String uri = trim(requestJson.getString("uri"));
        logger.debug("uri:{}", uri);
        /** 校验参数 */
        if (uri.isEmpty()) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2804, "");
        }
        PersonInfoLeaderSearchVo vo = personInfoLeaderSearchInterface.findPersonInfoLeaderSearchById(uri);
        Map<String, Object> map1 = new HashMap<>();
        if (vo == null) {
            logger.error("获取单个人员为空，uri:{}", uri);
        } else {

            String trackerAddr = ParamConfig.file_server_url;
            map1.put("uri", trim(vo.getUri()));
            map1.put("departmentsUri", trim(vo.getDepartmentsUri()));
            map1.put("personName", trim(vo.getPersonName()));
            map1.put("serialno", trim(vo.getSerialno()));
            map1.put("employeeNum", trim(vo.getEmployeeNum()));
            map1.put("officeTelNumDef", trim(vo.getOfficeTelNumDef()));
            map1.put("mobileTel", trim(vo.getMobileTel()));
            map1.put("workAddress", trim(vo.getWorkAddress()));
            map1.put("shortSpelling", trim(vo.getShortSpelling()));
            map1.put("position", trim(vo.getPosition()));
            map1.put("affiliation", trim(vo.getAffiliation()));
            map1.put("department", trim(vo.getDepartment()));
            map1.put("photoUrl", vo.getPhotoUrl() == null ? "" : trackerAddr + trim(vo.getPhotoUrl()));
            map1.put("affiliationUri", trim(vo.getAffiliationUri()));

        }
        return ResponsePackUtil.buildPack("0000", map1);
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
