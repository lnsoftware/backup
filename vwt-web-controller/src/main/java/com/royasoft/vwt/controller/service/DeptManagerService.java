/*
 * Copyright 2004-2017 上海若雅软件系统有限公司
 */
package com.royasoft.vwt.controller.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//by wdw import com.royasoft.vwt.common.rest.URLConnectionLoadBalancer;
import com.royasoft.vwt.common.security.MD5;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.constant.FunctionIdConstant;
import com.royasoft.vwt.controller.constant.ResponseInfoConstant;
import com.royasoft.vwt.controller.packet.QueuePacket;
import com.royasoft.vwt.controller.queue.ServicesQueue;
import com.royasoft.vwt.controller.util.PageUtils;
import com.royasoft.vwt.controller.util.ResponsePackUtil;
import com.royasoft.vwt.controller.vo.CurrentSysUser;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.DeptSquareInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.AccountManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.MenuManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.RolePowerManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.vo.AccountManegerVo;
import com.royasoft.vwt.soa.systemsettings.platform.api.vo.DeptMenuManagerVo;
import com.royasoft.vwt.soa.systemsettings.platform.api.vo.RolePowerManagerVo;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;

/**
 * 企业管理平台--部门管理员
 * 
 * @author hejinhu
 * @version ICT_VWT_REQ20160613_企业管理平台分部门角色管理 v3.2.0
 */
