/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.cag.constant.FunctionIdConstant;
import com.royasoft.vwt.cag.constant.ResponseInfoConstant;
import com.royasoft.vwt.cag.packet.QueuePacket;
import com.royasoft.vwt.cag.queue.ServicesQueue;
import com.royasoft.vwt.cag.util.MemberInfoUtil;
import com.royasoft.vwt.cag.util.ResponsePackUtil;
import com.royasoft.vwt.cag.util.StringUtils;
import com.royasoft.vwt.cag.vo.MyContactVo;
import com.royasoft.vwt.common.security.AESUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.sundry.addresscheck.api.interfaces.AddressCheckInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.vo.ClientUserVO;
import com.royasoft.vwt.soa.uic.contact.api.interfaces.ContactGroupInterface;
import com.royasoft.vwt.soa.uic.contact.api.interfaces.ContactInterface;
import com.royasoft.vwt.soa.uic.contact.api.vo.ContactGroupVO;
import com.royasoft.vwt.soa.uic.contact.api.vo.ContactVo;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.vo.CorpCustomVO;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * sqlite批量更新业务处理类
 *
 * @Author:MB
 * @Since:2015年9月15日
 */
@Scope("prototype")
@Component
public class SqliteUpdateService implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(SqliteUpdateService.class);

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	@Autowired
	private DatabaseInterface databaseInterface;

	@Autowired
	private CorpInterface corpInterface;
	@Autowired
	private DepartMentInterface departMentInterface;
	@Autowired
	private MemberInfoUtil memberInfoUtil;

	@Autowired
	private ClientUserInterface clientUserInterface;

	@Autowired
	private ContactInterface contactInterface;

	@Autowired
	private ContactGroupInterface contactGroupInterface;

	@Autowired
	private AddressCheckInterface addressCheckInterface;
	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private CorpCustomInterface corpCustomInterface;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** 不分页情况下调用分页方法的pageIndex */
	private final static int allPageIndex = 1;
	/** 不分页情况下调用分页方法的pageSize */
	private final static int allPageSize = 1000000;

	@Override
	public void run() {
		while (true) {
			try {
				queue_packet = ServicesQueue.address_queue.take();// 获取队列处理数据
				msg = queue_packet.getMsg();// 获取请求信息
				channel = queue_packet.getChannel();// 获取连接
				if (msg instanceof HttpRequest) {
					HttpRequest request = (HttpRequest) msg;
					String function_id = queue_packet.getFunction_id(); // 获取功能ID
					String user_id = queue_packet.getUser_id(); // 获取用户ID
					String request_body = queue_packet.getRequest_body();// 获取参数实体
					String tel_number = queue_packet.getTel_number();
					logger.debug("sqlite批量更新业务处理类(入口),function_id:{},user_id:{},request_body:{}", function_id, user_id,
							request_body);
					/***************************** 业务逻辑处理 *********************************************/

					String resInfo = ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1008, "");// 响应结果
					switch (function_id) {
					case FunctionIdConstant.ADDRESSCOUNT:
						resInfo = getSqliteUpdateCount(request_body, user_id);
						break;
					case FunctionIdConstant.ADDRESSALL:
						resInfo = getSqliteAllData(request_body, user_id);
						break;
					case FunctionIdConstant.ORGPAGE:
						resInfo = getSqlitePartUpdateByPage(request_body, user_id);
						break;
					case FunctionIdConstant.MEMBERPAGE:
						resInfo = getSqliteMemberUpdateByPage(request_body, user_id);
						break;
					case FunctionIdConstant.GARBAGEDEAL:
						resInfo = repairGarbageData(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT:
						resInfo = getMyContact(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_ADD:
						resInfo = saveMyContact(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_DELETE:
						resInfo = deleteMyContact(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_GROUP:
						resInfo = getMyContactGroup(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_GROUP_ADD:
						resInfo = saveMyContactGroup(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_GROUP_DELETE:
						resInfo = deleteMyContactGroup(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_GROUP_MODIFY:
						resInfo = modifyMyContactGroup(request_body, user_id);
						break;
					case FunctionIdConstant.MY_CONTACT_GROUP_UPDATE:
						resInfo = updateGroup(request_body, user_id);
						break;
					default:
						break;
					}
					logger.debug("sqlite批量更新业务处理类(响应),function_id:{},user_id:{},request_body:{},resInfo:{}",
							function_id, user_id, request_body,
							null == resInfo || resInfo.length() < 300 ? resInfo : resInfo.substring(0, 300));
					ResponsePackUtil.cagHttpResponse(channel, resInfo);
					String responseStatus = ResponsePackUtil.getResCode(resInfo);
					if (null != responseStatus && !"".equals(responseStatus))
						operationLogService.saveOperationLogNew(channel, request, user_id, tel_number, function_id,
								request_body, "", responseStatus);
					continue;
				}
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} catch (Exception e) {
				logger.error("sqlite批量更新业务处理类异常", e);
				// 响应客户端异常
				ResponsePackUtil.cagHttpResponse(channel,
						ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, ""));
			} finally {
				//2017/01/08 增加netty主动释放内存方法
                while(!ReferenceCountUtil.release(msg)){
                    //自动释放netty计数器
                }
			}
		}
	}

	/**
	 * 获取通讯录更新数量以及通讯录删除id(包括部门和人员)【50001】
	 * 
	 * @param requestBody
	 * @param userKey
	 * @return
	 * @Description:
	 */
	public String getSqliteUpdateCount(String requestBody, String userId) {
		logger.debug("获取通讯录更新数量,requestBody:{},userId:{}", requestBody, userId);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 手机号码 */
		String telNum = requestJson.getString("telNum");

		JSONArray corpAddressInfo = requestJson.getJSONArray("corpAddressInfo");

		/** 本地企业id */
		String corpIds = requestJson.getString("corpIds");

		logger.debug("获取通讯录更新数量(解析body),telNum:{},corpAddressInfo:{},corpIds:{}", telNum,
				corpAddressInfo.toJSONString(), corpIds);
		/** 校验参数 */
		if (null == telNum || "".equals(telNum) || null == corpAddressInfo)
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");

		List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(telNum);
		// List<ClientUserVO> clientUserVOs =
		// clientUserInterface.findByTelNum(telNum);
		if (null == memberInfoVOs || memberInfoVOs.isEmpty())
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011, "");

		List<String> corpIdListBefore = getCorpIdByTelnum(memberInfoVOs);

		logger.debug("获取通讯录更新数量,corpIdList:{}", JSON.toJSONString(corpIdListBefore));

		if (null == corpIdListBefore || corpIdListBefore.isEmpty())
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1011, "");

		List<String> corpIdList = filterList(telNum, corpIdListBefore);

		JSONObject resJson = findCheckInfo(corpIds, corpIdList, telNum);
		resJson.put("corpUpdateInfo", getCorpAddressUpdate(corpAddressInfo, corpIdList));

		/** 压缩加密返回body */
		// String userKey = getUserKeyByTelNum(userId);
		// if (null == userKey)
		// return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
		String resBody = ResponsePackUtil.compressEncryptData(resJson.toJSONString(), userId);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	private List<String> filterList(String telNum, List<String> corpIdList) {
		List<String> set = new ArrayList<String>();
		for (String str : corpIdList) {
			if (null != str && !"".equals(str) && !"null".equals(str))
				set.add(str);
			else
				logger.error("获取通讯录更新数量以及通讯录删除id(用户无企业id),telNum:{},corpId:{}", telNum, str);
		}
		return set;
	}

	/**
	 * 获取一人多职更新数据
	 * 
	 * @param corpAddressInfo
	 * @return
	 * @Description:
	 */
	private JSONArray getCorpAddressUpdate(JSONArray corpAddressInfo, List<String> corpIdSet) {
		JSONArray resJsonArray = new JSONArray();
		for (int i = 0; i < corpAddressInfo.size(); i++) {
			JSONObject corpJson = corpAddressInfo.getJSONObject(i);
			if (null == corpJson)
				continue;
			String corpId = corpJson.getString("corpId");
			if (null == corpId || "".equals(corpId) || "null".equals(corpId))
				continue;
			if (!corpIdSet.contains(corpId))
				continue;
			/** 人员表最后更新时间 */
			String memberLastTime = corpJson.getString("memberLastTime");
			/** 部门表最后更新时间 */
			String orgLastTime = corpJson.getString("orgLastTime");

			/** 人员表最后删除时间 */
			String memberDeleteTime = corpJson.getString("memberDeleteTime");
			/** 部门表最后删除时间 */
			String orgDeleteTime = corpJson.getString("orgDeleteTime");
			/** 最新激活时间 */
			String activateTime = corpJson.getString("activateTime");

			/** 客户端人员表总数 */
			String allMemberCount = corpJson.getString("allMemberCount");
			/** 客户端人员表所有id求和 */
			// String allMemberIdSum = corpJson.getString("allMemberIdSum");
			/** 客户端部门表总数 */
			String allOrgCount = corpJson.getString("allOrgCount");
			/** 客户端部门表所有id求和 */
			// String allOrgIdSum = corpJson.getString("allOrgIdSum");

			/** 格式化更新时间 */
			memberLastTime = formatTimeString(memberLastTime);
			orgLastTime = formatTimeString(orgLastTime);
			memberDeleteTime = formatTimeString(memberDeleteTime);
			orgDeleteTime = formatTimeString(orgDeleteTime);
			logger.debug("格式化更新时间,memberLastTime:{},orgLastTime:{},memberDeleteTime:{},orgDeleteTime:{}",
					memberLastTime, orgLastTime, memberDeleteTime, orgDeleteTime);
			boolean isFirst = false;
			if (null == memberLastTime && null == orgLastTime && null == memberDeleteTime && null == orgDeleteTime)
				isFirst = true;

			/** 获取通讯录更新数量 */
			JSONObject countJson = findUpdateCount(memberLastTime, orgLastTime, corpId);
			/** 获取通讯录删除id(包括部门和人员) */
			JSONObject bodyJson = findSqliteHistory(memberDeleteTime, orgDeleteTime, corpId, countJson, isFirst);
			/** 获取通讯录删除id(包括部门和人员) */
			JSONObject activeJson = findActiveInfo(activateTime, corpId, bodyJson);

			boolean errorDataStatus = false;
			// if (null != memberLastTime && !"".equals(memberLastTime))
			// errorDataStatus =
			// addressCheckInterface.validateMemberData(corpId, memberLastTime,
			// orgLastTime, formatString2Int(allMemberCount),
			// formatString2Int(allOrgCount));

			activeJson.put("errorDataStatus", errorDataStatus);
			activeJson.put("corpId", corpId);
			logger.debug("获取通讯录更新数量(返回信息),corpJson:{},activeJson:{}", corpJson.toJSONString(),
					activeJson.toJSONString());
			resJsonArray.add(activeJson);
			corpIdSet.remove(corpId);
		}

		for (String newCorpId : corpIdSet) {
			/** 获取通讯录更新数量 */
			JSONObject countJson = findUpdateCount(null, null, newCorpId);
			/** 获取通讯录删除id(包括部门和人员) */
			JSONObject bodyJson = findSqliteHistory(null, null, newCorpId, countJson, true);
			/** 获取通讯录删除id(包括部门和人员) */
			JSONObject activeJson = findActiveInfo(null, newCorpId, bodyJson);

			activeJson.put("errorDataStatus", false);
			activeJson.put("corpId", newCorpId);
			logger.debug("获取通讯录更新数量(返回信息),newCorpId:{},activeJson:{}", newCorpId, activeJson.toJSONString());
			resJsonArray.add(activeJson);
		}
		return resJsonArray;
	}

	/**
	 * 分页获取通讯录部门更新【50002】
	 * 
	 * @param requestBody
	 * @param userKey
	 * @return
	 * @Description:
	 */
	public String getSqlitePartUpdateByPage(String requestBody, String userId) {
		logger.debug("分页获取通讯录部门更新,requestBody:{},userId:{}", requestBody, userId);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 手机号码 */
		String telNum = requestJson.getString("telNum");

		String corpId = requestJson.getString("corpId");
		/** 部门表最后更新时间 */
		String orgLastTime = requestJson.getString("orgLastTime");
		/** 页码 */
		String pageIndex = requestJson.getString("pageIndex");
		/** 每页长度 */
		String pageSize = requestJson.getString("pageSize");

		logger.debug("分页获取通讯录部门更新(解析body),telNum:{},orgLastTime:{},pageIndex:{},pageSize:{}", telNum, orgLastTime,
				pageIndex, pageSize);

		/** 格式化更新时间 */
		orgLastTime = formatTimeString(orgLastTime);

		/** 校验参数 */
		if (!valicateParams(pageSize, pageIndex, telNum, corpId))
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");

		/** 获取通讯录部门更新信息 */
		List<String> partStrList = getPartUpdateOfPage(corpId, orgLastTime, Integer.valueOf(pageIndex),
				Integer.valueOf(pageSize));

		logger.debug("分页获取通讯录部门更新(获取部门表更新信息),telNum:{},orgLastTime:{},pageIndex:{},pageSize:{},partStrList:{}", telNum,
				orgLastTime, pageIndex, pageSize, partStrList.size());

		JSONObject bodyJson = new JSONObject();
		bodyJson.put("enterpriseVOList", partStrList);
		/** 压缩加密返回body */
		// String userKey = getUserKeyByTelNum(userId);
		// if (null == userKey)
		// return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
		String resBody = ResponsePackUtil.compressEncryptData(JSONObject.toJSONString(bodyJson), userId);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	/**
	 * 分页获取通讯录人员更新【50003】
	 * 
	 * @param requestBody
	 * @param userKey
	 * @return
	 * @Description:
	 */
	public String getSqliteMemberUpdateByPage(String requestBody, String userId) {
		logger.debug("分页获取通讯录人员更新,requestBody:{},userId:{}", requestBody, userId);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 手机号码 */
		String telNum = requestJson.getString("telNum");

		String corpId = requestJson.getString("corpId");
		/** 人员表最后更新时间 */
		String memberLastTime = requestJson.getString("memberLastTime");
		/** 页码 */
		String pageIndex = requestJson.getString("pageIndex");
		/** 每页长度 */
		String pageSize = requestJson.getString("pageSize");

		logger.debug("分页获取通讯录人员更新(解析body),telNum:{},memberLastTime:{},pageIndex:{},pageSize:{}", telNum, memberLastTime,
				pageIndex, pageSize);

		/** 格式化更新时间 */
		memberLastTime = formatTimeString(memberLastTime);

		/** 校验参数 */
		if (!valicateParams(pageSize, pageIndex, telNum, corpId))
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");

		/** 获取人员表更新信息 */
		List<String> memberStrList = getMemberUpdateOfPage(corpId, memberLastTime, Integer.valueOf(pageIndex),
				Integer.valueOf(pageSize));

		logger.debug("分页获取通讯录人员更新(获取人员表更新信息),telNum:{},memberLastTime:{},pageIndex:{},pageSize:{},memberStrList:{}",
				telNum, memberLastTime, pageIndex, pageSize, memberStrList.size());

		JSONObject bodyJson = new JSONObject();
		bodyJson.put("memberInfoVOList", memberStrList);
		/** 压缩加密返回body */
		// String userKey = getUserKeyByTelNum(userId);
		// if (null == userKey)
		// return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
		String resBody = ResponsePackUtil.compressEncryptData(JSONObject.toJSONString(bodyJson), userId);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	/**
	 * 全量获取通讯录更新数据【50004】
	 * 
	 * @param requestBody
	 * @param userKey
	 * @return
	 * @Description:
	 */
	public String getSqliteAllData(String requestBody, String userId) {
		logger.debug("全量获取通讯录更新数据(包括部门和人员),requestBody:{},userId:{}", requestBody, userId);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 手机号码 */
		String telNum = requestJson.getString("telNum");

		String corpId = requestJson.getString("corpId");
		/** 人员表最后更新时间 */
		String memberLastTime = requestJson.getString("memberLastTime");
		/** 部门表最后更新时间 */
		String orgLastTime = requestJson.getString("orgLastTime");

		logger.debug("全量获取通讯录更新数据(包括部门和人员)(解析body),telNum:{},memberLastTime:{},orgLastTime:{}", telNum, memberLastTime,
				orgLastTime);

		memberLastTime = formatTimeString(memberLastTime);
		orgLastTime = formatTimeString(orgLastTime);

		/** 校验参数 */
		if (null == telNum || "".equals(telNum) || null == corpId)
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");

		/** 将服务返回信息转化(提高客户端解析效率) */
		List<String> memberStrList = getMemberUpdateOfPage(corpId, memberLastTime, allPageIndex, allPageSize);
		List<String> partStrList = getPartUpdateOfPage(corpId, orgLastTime, allPageIndex, allPageSize);

		logger.debug("全量获取通讯录更新数据(包括部门和人员),telNum:{},memberUpdateList:{},partUpdateList:{}", telNum,
				memberStrList.size(), partStrList.size());

		JSONObject bodyJson = new JSONObject();
		bodyJson.put("memberUpdateList", memberStrList);
		bodyJson.put("partUpdateList", partStrList);

		/** 压缩加密返回body */
		// String userKey = getUserKeyByTelNum(userId);
		// if (null == userKey)
		// return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");
		String resBody = ResponsePackUtil.compressEncryptData(JSON.toJSONString(bodyJson), userId);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	/**
	 * 修复历史垃圾数据【5005】
	 * 
	 * @param requestBody
	 * @param userKey
	 * @return
	 * @Description:
	 */
	public String repairGarbageData(String requestBody, String userId) {
		logger.debug("修复历史垃圾数据,requestBody:{},userId:{}", null == requestBody ? null : requestBody.length(), userId);

		// String userKey = getUserKeyByTelNum(userId);
		// if (null == userKey)
		// return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1003, "");

		requestBody = uncompressAndDecode(requestBody, userId);

		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 手机号码 */
		String telNum = requestJson.getString("telNum");

		String corpId = requestJson.getString("corpId");
		/** 所有客户端员工表数据 */
		String allMemberInfo = requestJson.getString("allMemberInfo");
		/** 所有客户端部门表数据 */
		String allOrgInfo = requestJson.getString("allOrgInfo");
		/** 客户端员工表最新时间 */
		String memberTime = requestJson.getString("memberTime");
		/** 客户端部门表最新时间 */
		String orgTime = requestJson.getString("orgTime");

		logger.debug("修复历史垃圾数据(解析body),telNum:{},memberTime:{},orgTime:{}", telNum, memberTime, orgTime);

		Date memberDate = formatTime2Date(memberTime);
		Date orgDate = formatTime2Date(orgTime);

		/** 校验参数 */
		if (null == telNum || "".equals(telNum) || null == corpId)
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1010, "");

		Map<String, MemberInfoVO> serverMember = null;
		if (null != memberDate)
			serverMember = memberInfoUtil.findByOperationTimeAndCorpId(corpId, memberDate);// memberInfoInterface.findByOperationTimeAndCorpId(corpId,
																							// memberDate);
		Map<String, DepartMentVO> serverEnterprise = null;
		if (null != orgDate)
			serverEnterprise = departMentInterface.findEnterByOperationTimeAndCorpId(corpId, orgDate);

		Map<String, List<String>> memberGarbageData = getMemberGarbageData(allMemberInfo, serverMember);

		Map<String, List<String>> orgGarbageData = getOrgGarbageData(allOrgInfo, serverEnterprise);

		JSONObject bodyJson = new JSONObject();

		if (null == memberGarbageData) {
			bodyJson.put("memberDeleteInfo", "");
			bodyJson.put("memberInfoVOList", "");
		} else {
			bodyJson.put("memberDeleteInfo", memberGarbageData.get("deleteMember"));
			bodyJson.put("memberInfoVOList", memberGarbageData.get("updateMember"));
		}

		if (null == orgGarbageData) {
			bodyJson.put("orgDeleteInfo", "");
			bodyJson.put("enterpriseVOList", "");
		} else {
			bodyJson.put("orgDeleteInfo", orgGarbageData.get("deleteOrg"));
			bodyJson.put("enterpriseVOList", orgGarbageData.get("updateOrg"));
		}

		logger.debug("修复历史垃圾数据(对比结果),telNum:{},corpId:{},memberGarbageData:{},orgGarbageData:{}", telNum, corpId,
				memberGarbageData.size(), orgGarbageData.size());
		/** 压缩加密返回body */
		String resBody = ResponsePackUtil.compressEncryptData(JSON.toJSONString(bodyJson), userId);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	/**
	 * 获取人员历史问题数据
	 * 
	 * @param allMemberInfo
	 * @param serverMember
	 * @return
	 * @Description:
	 */
	private Map<String, List<String>> getMemberGarbageData(String allMemberInfo,
			Map<String, MemberInfoVO> serverMember) {
		logger.debug("获取人员历史问题数据,allMemberInfo:{},serverMember:{}",
				null == allMemberInfo ? null : allMemberInfo.length(),
				null == serverMember ? null : serverMember.size());
		try {
			if (null == allMemberInfo)
				return null;
			if (null == serverMember)
				return null;
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			List<MemberInfoVO> updateMember = new ArrayList<MemberInfoVO>();
			List<String> deleteMember = new ArrayList<String>();
			JSONArray jsonArray = JSONArray.parseArray(allMemberInfo);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String operation_time = jsonObject.getString("operation_time");
				if (serverMember.containsKey(id)) {
					MemberInfoVO memberInfoVO = serverMember.get(id);
					Date optTime = memberInfoVO.getOperationTime();
					if (null == operation_time || "".equals(operation_time) || null == optTime
							|| operation_time.equals(optTime.getTime()))
						updateMember.add(memberInfoVO);
					serverMember.remove(id);
				} else {
					deleteMember.add(id);
				}
			}
			Iterator<Map.Entry<String, MemberInfoVO>> it = serverMember.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, MemberInfoVO> entry = it.next();
				updateMember.add(entry.getValue());
			}

			List<String> updateMemberList = getMemberStrList(updateMember);
			if (updateMemberList.size() > 9000)
				map.put("updateMember", updateMemberList.subList(0, 9000));
			else
				map.put("updateMember", updateMemberList);

			map.put("deleteMember", deleteMember);
			logger.debug("获取人员历史问题数据(返回信息),updateMemberList:{},deleteMember:{}", updateMemberList.size(),
					deleteMember.size());
			return map;
		} catch (Exception e) {
			logger.error("获取人员历史问题数据异常,allMemberInfo:{},serverMember:{},exception:{}", allMemberInfo.length(),
					serverMember.size(), e);
			return null;
		}

	}

	/**
	 * 获取部门历史问题数据
	 * 
	 * @param allOrgInfo
	 * @param serverOrg
	 * @return
	 * @Description:
	 */
	private Map<String, List<String>> getOrgGarbageData(String allOrgInfo, Map<String, DepartMentVO> serverOrg) {
		logger.debug("获取部门历史问题数据,allOrgInfo:{},serverOrg:{}", allOrgInfo.length(), serverOrg.size());
		try {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			List<DepartMentVO> updateOrg = new ArrayList<DepartMentVO>();
			List<String> deleteOrg = new ArrayList<String>();
			JSONArray jsonArray = JSONArray.parseArray(allOrgInfo);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String act_time = jsonObject.getString("act_time");
				if (serverOrg.containsKey(id)) {
					DepartMentVO enterpriseVO = serverOrg.get(id);
					Date actTime = enterpriseVO.getActTime();
					if (null == act_time || "".equals(act_time) || null == actTime
							|| act_time.equals(actTime.getTime()))
						updateOrg.add(enterpriseVO);
					serverOrg.remove(id);
				} else {
					deleteOrg.add(id);
				}
			}
			Iterator<Map.Entry<String, DepartMentVO>> it = serverOrg.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, DepartMentVO> entry = it.next();
				updateOrg.add(entry.getValue());
			}

			List<String> updateOrgList = getPartStrList(updateOrg);
			if (updateOrgList.size() > 1000)
				map.put("updateOrg", updateOrgList.subList(0, 1000));
			else
				map.put("updateOrg", updateOrgList);

			map.put("deleteOrg", deleteOrg);
			logger.debug("获取部门历史问题数据(返回信息),updateOrgList:{},deleteOrg:{}", updateOrgList.size(), deleteOrg.size());
			return map;
		} catch (Exception e) {
			logger.error("获取部门历史问题数据异常,exception:{}", e);
			return null;
		}

	}

	/**
	 * 获取通讯录更新数量
	 * 
	 * @param memberLastTime
	 * @param orgLastTime
	 * @param corpId
	 * @return
	 * @Description:
	 */
	private JSONObject findUpdateCount(String memberLastTime, String orgLastTime, String corpId) {
		/** 获取员工表更新信息 */
		long memberCount = memberInfoUtil.getMemberInfoCount(memberLastTime, corpId);// memberInfoInterface.getMemberInfoCount(memberLastTime,
																						// corpId);
		/** 获取部门表更新信息 */
		long orgCount = departMentInterface.getDepartMentCount(orgLastTime, corpId);

		logger.debug("获取通讯录更新数量(获取更新数量),corpId:{},memberCount:{},orgCount:{}", corpId, memberCount, orgCount);

		JSONObject bodyJson = new JSONObject();
		bodyJson.put("memberCount", memberCount);
		bodyJson.put("orgCount", orgCount);
		return bodyJson;
	}

	/**
	 * 通讯录删除id(包括部门和人员)
	 * 
	 * @param memberLastTime
	 * @param orgLastTime
	 * @param corpId
	 * @return
	 * @Description:
	 */
	@SuppressWarnings("unchecked")
	private JSONObject findSqliteHistory(String memberDeleteTime, String orgDeleteTime, String corpId,
			JSONObject countJosn, boolean isFirst) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

		conditions.put("EQ_corpId", corpId);

		sortMap.put("deleteTime", true);

		List<String> memberHistoryList = new ArrayList<String>();
		List<String> partHistoryList = new ArrayList<String>();
		if (!isFirst) {
			if (null != memberDeleteTime)
				conditions.put("start_time_deleteTime", memberDeleteTime);
			/** 获取员工表历史信息 */
			Map<String, Object> memberHistoryMap = memberInfoUtil.findMemberInfoHisAllByPage(1, 1000000, conditions,
					sortMap);// memberInfoInterface.findMemberInfoHisAllByPage(1,
								// 1000000, conditions,
								// sortMap);
			if (null != memberHistoryMap && null != memberHistoryMap.get("content")
					&& !"".equals(memberHistoryMap.get("content")))
				memberHistoryList = (List<String>) memberHistoryMap.get("content");
		}

		Date memberTime = memberInfoUtil.findMaxDeleteTime(corpId);// memberInfoInterface.findMaxDeleteTime(corpId);
		if (null == memberTime) {
			countJosn.put("memberMaxDelTime", "");
		} else {
			countJosn.put("memberMaxDelTime", memberTime.getTime());
		}

		if (!isFirst) {
			if (null != orgDeleteTime)
				conditions.put("start_time_deleteTime", orgDeleteTime);
			/** 获取部门表历史信息 */
			Map<String, Object> partHistoryMap = departMentInterface.findAllDepartMentHisByPage(1, 1000000, conditions,
					sortMap);
			if (null != partHistoryMap && null != partHistoryMap.get("content")
					&& !"".equals(partHistoryMap.get("content")))
				partHistoryList = (List<String>) partHistoryMap.get("content");
		}

		Date orgTime = departMentInterface.findMaxDeleteTime(corpId);

		if (null == orgTime) {
			countJosn.put("orgMaxDelTime", "");
		} else {
			countJosn.put("orgMaxDelTime", orgTime.getTime());
		}

		logger.debug("获取通讯录删除id(包括部门和人员),corpId:{},memberHistoryList:{},partHistoryList:{}", corpId,
				JSON.toJSONString(memberHistoryList), JSON.toJSONString(partHistoryList));

		countJosn.put("memberHistoryList", memberHistoryList);
		countJosn.put("partHistoryList", partHistoryList);
		return countJosn;
	}

	/**
	 * 获取该企业激活用户增量更新数据
	 * 
	 * @param memberLastTime
	 * @param orgLastTime
	 * @param corpId
	 * @return
	 * @Description:
	 */
	private JSONObject findActiveInfo(String activeTime, String corpId, JSONObject bodyJson) {

		Date thisDate = null;
		if (null == activeTime || "".equals(activeTime))
			thisDate = new Date(0L);
		else
			thisDate = new Date(Long.valueOf(activeTime));

		List<Long> activeList = new ArrayList<Long>();
		if (null != activeTime && !"".equals(activeTime) && activeTime != "0") {
			/** 获取激活增量信息 */
			activeList = clientUserInterface.findActiveIdsByCorpIdAndAfterCreateTime(corpId, thisDate);
		}
		Long activeTimeMax = clientUserInterface.findMaxTimeByCorpId(corpId);
		if (null == activeTimeMax) {
			bodyJson.put("activateTimeMax", "");
		} else {
			bodyJson.put("activateTimeMax", activeTimeMax);
		}

		logger.debug("获取该企业激活用户增量更新数据,corpId:{},activeTime:{},activeTimeMax:{},activeList:{}", corpId, activeTime,
				activeTimeMax, JSON.toJSONString(activeList));

		bodyJson.put("activateTimeList", activeList);
		return bodyJson;
	}

	/**
	 * 分页获取部门更新信息
	 * 
	 * @param corpId
	 * @param orgLastTime
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @Description:
	 */
	@SuppressWarnings("unchecked")
	private List<String> getPartUpdateOfPage(String corpId, String orgLastTime, int pageIndex, int pageSize) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

		conditions.put("EQ_corpId", corpId);
		if (null != orgLastTime)
			conditions.put("start_time_actTime", orgLastTime);

		sortMap.put("deptId", true);

		/** 获取部门表更新信息 */
		Map<String, Object> partMap = departMentInterface.findAllByPage(pageIndex, pageSize, conditions, sortMap);

		List<DepartMentVO> enterpriseVOs = new ArrayList<DepartMentVO>();
		if (null != partMap.get("content") && !"".equals(partMap.get("content")))
			enterpriseVOs = (List<DepartMentVO>) partMap.get("content");

		/** 将服务返回信息转化(提高客户端解析效率) */
		return getPartStrList(enterpriseVOs);
	}

	/**
	 * 分页获取人员更新信息
	 * 
	 * @param corpId
	 * @param orgLastTime
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @Description:
	 */
	@SuppressWarnings("unchecked")
	private List<String> getMemberUpdateOfPage(String corpId, String memberLastTime, int pageIndex, int pageSize) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		// Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

		conditions.put("EQ_corpId", corpId);
		if (null != memberLastTime)
			conditions.put("start_time_operationTime", memberLastTime);

		// sortMap.put("id", true);

		/** 获取人员表更新信息 */
		Map<String, Object> memberMap = memberInfoUtil.findAddressBookByPage(pageIndex, pageSize, conditions);// memberInfoInterface.findAddressBookByPage(pageIndex,
																												// pageSize,
																												// conditions);

		List<MemberInfoVO> memberInfoVOs = new ArrayList<MemberInfoVO>();
		if (null != memberMap && null != memberMap.get("content") && !"".equals(memberMap.get("content")))
			memberInfoVOs = (List<MemberInfoVO>) memberMap.get("content");

		/** 将服务返回信息转化(提高客户端解析效率) */
		return getMemberStrList(memberInfoVOs);
	}

	/**
	 * 将服务返回list转化（提供客户端解析速度）
	 * 
	 * @param memberUpdateList
	 * @return
	 * @Description:
	 */
	private List<String> getPartStrList(List<DepartMentVO> partUpdateList) {
		List<String> partStrList = new ArrayList<String>();
		String splitStr = ",";
		for (DepartMentVO enterpriseVO : partUpdateList) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(checkStrReturn(enterpriseVO.getDeptId()));
			buffer.append(checkStrReturn(enterpriseVO.getParentDeptNum()));
			buffer.append(checkStrReturn(enterpriseVO.getPartName()));
			buffer.append(checkStrReturn(enterpriseVO.getPartFullName()));

			buffer.append(enterpriseVO.getActTime().getTime() + splitStr);
			buffer.append(checkStrReturn(enterpriseVO.getCorpId()));
			buffer.append(enterpriseVO.getSort() + splitStr);
			buffer.append("''");

			partStrList.add(buffer.toString());
		}
		String firstPart = "1,0,'集团通讯录','','',1388509261000,1,''";
		partStrList.add(firstPart);
		return partStrList;
	}

	/**
	 * 将服务返回list转化（提供客户端解析速度）
	 * 
	 * @param memberUpdateList
	 * @return
	 * @Description:
	 */
	private List<String> getMemberStrList(List<MemberInfoVO> memberUpdateList) {
		List<String> memberStrList = new ArrayList<String>();
		String splitStr = ",";
		for (MemberInfoVO memberInfoVO : memberUpdateList) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(checkStrReturn(memberInfoVO.getMemId()));//
			buffer.append(checkStrReturn(memberInfoVO.getDeptId()));
			buffer.append(checkStrReturn(memberInfoVO.getPartName()));
			buffer.append(checkStrReturn(memberInfoVO.getTelNum()));
			buffer.append(checkStrReturn(memberInfoVO.getMemberName()));
			buffer.append(checkStrReturn(memberInfoVO.getShortNum()));
			buffer.append(checkStrReturn(memberInfoVO.getSpell()));
			buffer.append(checkStrReturn(memberInfoVO.getFirstSpell()));
			buffer.append(memberInfoVO.getIsActive() + splitStr);
			buffer.append(checkStrReturn(memberInfoVO.getDuty()));
			buffer.append(checkStrReturn(memberInfoVO.getSex()));
			buffer.append(checkStrReturn(memberInfoVO.getEmail()));
			buffer.append(checkStrReturn(memberInfoVO.getJobNum()));
			buffer.append(checkStrReturn(memberInfoVO.getAvatar()));
			buffer.append(checkStrReturn(memberInfoVO.getClique()));
			buffer.append(memberInfoVO.getSort() + splitStr);//
			buffer.append(checkStrReturn(memberInfoVO.getReserveField1()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField2()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField3()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField4()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField5()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField6()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField7()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField8()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField9()));
			buffer.append(checkStrReturn(memberInfoVO.getReserveField10()));
			buffer.append(memberInfoVO.getRoleAuth() + splitStr);//
			buffer.append(memberInfoVO.getVisitAuth() + splitStr);//
			buffer.append(checkStrReturn(memberInfoVO.getCorpId()));//
			buffer.append(memberInfoVO.getOperationTime().getTime());//

			memberStrList.add(buffer.toString());
		}
		return memberStrList;
	}

	/**
	 * 校验字符串
	 * 
	 * @param str
	 * @return
	 * @Description:
	 */
	private String checkStrReturn(String str) {
		String splitStr = ",";
		String addStr = "'";
		if (null == str || "".equals(str) || "NULL".equals(str) || "Null".equals(str))
			return addStr + addStr + splitStr;

		return addStr + filterChar(str) + addStr + splitStr;
	}

	/**
	 * 过滤英文单双引号
	 * 
	 * @param str
	 * @return
	 * @Description:
	 */
	private String filterChar(String str) {
		return str.replaceAll("'", "‘").replaceAll("\"", "“");
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
	private boolean valicateParams(String pageSize, String pageIndex, String telNum, String corpId) {
		if (null == pageSize || "".equals(pageSize))
			return false;
		if (null == pageIndex || "".equals(pageIndex))
			return false;
		if (null == telNum || "".equals(telNum))
			return false;
		if (org.springframework.util.StringUtils.isEmpty(corpId))
			return false;
		return true;
	}

	/**
	 * 根据手机号码获取企业id
	 * 
	 * @param telNum
	 * @return
	 * @Description:
	 */
	private List<String> getCorpIdByTelnum(List<MemberInfoVO> memberInfoVOs) {
		List<String> corpIdList = new ArrayList<String>();
		Set<String> corpIdSet = new HashSet<String>();
		for (MemberInfoVO memberInfoVO : memberInfoVOs) {
			corpIdSet.add(memberInfoVO.getCorpId());
		}
		corpIdList.addAll(corpIdSet);
		return corpIdList;
	}

	/**
	 * 将时间戳格式化为时间字符串
	 * 
	 * @param beforeStr
	 * @return
	 * @Description:
	 */
	private String formatTimeString(String beforeStr) {
		if (null == beforeStr || "".equals(beforeStr)) {
			return null;
		} else {
			try {
				return dateFormat.format(Long.valueOf(beforeStr));
			} catch (Exception e) {
				logger.error("将时间戳格式化为时间字符串异常", e);
				return null;
			}
		}
	}

	/**
	 * 将时间戳格式化为Date
	 * 
	 * @param beforeStr
	 * @return
	 * @Description:
	 */
	private Date formatTime2Date(String beforeStr) {
		if (null == beforeStr || "".equals(beforeStr)) {
			return null;
		} else {
			try {
				return new Date(Long.parseLong(beforeStr));
			} catch (Exception e) {
				logger.error("将时间戳格式化为Date异常", e);
				return null;
			}
		}
	}

	/**
	 * 字符串转为Long
	 * 
	 * @param str
	 * @return
	 * @Description:
	 */
	// private Long formatString2Long(String str) {
	// if (null == str || "".equals(str))
	// return 0L;
	// try {
	// return Long.parseLong(str);
	// } catch (Exception e) {
	// logger.error("字符串转为Long异常", e);
	// return 0L;
	// }
	// }

	/**
	 * 字符串转为int
	 * 
	 * @param str
	 * @return
	 * @Description:
	 */
	private int formatString2Int(String str) {
		if (null == str || "".equals(str))
			return 0;
		try {
			return Integer.valueOf(str);
		} catch (Exception e) {
			logger.error("字符串转为int异常", e);
			return 0;
		}
	}

	private String uncompressAndDecode(String str, String aes) {
		try {
			byte[] b = AESUtil.decodeByte(aes, str);
			return StringUtils.uncompressByte(b);
		} catch (Exception e) {
			logger.error("解密解压缩异常", e);
			return null;
		}

	}

	/**
	 * 获取企业校验信息
	 * 
	 * @param corpIds
	 * @param jsonObject
	 * @param corpIdList
	 * @param username
	 * @return
	 * @Description:
	 */
	private JSONObject findCheckInfo(String corpIds, List<String> corpIdList, String username) {
		JSONObject jsonObject = new JSONObject();
		/** 2016年6月7日 由于一人多职其他信息未作校验，故而暂且每次都更新一人多职信息 */
		boolean checkFlag = false;// getCheckFlag(corpIds, corpIdList);
		jsonObject.put("checkFlag", checkFlag);
		if (!checkFlag)
			jsonObject.put("companyList", getMemberInfos(username));
		return jsonObject;
	}

	/**
	 * 判断客户端与服务端企业是否一致
	 * 
	 * @param corpIds
	 * @param corpIdList
	 * @return
	 * @Description:
	 */
	private boolean getCheckFlag(String corpIds, List<String> corpIdList) {
		if (null == corpIds || "".equals(corpIds))
			return false;
		List<String> clientCorpIdList = JSON.parseArray(corpIds, String.class);
		if (null == clientCorpIdList || clientCorpIdList.isEmpty())
			return false;
		if (!clientCorpIdList.containsAll(corpIdList) || clientCorpIdList.size() != corpIdList.size())
			return false;
		return true;
	}

	/**
	 * 获取一人多职信息
	 * 
	 * @param memberInfoVOs
	 * @return
	 * @Description:
	 */
	private JSONArray getMemberInfos(String username) {
		List<MemberInfoVO> memberInfoVOs = memberInfoUtil.findMemberInfosByTelNum(username);// memberInfoInterface.findByTelNum(username);
		logger.debug("获取一人多职信息,memberInfoVOs:{}", memberInfoVOs.size());
		JSONArray jsonArray = new JSONArray();
		try {
			for (MemberInfoVO memberInfoVO : memberInfoVOs) {
				String corpId = memberInfoVO.getCorpId();
				CorpVO corpVO = corpInterface.findById(corpId);
				ClientUserVO clientUserVO = clientUserInterface.findById(memberInfoVO.getMemId());
				CorpCustomVO customVO = corpCustomInterface.findCorpCustomById(corpId);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("companyName", corpVO.getCorpName());
				jsonObject.put("departmentName", memberInfoVO.getPartName());
				jsonObject.put("memberID", memberInfoVO.getMemId());
				jsonObject.put("headPhotoUrl",
						null == clientUserVO || null == clientUserVO.getAvatar() ? "" : clientUserVO.getAvatar());
				jsonObject.put("userName", memberInfoVO.getMemberName());
				jsonObject.put("corpId", corpId);
				jsonObject.put("CLIQUE_ID",
						null == clientUserVO || null == clientUserVO.getClique() ? "" : clientUserVO.getClique());
				jsonObject.put("shortPhoneNumber",
						null == memberInfoVO.getShortNum() ? "" : memberInfoVO.getShortNum());
				jsonObject.put("emailAddr ", null == memberInfoVO.getEmail() ? "" : memberInfoVO.getEmail());
				jsonObject.put("shortName",
						null == customVO || null == customVO.getShortname() ? "" : customVO.getShortname());
				boolean internetManager = false;
				if (null != corpVO.getFromchannel() && !"".equals(corpVO.getFromchannel())
						&& corpVO.getFromchannel() == 7 && corpVO.getCorpMobilephone().equals(memberInfoVO.getTelNum()))
					internetManager = true;
				jsonObject.put("internetManager", internetManager);
				jsonArray.add(jsonObject);
			}
			logger.debug("获取一人多职信息,jsonArray:{}", jsonArray.toJSONString());
		} catch (Exception e) {
			logger.error("获取一人多职信息异常", e);
		}

		return jsonArray;
	}

	/**
	 * 获取个人常用通讯录 【5006】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String getMyContact(String requestBody, String user_id) {
		logger.debug("获取个人常用通讯录数据,requestBody:{},userId:{}", requestBody, user_id);
		// JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 校验参数 */
		if (null == user_id || "".equals(user_id))
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
		// 常用联系人集合
		ArrayList<Serializable> myContactList = new ArrayList<Serializable>();
		ArrayList<Serializable> myGroupList = new ArrayList<Serializable>();
		// 增加常用联系人分组集合
		List<ContactGroupVO> groupVOList = contactGroupInterface.findByFromUserId(user_id);
		logger.debug("获取常用联系人分组,groupVOList:{}", JSON.toJSONString(groupVOList));
		if (null != groupVOList && groupVOList.size() > 0) {
			// 遍历加入联系人分组的信息
			for (ContactGroupVO vo : groupVOList) {
				ContactGroupVO myCVo = new ContactGroupVO();
				myCVo.setGroupid(vo.getGroupid());
				myCVo.setGroupname(vo.getGroupname());
				myCVo.setRankGroupFlag(StringUtils.stringIsNotNull(vo.getRankGroupFlag()) ? vo.getRankGroupFlag() : "");
				myGroupList.add(myCVo);
				logger.debug("获取个人常用通讯录数据,myCVo:{},myGroupList:{}",JSON.toJSONString(myCVo),JSON.toJSONString(myGroupList));
			}
		}
		// 查询用户添加的联系人列表
		List<ContactVo> contactList = contactInterface.findContactByFromUserId(user_id);
		logger.debug("获取个人常用通讯录数据,requestBody:{},userId:{},contactlist:{}", requestBody, user_id,
				JSON.toJSONString(contactList));
		if (null != contactList && contactList.size() > 0) {
			// 遍历查询加入联系人的信息
			for (ContactVo vo : contactList) {
				// 查询加入方的通讯录信息
				ContactVo cvo = contactInterface.findByFromUserIdAndToUserId(user_id, vo.getToUserId());
				ContactVo cv = new ContactVo();
				cv.setToUserId(cvo.getToUserId());
				cv.setGroupid(cvo.getGroupid());
				cv.setRankFlag(StringUtils.stringIsNotNull(cvo.getRankFlag()) ? cvo.getRankFlag() : "");
				myContactList.add(cv);
			}
		}
		logger.debug("获取个人常用通讯录数据,requestBody:{},userId:{},myContactList:{}", requestBody, user_id,
				JSON.toJSONString(myContactList));
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("contactList", myContactList);
		bodyJson.put("myGroupList", myGroupList);
		String resBody = ResponsePackUtil.encryptData(JSON.toJSONString(bodyJson), user_id);
		logger.debug("获取个人常用通讯录数据,resBody:{}", resBody);
		return ResponsePackUtil.buildPack("0000", resBody);
	}

	/**
	 * 添加常用联系人【5007】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String saveMyContact(String requestBody, String user_id) {
		logger.debug("添加保存常用通讯录数据!,requestBody:{},user_id:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		JSONArray toUserid = requestJson.getJSONArray("toUserId");// 多个分号
		String groupId = requestJson.getString("groupId");// 获取分组ID
		if (null == toUserid || "".equals(toUserid)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
		}
		ContactGroupVO congvo = null;
		if (StringUtils.stringIsNotNull(groupId)) {
			congvo = contactGroupInterface.findContactByGroupId(groupId);
			logger.debug("成功!,requestBody:{},userId:{}", requestBody, user_id);
		}
		// 查询用户所有添加的常用联系人列表(包含用户逻辑删除的记录)
		List<ContactVo> all_contactVo = contactInterface.findByFromUserId(user_id);
		logger.debug("添加保存常用通讯录元数据!,all_contactVo:{}", JSON.toJSONString(all_contactVo));
		Iterator<Object> it = toUserid.iterator();
		while(it.hasNext()){
			String toUserId=it.next().toString();
			ContactVo result_checkVo = checkContact(all_contactVo, toUserId);
			logger.debug("添加保存常用通讯录元数据!,result_checkVo:{}", JSON.toJSONString(result_checkVo));
	    	// 不存在添加
			if (null == result_checkVo) {
				ContactVo cVo = new ContactVo();
				cVo.setCtId(UUID.randomUUID().toString());
				cVo.setFromUserId(user_id);
				cVo.setToUserId(toUserId);
				// 查询加入方的通讯录信息
				MemberInfoVO memberInfoVo = memberInfoUtil.findMemberInfoById(toUserId);
				logger.debug("查询加入方用户的名称全拼toUserId:{},spell:{}", toUserId,
						null == memberInfoVo ? "" : memberInfoVo.getSpell());
				Date now = new Date();
				cVo.setToUserNameSpell(null == memberInfoVo ? "" : memberInfoVo.getSpell());
				cVo.setCtDate(now);
				cVo.setRemark(user_id + "添加" + toUserId);
				cVo.setCreateId(user_id);
				cVo.setCreateTime(now);
				cVo.setUpdateTime(now);
				cVo.setDelFlag("0");
				cVo.setGroupid(StringUtils.stringIsNotNull(groupId) ? groupId : "");
				logger.debug("详细数据!,cVo:{}", cVo);
				boolean resultSaveBool = contactInterface.saveContact(cVo);
				logger.debug(user_id + "添加" + toUserId + " >>" + resultSaveBool);
			} else {
				// 已是逻辑删除，进行恢复
				if ("1".equals(result_checkVo.getDelFlag())) {
					result_checkVo.setUpdateId(user_id);
					result_checkVo.setUpdateTime(new Date());
					result_checkVo.setDelFlag("0");
					result_checkVo.setGroupid(StringUtils.stringIsNotNull(groupId) ? groupId : "");
					boolean resultSaveBool = contactInterface.saveContact(result_checkVo);
					logger.debug(user_id + "已是逻辑删除，进行恢复" + toUserId + " >>" + resultSaveBool);
				} else {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1074, "");
				}
			}
		}
		return ResponsePackUtil.buildPack("0000", "");
	}

	/**
	 * 添加常用联系人分组【50071】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */

	public String saveMyContactGroup(String requestBody, String user_id) {
		logger.debug("添加保存常用联系人分组,requestBody:{},userId:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		JSONArray toUserId = requestJson.getJSONArray("toUserId");// 获取人员
		String groupName = requestJson.getString("groupName");// 分组名称
		String groupId = UUID.randomUUID().toString();
		if (null == groupName || "".equals(groupName)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
		}
		try {
			ContactGroupVO groupVO = new ContactGroupVO();
			groupVO.setGroupid(groupId);
			groupVO.setGroupname(groupName);
			groupVO.setFromUserId(user_id);
			groupVO.setCreateuserid(user_id);
			groupVO.setCreatetime(new Date());
			groupVO.setDeleFlag("0"); // 0:未删除 1:删除
			boolean resultSaveBool = contactGroupInterface.saveContactGroup(groupVO);
			logger.debug("人员" + user_id + "添加了群组" + groupId + ">>" + resultSaveBool);
			Iterator<Object> it = toUserId.iterator();
			while (it.hasNext()) {
				// 查询用户所有添加的常用联系人列表(包含用户逻辑删除的记录)
				String tuId = it.next().toString();
				List<ContactVo> all_contactVo = contactInterface.findByFromUserId(user_id);
				ContactVo result_checkVo = checkContact(all_contactVo, tuId);
				// 不存在添加
				if (null == result_checkVo) {
					ContactVo cVo = new ContactVo();
					cVo.setCtId(UUID.randomUUID().toString());
					cVo.setFromUserId(user_id);
					cVo.setToUserId(tuId);
					cVo.setCreateId(user_id);
					cVo.setCtDate(new Date());
					cVo.setRemark(user_id + "添加成员" + tuId + "至分组" + groupId);
					cVo.setDelFlag("0");// 0是未删除
					cVo.setGroupid(groupId);
					boolean resultSaveContBool = contactInterface.saveContact(cVo);
					logger.debug(tuId + "被添加到了分组ID是" + groupId + " >>" + resultSaveContBool);
				} else {
					// 已是逻辑删除，进行恢复
					if ("1".equals(result_checkVo.getDelFlag())) {
						result_checkVo.setUpdateId(user_id);
						result_checkVo.setUpdateTime(new Date());
						result_checkVo.setDelFlag("0");
						result_checkVo.setGroupid(groupId);
						boolean resultSaveDelBool = contactInterface.saveContact(result_checkVo);
						logger.debug(user_id + "已是逻辑删除，进行恢复" + toUserId + " >>" + resultSaveDelBool);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("wdw exception 20170401");
			System.out.println(e.toString());
		}
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("groupId", groupId);
		String resBody = ResponsePackUtil.encryptData(JSON.toJSONString(bodyJson), user_id);
		return ResponsePackUtil.buildPack("0000", resBody);

	}

	/**
	 * 修改常用联系人分组【50091】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */

	public String modifyMyContactGroup(String requestBody, String user_id) {
		logger.debug("修改常用联系人分组,requestBody:{},userId:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		String groupid = requestJson.getString("groupId");// 分组ID
		String modifyinfo = requestJson.getString("modifyinfo");// 操作类型
		try {
			if ("add".equals(modifyinfo)) {
				// 向指定常用联系人分组增加人员
				String toUserId = requestJson.getString("toUserId");
				if (null == toUserId || "".equals(toUserId)) {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
				}
				ContactVo cVo = contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
				if (cVo == null) {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1065, "");
				}
				cVo.setUpdateId(user_id);
				cVo.setUpdateTime(new Date());
				cVo.setGroupid(groupid);
				boolean resultSaveBool = contactInterface.saveContact(cVo);

			} else if ("delete".equals(modifyinfo)) {
				// 向指定常用联系人分组删除人员
				String toUserId = requestJson.getString("toUserId");
				if (null == toUserId || "".equals(toUserId)) {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
				}
				ContactVo cVo = contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
				if (cVo == null) {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1065, "");
				}
				cVo.setUpdateId(user_id);
				cVo.setUpdateTime(new Date());
				cVo.setDelFlag("1");
				boolean resultSaveBool = contactInterface.saveContact(cVo);

			} else if ("modify".equals(modifyinfo)) {
				// 向指定常用联系人分组人员
				String toUserId = requestJson.getString("toUserId");
				//修改联系人分组
				ContactVo contactVO= contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
				contactVO.setGroupid(null==groupid?"":groupid);
				contactVO.setUpdateId(user_id);
				contactVO.setCreateTime(new Date());
				contactVO.setUpdateTime(new Date());
				contactVO.setRankFlag(null);
				boolean resultBool = contactInterface.saveContact(contactVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("wdw modifyMyContactGroup exception");
		}
		return ResponsePackUtil.buildPack("0000", "");
	}

	/**
	 * 删除个人常用联系人分组【50081】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String deleteMyContactGroup(String requestBody, String user_id) {
		logger.debug("删除常用联系人分组,requestBody:{},userId:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		String groupid = requestJson.getString("groupId");// 分组ID
		// 查询出这个分组下的成员
		List<ContactVo> resultContactVo = contactInterface.findContactByGroupId(groupid);
		logger.debug("删除个人常用联系人分组【50081】数据,resultContactVo{}",JSON.toJSONString(resultContactVo));
		if (resultContactVo != null) {
			// 删除这个分组下面所有成员
			for (ContactVo resultList : resultContactVo) {
				// 删除用户常用联系人，
				// 逻辑删除，修改删除标识状态
				String toUserId = resultList.getToUserId();
				ContactVo cVo = contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
				logger.debug("数据,cVo:{},toUserId:{}，userId{}", cVo, toUserId,user_id);
				if (cVo == null) {
					return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1065, "");
				}
				cVo.setGroupid(groupid);
				cVo.setUpdateId(user_id);
				cVo.setFromUserId(toUserId);
				cVo.setUpdateTime(new Date());
				cVo.setDelFlag("1"); // 0:未删除 1:删除
				boolean resultSaveBool = contactInterface.saveContact(cVo);
				logger.debug(user_id + "逻辑删除，" + toUserId + " >>" + resultSaveBool);
			}
		}
		// 删除用户常用联系人分组
		// 逻辑删除，修改删除标识状态
		ContactGroupVO groupVO = contactGroupInterface.findContactByGroupId(groupid);
		groupVO.setUpdateuserid(user_id);
		groupVO.setUpdateTime(new Date());
		groupVO.setDeleFlag("1"); // 0:未删除 1:删除
		boolean resultSaveBool = contactGroupInterface.saveContactGroup(groupVO);
		return ResponsePackUtil.buildPack("0000", "");
	}

	/**
	 * 获取某个常用通讯录分组【50061】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String getMyContactGroup(String requestBody, String user_id) {
		logger.debug("获取某个常用通讯录分组,requestBody:{},userId:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		/** 校验参数 */
		if (null == user_id || "".equals(user_id)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
		}
		String groupid = requestJson.getString("groupId");// 分组ID
		// 常用联系人集合
		List<MyContactVo> myContactList = new ArrayList<MyContactVo>();
		// 查询用户添加的联系人列表
		List<ContactVo> contactList = contactInterface.findContactByGroupId(groupid);
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("contactList", contactList);
		String resBody = ResponsePackUtil.encryptData(JSON.toJSONString(bodyJson), user_id);
		logger.debug("获取个人常用通讯录分组数据,resBody:{}", resBody);
		return ResponsePackUtil.buildPack("0000", resBody);
		
	}

	/**
	 * 检查用户是否已在常用联系人，（包含逻辑删除的记录）
	 * 
	 * @param all_contactVo
	 *            用户的所有常用联系人列表
	 * @param toUserId
	 *            ，被加入方用户ID号
	 * @return ContactVo
	 */
	public ContactVo checkContact(List<ContactVo> all_contactVo, String toUserId) {
		logger.debug("检查被加入用户是否已在常用联系人toUserId：{},all_contactVo:{}", toUserId,
				null != all_contactVo ? JSON.toJSONString(all_contactVo) : "null");
		for (ContactVo cVo : all_contactVo) {
			if (toUserId.equals(cVo.getToUserId())) {
				return cVo;
			}
		}
		return null;
	}

	/**
	 * 检查被加入分组是否存在
	 * 
	 * @param all_ContactGroupVo
	 * @param groupId
	 * @return
	 */
	public ContactGroupVO checContactGroup(List<ContactGroupVO> all_ContactGroupVo, String groupId) {
		logger.debug("检查被加入用户的联系分组是否是已存在的分组groupId：{},all_ContactGroupVo", groupId,
				null != all_ContactGroupVo ? JSON.toJSONString(all_ContactGroupVo) : "Null");
		for (ContactGroupVO cGo : all_ContactGroupVo) {
			if (groupId.equals(cGo.getGroupid())) {
				return cGo;
			}
		}
		return null;

	}

	/**
	 * 删除个人常用联系人【5008】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String deleteMyContact(String requestBody, String user_id) {
		logger.debug("删除常用通讯录数据,requestBody:{},userId:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		String toUserId = requestJson.getString("toUserId");// 常用联系人记录ID
		if (null == toUserId || "".equals(toUserId)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1064, "");
		}
		// 删除用户常用联系人，
		// 逻辑删除，修改删除标识状态
		ContactVo cVo = contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
		if (cVo == null) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1065, "");
		}
		cVo.setUpdateId(user_id);
		cVo.setUpdateTime(new Date());
		cVo.setDelFlag("1");
		boolean resultSaveBool = contactInterface.saveContact(cVo);
		logger.debug(user_id + "逻辑删除，" + toUserId + " >>" + resultSaveBool);
		if (!resultSaveBool) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1073, "");
		}
		return ResponsePackUtil.buildPack("0000", "");
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
	// logger.debug("获取用户对应的密钥,userId:{},clientUserVO:{}", userId,
	// clientUserVO);
	// if (null == clientUserVO)
	// return null;
	// logger.debug("获取用户对应的密钥,userId:{},privateKey:{}", userId,
	// clientUserVO.getPrivateKey());
	// return clientUserVO.getPrivateKey();
	// } catch (Exception e) {
	// logger.error("获取用户对应的密钥异常,userId:{}", userId, e);
	// return null;
	// }
	//
	// }

	/**
	 * 整顺序接口【5010】
	 * 
	 * @param requestBody
	 * @param userId
	 * @return
	 */
	public String updateGroup(String requestBody, String user_id) {
		logger.debug("获取某个常用通讯录分组,requestBody:{},user_id:{}", requestBody, user_id);
		JSONObject requestJson = JSONObject.parseObject(requestBody);
		JSONArray groupid = requestJson.getJSONArray("groupId");
		logger.debug("获取某个常用通讯录分组,groupid:{}", groupid);
		String modifyinfo = requestJson.getString("modifyinfo");// 操作类型分为操作群组或是联系人
		// 检验参数
		try {
			if ("changContact".equals(modifyinfo)) {
				// 调整常用联系人顺序
				JSONArray arr_toUserId = requestJson.getJSONArray("toUserId");
				Iterator<Object> it = arr_toUserId.iterator();
				int count = 1;
				while (it.hasNext()) {
					String toUserId = it.next().toString();
					ContactVo cVo = contactInterface.findByFromUserIdAndToUserId(user_id, toUserId);
					cVo.setUpdateId(user_id);
					cVo.setUpdateTime(new Date());
					String s=""+count++;
					cVo.setRankFlag(s);
					boolean resultSaveBool = contactInterface.saveContact(cVo);
					logger.debug("调整联系人排序,cVo:{},userId:{}", JSON.toJSONString(cVo), user_id);
				}
			} else {
				if ("changGroup".equals(modifyinfo)) {
					// 调整常用联系人分组顺序
					Iterator<Object> it = groupid.iterator();
					int count = 1;
					while (it.hasNext()) {
						String groupId = it.next().toString();
						ContactGroupVO cGvo = contactGroupInterface.findByFromUserIdAndGroupId(user_id, groupId);
						cGvo.setUpdateTime(new Date());
						cGvo.setUpdateuserid(user_id);
						String s = "" + count++;
						cGvo.setRankGroupFlag(s);
						boolean resultSaveBool = contactGroupInterface.saveContactGroup(cGvo);
						logger.debug("调整联系人分组排序,cGvo:{},userId:{}", JSON.toJSONString(cGvo), user_id);
					}
				}

			}
		} catch (Exception e) {
			logger.error("调整常用联系人顺序出错", e);
		}
		return ResponsePackUtil.buildPack("0000", "");

	}

}