@Scope("prototype")
@Service
public class DeptManagerService implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DeptManagerService.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** 包含链接信息与报文信息的packet **/
	private QueuePacket queue_packet = null;

	/** 包含请求以及头信息报文内容 **/
	private Object msg = null;

	/** 客户端链接 **/
	private Channel channel = null;

	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private AccountManagerInterface accountManagerInterface;

	@Autowired
	private RolePowerManagerInterface rolePowerManagerInterface;

	@Autowired
	private MenuManagerInterface menuManagerInterface;

	@Autowired
	private DatabaseInterface databaseInterface;

	@Autowired
	private RedisInterface redisInterface;

	@Autowired
	private DepartMentInterface departMentInterface;

	@Autowired
    private DeptSquareInterface deptSquareInterface;
	
	/**
	 * 应用springcould地址
	 */
	//by wdw @Resource(name = "squareLoadBalancer")
	//by wdw private URLConnectionLoadBalancer squareLoadBalancer;

	/**
	 * 应用管理员添加应用列表功能
	 */
	private static final String SQARE_SET_MANAGER = "/square/set/manager";

	/**
	 * 应用管理员添加应用列表功能
	 */
	private static final String DEL_SQARE_MANAGER = "/square/del/manager";

	/**
	 * 查询应用管理员订购应用服务
	 */
	private static final String SQARE_MANAGER_LIST = "/square/manager/list";

	@Override
	public void run() {
		while (true) {
			try {
				queue_packet = ServicesQueue.deptManager_queue.take();// 获取队列处理数据
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
					/** 获取部门管理员列表 */
					case FunctionIdConstant.QUARE_DEPTMANAGER:
						resInfo = qryAccountList(request_body);
						break;
					/** 删除部门管理员 */
					case FunctionIdConstant.DEL_DEPTMANAGER:
						resInfo = delMagager(request_body);
						break;
					/** 部门管理员菜单查询 */
					case FunctionIdConstant.QUARY_MENU_DEPTMANAGER:
						resInfo = qryRoleMenuList(request_body);
						break;
					/** 部门管理员保存 */
					case FunctionIdConstant.SAVE_DEPTMANAGER:
						resInfo = addAdmin(request_body);
						break;
					/** 部门管理员修改 */
					case FunctionIdConstant.EDIT_DEPTMANAGER:
						resInfo = editAdmin(request_body);
						break;
					/** 部门管理员详情 */
					case FunctionIdConstant.DETAIL_DEPTMANAGER:
						resInfo = detailAdmin(request_body);
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
				logger.error("企业管理平台--部门管理员处理类异常:{}", e);
			} finally {

			}

		}
	}

	/**
	 * 查询部门管理员详情
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public String detailAdmin(String request_body) {
		logger.debug("部门管理员详情start,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		// String sessionid = requestJson.getString("sessionid");
		String userId = requestJson.getString("userId"); // 用户id
		if (ObjectUtils.isEmpty(userId)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "用户编码不能为空");
		}
		AccountManegerVo vo = accountManagerInterface.findAccountManagerById(Integer.valueOf(userId));
		vo.setPassword(null);
		String res = "";
		if (!Constants.DeptManager.SQUARE_ROLE.equals(Integer.toString(vo.getRoleid()))) {
			res = detailDeptAdmin(request_body, vo);
		} else {
//			requestJson.put("adminId", userId);
//			res = detailSquareAdmin(requestJson.toJSONString(), vo);

		}
		return res;
	}

	/**
	 * 查询部门管理员详情
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	private String detailDeptAdmin(String request_body, AccountManegerVo vo) {
		logger.debug("部门管理员详情start,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		// String sessionid = requestJson.getString("sessionid");
		String userId = requestJson.getString("userId"); // 用户id
		if (ObjectUtils.isEmpty(userId)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "用户编码不能为空");
		}
		List<DeptMenuManagerVo> list = null;
		DepartMentVO dvo = null;

		list = menuManagerInterface.qryDeptAdminMenuList(userId);
		dvo = departMentInterface.findById(vo.getReserved1());
		if (ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, vo);
		} else {
			String json = JSONObject.toJSONString(vo);
			JSONObject result = JSONObject.parseObject(json);
			result.put("items", list);
			result.put("deptName", dvo.getPartName());
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, result);
		}
	}

	/**
	 * 查询应用管理员信息
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	/*
	//by wdw 因为调用squareLoadBalancer在山东环境中没有，所以屏蔽
	public String detailSquareAdmin(String request_body, AccountManegerVo vo) {
		logger.debug("查询应用管理员,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		Map<String, Object> map = new HashMap<>();
		// 企业订购列表功能配置选择应用管理员功能
		map.put("content", requestJson.toJSONString());
		String res = squareLoadBalancer.call(SQARE_MANAGER_LIST, map);
		String responseStatus = ResponsePackUtil.getResCode(res);
		if (!"0000".equals(responseStatus)) {
			logger.error("查询应用管理员信息失败！:{}", res);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "查询应用管理员信息失败");
		} else {
			JSONObject squareJson = JSONObject.parseObject(res);
			String response_body = squareJson.getString("response_body");
			JSONArray items = JSONObject.parseObject(response_body).getJSONArray("items");
			String json = JSONObject.toJSONString(vo);
			JSONObject result = JSONObject.parseObject(json);
			result.put("items", items);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, result);
		}
	}
    */
	/**
	 * 部门管理员菜单查询
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public String qryRoleMenuList(String request_body) {
		logger.debug("部门管理员菜单查询 ,requestBody:{}", request_body);
		// JSONObject requestJson = JSONObject.parseObject(request_body);
		// String sessionid = requestJson.getString("sessionid");
		List<RolePowerManagerVo> list = rolePowerManagerInterface
				.findDeptRoleId(Integer.valueOf(Constants.DeptManager.DEPT_ROLE));
		Map<String, Object> model = new HashMap<String, Object>();
		if (ObjectUtils.isEmpty(list)) {
			model.put("DeptMenuList", "数据不存在");
		}
		model.put("items", list);
		return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
	}

	public String addAdmin(String request_body) {
		JSONObject requestJson = JSONObject.parseObject(request_body);
		String roleId = requestJson.getString("roleId"); // 角色id
		/** 校验用户是否存在 */
		String sessionid = requestJson.getString("sessionid");
		JSONObject session = getSession(sessionid);

		if (null == session) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户不存在");
		}

		if (ObjectUtils.isEmpty(session.getString("roleId")) || !session.getString("roleId").equals("3")) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户没有权限");
		}
		requestJson.put("corpId", session.getString("corpId"));
		requestJson.put("userCityArea", session.getString("userCityArea"));

		/** 校验参数 */
		if (session.getString("roleId").equals("8")) {

			if (ObjectUtils.isEmpty(requestJson.getString("reserved1"))) {
				return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "管理部门编码不能为空");
			}
		}

		String loginName = requestJson.getString("loginName");
		if (ObjectUtils.isEmpty(loginName)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "登录名不能为空");
		}

		String password = requestJson.getString("password"); // 登录密码
		if (ObjectUtils.isEmpty(password)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "密码不能为空");
		}
		//校验密码和账号重复性
        String loginName1 = loginName.toLowerCase();
        String password1 = password.toLowerCase();
        if(password1.contains(loginName1)){
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "用户口令不得包含用户名的完整字符串、大小写变位，请重新设置密码");
        }

		AccountManegerVo vo = null;
		vo = accountManagerInterface.findAccountManagerByLoginName(loginName);
		if (!ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, loginName + "登录名称已存在");
		}
		try {
			vo = getJsonToAccountManeger(requestJson);
		} catch (Exception e) {
			logger.error("保存获取部门管理员信息失败！", e);
		}

		/** 保存 */
		if (ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "获取管理员信息失败");
		} else {
			vo = accountManagerInterface.save(vo);
			if (!Constants.DeptManager.SQUARE_ROLE.equals(roleId)) {
				addDeptAdmin(request_body, vo);
			} else {
//				requestJson.put("adminId", vo.getAccountid());
//				String res = addSquareAdmin(requestJson.toJSONString(), vo);
//				return res;

			}
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "保存管理员信息成功");
		}

	}

	/**
	 * 新增部门管理员
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public String addDeptAdmin(String request_body, AccountManegerVo vo) {
		logger.debug("新增部门管理员,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		/** 保存 */
		if (ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "获取管理员信息失败");
		} else {
			JSONArray deptAdminMenuList = requestJson.getJSONArray("deptAdminMenuList"); // 菜单权限集合
			// 获取报文中菜单集合
			List<DeptMenuManagerVo> deptMenuList = getDetpMenuList(deptAdminMenuList, vo.getAccountid());
			menuManagerInterface.saveDeptRoleMenu(deptMenuList);
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "保存管理员信息成功");
		}

	}

	/**
	 * 新增应用管理员
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
//	public String addSquareAdmin(String request_body, AccountManegerVo vo) {
//		logger.debug("新增部门管理员,requestBody:{}", request_body);
//		JSONObject requestJson = JSONObject.parseObject(request_body);
//		Map<String, Object> map = new HashMap<>();
//		// 企业订购列表功能配置选择应用管理员功能
//		map.put("content", requestJson.toJSONString());
//		String res = squareLoadBalancer.call(SQARE_SET_MANAGER, map);
//		String responseStatus = ResponsePackUtil.getResCode(res);
//
//		if (!"0000".equals(responseStatus)) {
//			logger.error("保存管理员信息失败！:{}", res);
//			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "保存管理员信息失败");
//		} else {
//
//			JSONObject squareJson = JSONObject.parseObject(request_body);
//			 String items = squareJson.getString("squareIdList");
//			 List<String> list = new ArrayList<String>();
//			 if (!("").equals(items) && items != null) {
//				
//				String[] t = items.split(",");
//				
//				if (ObjectUtils.isArray(t)) {
//					list = Arrays.asList(t);
//				}
//			}
//			
//			String json = JSONObject.toJSONString(vo);
//			JSONObject result = JSONObject.parseObject(json);
//			result.put("items", list);
//			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, result);
//		}
//
//	}

	/**
	 * 编辑部门管理员
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public String editAdmin(String request_body) {
		logger.debug("编辑部门管理员,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);

		/** 校验用户是否存在 */
		String sessionid = requestJson.getString("sessionid");
		JSONObject session = getSession(sessionid);

		if (null == session) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户不存在");
		}

		if (ObjectUtils.isEmpty(session.getString("roleId")) || !session.getString("roleId").equals("3")) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户没有权限");
		}
		requestJson.put("corpId", session.getString("corpId"));
		requestJson.put("userCityArea", session.getString("userCityArea"));
		/** 校验参数 */
		if (session.getString("roleId").equals("8")) {

			if (ObjectUtils.isEmpty(requestJson.getString("reserved1"))) {
				return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "管理部门编码不能为空");
			}
		}
		String loginName = requestJson.getString("loginName");
		if (ObjectUtils.isEmpty(loginName)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "登录名不能为空");
		}
		String userId = requestJson.getString("userId");
		if (ObjectUtils.isEmpty(userId)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "用户id不能为空");
		}
		String password = requestJson.getString("password"); // 登录密码
        if (ObjectUtils.isEmpty(password)) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "密码不能为空");
        }
        //校验密码和账号重复性
        String loginName1 = loginName.toLowerCase();
        String password1 = password.toLowerCase();
        if(password1.contains(loginName1)){
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "用户口令不得包含用户名的完整字符串、大小写变位，请重新设置密码");
        }
		AccountManegerVo vo = null;
		vo = accountManagerInterface.findAccountManagerById(Integer.valueOf(userId));
		if (ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "编辑时用户信息不存在");
		}
		String pwd = vo.getPassword();
		vo = accountManagerInterface.findAccountManagerByLoginName(loginName);
		if (!ObjectUtils.isEmpty(vo) && vo.getAccountid() != Integer.valueOf(userId).intValue()) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, loginName + "登录名称已存在");
		}
		try {
			vo = getJsonToAccountManeger(requestJson);
		} catch (Exception e) {
			logger.error("编辑时获取部门管理员信息失败！", e);
		}

		/** 保存 */
		if (ObjectUtils.isEmpty(vo)) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, "编辑时获取管理员信息失败");
		} else {
			// 密码为空时，则为原来的密码
			if (ObjectUtils.isEmpty(vo.getPassword())) {
				vo.setPassword(pwd);
			}
			vo = accountManagerInterface.save(vo);

			if (!Constants.DeptManager.SQUARE_ROLE.equals(Integer.toString(vo.getRoleid()))) {
				editDeptAdmin(request_body, vo);
			} else {
//				requestJson.put("adminId", vo.getAccountid());
//				String res = addSquareAdmin(requestJson.toJSONString(), vo);
//				return res;
			}
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "编辑管理员信息成功");
		}

	}

	/**
	 * 编辑部门管理员
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public void editDeptAdmin(String request_body, AccountManegerVo vo) {
		logger.debug("编辑部门管理员,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);
		// 先删除部门菜单
		List<String> userStrs = new ArrayList<String>();
		userStrs.add(String.valueOf(vo.getAccountid()));
		menuManagerInterface.delDeptRoleMenu(userStrs);
		JSONArray deptAdminMenuList = requestJson.getJSONArray("deptAdminMenuList"); // 菜单权限集合
		// 获取报文中菜单集合
		List<DeptMenuManagerVo> deptMenuList = getDetpMenuList(deptAdminMenuList, vo.getAccountid());
		// 后保存部门菜单
		menuManagerInterface.saveDeptRoleMenu(deptMenuList);
	}

	/**
	 * 从请求报文中获取管理员菜单
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param deptAdminMenuList
	 * @param userId
	 * @return
	 */
	private List<DeptMenuManagerVo> getDetpMenuList(JSONArray deptAdminMenuList, Integer userId) {
		List<DeptMenuManagerVo> list = new ArrayList<DeptMenuManagerVo>();
		if (ObjectUtils.isEmpty(deptAdminMenuList)) {
			return list;
		}
		DeptMenuManagerVo vo = null;
		JSONObject ob = null;
		for (Object o : deptAdminMenuList) {
			ob = JSONObject.parseObject(o.toString());
			vo = new DeptMenuManagerVo();
			vo.setDeptMenuId(UUID.randomUUID().toString().replace("-", ""));
			vo.setAdminId(String.valueOf(userId));
			vo.setMenuId(ob.getInteger("menuId"));
			list.add(vo);
		}
		return list;
	}

	/**
	 * 获取部门管理员信息
	 * 
	 * @param requestJson
	 *            请求报文
	 * @author hejinhu
	 * @version 3.2.0
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private AccountManegerVo getJsonToAccountManeger(JSONObject requestJson)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		AccountManegerVo vo = null;
		if (ObjectUtils.isEmpty(requestJson)) {
			return vo;
		}
		vo = new AccountManegerVo();
		String reserved1 = requestJson.getString("reserved1"); // 管理部门编码
		String loginName = requestJson.getString("loginName"); // 登录名称
		String password = requestJson.getString("password"); // 登录密码
		String telNum = requestJson.getString("telNum"); // 手机号码
		String iseffective = requestJson.getString("isEffective"); // 是否有效
		String userId = requestJson.getString("userId"); // 用户id
		String userName = requestJson.getString("userName"); // 用户名
		String corpId = requestJson.getString("corpId"); // 企业id
		String roleId = requestJson.getString("roleId"); // 角色id
		String region = requestJson.getString("userCityArea"); // 区域编码
		if (ObjectUtils.isEmpty(userId)) {
			vo.setAccountid(Integer.valueOf(databaseInterface.generateId("sys_user", "user_id") + ""));
		} else {
			vo.setAccountid(Integer.valueOf(userId));
		}
		vo.setAccountlogginname(loginName);
		vo.setAccountusername(userName);
		vo.setIseffective(iseffective);
		if (!ObjectUtils.isEmpty(password)) {
			vo.setPassword(MD5.encodeMD5(password));
		}

		vo.setReserved1(reserved1);
		vo.setTelnum(telNum);
		vo.setRoleid(Integer.valueOf(roleId));
		vo.setCorpid(corpId);
		vo.setRegionid(region);
		return vo;
	}

	/**
	 * 删除管理员
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	public String delMagager(String request_body) {
		logger.debug("删除部门管理员,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);

		/** 校验参数 */
		JSONArray userIds = requestJson.getJSONArray("deptUserId");
		JSONArray squareUserIds = requestJson.getJSONArray("squareUserIdList");

		/** 校验用户是否存在 */
		String sessionid = requestJson.getString("sessionid");
		JSONObject session = getSession(sessionid);

		if (null == session) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户不存在");
		}

		if (ObjectUtils.isEmpty(session.getString("roleId")) || !session.getString("roleId").equals("3")) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户没有权限");
		}

		/** 删除用户信息 */
		List<Integer> userAllList = new ArrayList<Integer>();
		// 获取用户id列表
		List<Integer> userIdList = new ArrayList<Integer>();
		for (int i = 0; i < userIds.size(); i++) {
			userIdList.add(userIds.getInteger(i));
		}
		userAllList.addAll(userIdList);
		List<Integer> squareUserIdList = new ArrayList<Integer>();
		for (int i = 0; i < squareUserIds.size(); i++) {
			squareUserIdList.add(squareUserIds.getInteger(i));
		}
		userAllList.addAll(squareUserIdList);

		boolean flag = accountManagerInterface.deleteByIds(userAllList);

		logger.info("wdw userIdList.size(): {}",userIdList.size());
		if (userIdList.size() > 0) {
			logger.info("wdw aa userIdList.size(): {}",userIdList.size());			
			for (int i = 0; i < userIdList.size(); i++) {
				logger.info("wdw bb userIdList.get(i): {}",userIdList.get(i));				
			    deptSquareInterface.deleteByAdminid(String.valueOf(userIdList.get(i)));
			    
			}
		}
		
		logger.debug("删除公共信息");
		if (userIdList.size() > 0) {
			delDeptMenuList(userIdList);
		}
		if (squareUserIdList.size() > 0) {
			requestJson.put("squareUserIdList", squareUserIdList);
//			delSquareInfoList(request_body);
		}

		if (flag) {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "删除成功");
		} else {
			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1004, userIds + "删除失败");
		}
	}

	/**
	 * 
	 */
	private void delDeptMenuList(List<Integer> userIdList) {
		List<String> userStrs = new ArrayList<String>();
		for (Integer us : userIdList) {
			userStrs.add(us.toString());
		}
		menuManagerInterface.delDeptRoleMenu(userStrs);
	}

	/**
	 * 删除应用管理员信息
	 * 
	 * @param request_body
	 */
//	private String delSquareInfoList(String request_body) {
//
//		logger.debug("删除应用管理员信息,requestBody:{}", request_body);
//		JSONObject requestJson = JSONObject.parseObject(request_body);
//		Map<String, Object> map = new HashMap<>();
//		// 企业订购列表功能配置选择应用管理员功能
//		map.put("content", requestJson.toJSONString());
//		String res = squareLoadBalancer.call(DEL_SQARE_MANAGER, map);
//		String responseStatus = ResponsePackUtil.getResCode(res);
//		if (!"0000".equals(responseStatus)) {
//			logger.error("删除应用管理员信息！:{}", res);
//			return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL1006, "删除应用管理员信息失败");
//		} else {
//			return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, "删除应用管理员信息成功");
//		}
//
//	}

	/**
	 * 获取部门管理员列表
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param request_body
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String qryAccountList(String request_body) {
		logger.debug("获取部门管理员列表,requestBody:{}", request_body);
		JSONObject requestJson = JSONObject.parseObject(request_body);

		Map<String, Object> model = new HashMap<String, Object>();

		String sessionid = requestJson.getString("sessionid");
		String userName = requestJson.getString("userName"); // 用户名字(支持模糊查询)
		String logginName = requestJson.getString("loginName"); // 登录名字(支持模糊查询)
		String roleId = requestJson.getString("roleId"); // 角色(支持模糊查询)
		String qryTelNum = requestJson.getString("qryTelNum"); // 手机号码
		String page = requestJson.getString("page");// 前台传递的页面位置请求
		String row = requestJson.getString("row");// 前台传递的每页显示数
		JSONObject session = getSession(sessionid);

        if (null == session) {
            return ResponsePackUtil.buildPack(ResponseInfoConstant.FAIL2051, "用户不存在");
        }
        String corpId = session.getString("corpId").toString();
		int pageIndex = 1;
		int pageSize = 10;
		if (StringUtils.isNotEmpty(page)) {
			pageIndex = Integer.parseInt(page);
		}
		if (StringUtils.isNotEmpty(row)) {
			pageSize = Integer.parseInt(row);
		}
		if (ObjectUtils.isEmpty(roleId)) {
			roleId = "" + Constants.DeptManager.DEPT_ROLE + "," + Constants.DeptManager.SQUARE_ROLE;
		}
		int total = 0;
		List<AccountManegerVo> list = null;
		Map<String, Object> m = getUsers(null, corpId, userName, logginName, roleId, qryTelNum, pageIndex, pageSize);
		if (null != m) {
			list = (List<AccountManegerVo>) m.get("content");
			System.out.println("-------------->"+JSON.toJSONString(list));
			// 密码隐藏
			for (AccountManegerVo vo : list) {
				vo.setPassword(null);
			}
			total = Integer.parseInt(m.get("total").toString());
			if (total > 0) {
				int pageNum = PageUtils.getPageCount(total, pageSize);
				// 封装后的数据
				model.put("items", list);
				model.put("pageNum", pageNum);// 总页数
				return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
			}
		}
		// 数据查询异常返回异常提示
		model.put("items", list);
		model.put("pageNum", 1);// 数据总数

		return ResponsePackUtil.buildPack(ResponseInfoConstant.SUCC, model);
	}

	/**
	 * 查询用户列表
	 * 
	 * @author hejinhu
	 * @version 3.2.0
	 * @param userId
	 *            用户id
	 * @param userName
	 *            用户名
	 * @param logginName
	 *            登录名
	 * @param roleId
	 *            角色id
	 * @param qryTelNum
	 *            手机号
	 * @param pageIndex
	 *            页码
	 * @param pageSize
	 *            每页条数
	 * @return 用户列表
	 */
	private Map<String, Object> getUsers(String userId, String corpId, String userName, String logginName, String roleId,
			String qryTelNum, int pageIndex, int pageSize) {

		// 地市区域条件查询
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("EQ_corpid", corpId);
		// 角色、用户名和登录名模糊查询
		if (StringUtils.isNotEmpty(roleId)) {
			String[] roleItems = roleId.split(",");
			if (roleItems.length > 1) {
				condition.put("IN_roleid", roleId.trim());
			} else {
				condition.put("EQ_roleid", roleId.trim());
			}
		}
		if (StringUtils.isNotEmpty(logginName)) {
			condition.put("LIKE_accountlogginname", logginName.trim());
		}

		if (StringUtils.isNotEmpty(userId)) {
			condition.put("EQ_accountid", userId.trim());
		}

		if (StringUtils.isNotEmpty(userName)) {
			condition.put("LIKE_accountusername", userName.trim());
		}
		if (StringUtils.isNotEmpty(qryTelNum)) {
			condition.put("LIKE_telnum", qryTelNum.trim());
		}

		logger.debug("getUsers : pageIndex:{},pageSize:{},condition:{}" + pageIndex, pageSize,
				JSONObject.toJSONString(condition));
		//Map<String, Boolean> sortMap = new TreeMap<String, Boolean>();
		//sortMap.put("createDate", false);
		//Map<String, Object> m = accountManagerInterface.findAccountManagerOfPage(pageIndex, pageSize, condition, sortMap);
		Map<String, Object> m = accountManagerInterface.findAccountManagerOfPage(pageIndex, pageSize, condition, null);
		
		return m;
	}

	/**
	 * 获取登录用户信息
	 * 
	 * @version 3.2.0
	 * @author hejinhu
	 * @param sessionId
	 * @return
	 */
	private JSONObject getSession(String sessionId) {
		/** 校验session */
		String session = redisInterface.getString(Constants.nameSpace + sessionId);
		JSONObject sessionJson = JSONObject.parseObject(session);
		return sessionJson;
	}
}
