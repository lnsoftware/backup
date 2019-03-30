package com.royasoft.vwt.soa.sundry.insidePurch.impl.services;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.soa.base.zookeeper.api.interfaces.ZookeeperInterface;
import com.royasoft.vwt.soa.sundry.Constants;
import com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces.InsidePruchInterface;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.GoodsDetailVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.GoodsInfoVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.OrderRecordInfoVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.OrderRecordVO;
import com.royasoft.vwt.soa.sundry.insidePurch.api.vo.SaveOrderVO;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.dao.GoodsDetailDao;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.dao.GoodsInfoDao;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.dao.OrderRecordDao;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.dao.OrderRecordInfoDao;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.GoodsDetail;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.GoodsInfo;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.OrderRecord;
import com.royasoft.vwt.soa.sundry.insidePurch.impl.entity.OrderRecordInfo;
import com.royasoft.vwt.soa.sundry.utils.Response;
import com.royasoft.vwt.soa.sundry.utils.ResponseInfoConstant;
import com.royasoft.vwt.soa.sundry.utils.ResponsePackUtil;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.vo.DepartMentVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

@Transactional
@Service(cluster = "failfast", timeout = 180000)
public class InsidePruchService implements InsidePruchInterface {

	private Logger logger = LoggerFactory.getLogger(InsidePruchService.class);

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private GoodsInfoDao goodsInfoDao;

	@Resource
	private GoodsDetailDao goodsDetailDao;

	@Resource
	private OrderRecordDao orderRecordDao;
	
	@Resource
	private OrderRecordInfoDao orderRecordInfoDao;

	@Resource
	private MemberInfoInterface memberInfoInterface;

	@Resource
	private DepartMentInterface departMentInterface;

	@Resource
	private ZookeeperInterface zookeeperInterface;

	@Override
	public Response detailGoodsInfo(Long goodsId) throws Exception {
		logger.debug("内购模块-查询商品详情，入参,goodsId:{}", goodsId);
		if (null == goodsId) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		GoodsInfo goodsInfo = goodsInfoDao.findOne(goodsId);
		if (goodsInfo == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1003, "");
		}
		logger.debug("内购模块-查询商品详情，出参,goodsInfo:{}", JSON.toJSONString(goodsInfo));
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, goodsInfo);
	}

	@Override
	public Response insertGoodsInfo(GoodsInfoVO goodsInfoVO) throws Exception {
		logger.debug("内购模块-新增商品详情，入参,goodsId:{}", JSON.toJSONString(goodsInfoVO));
		String checkResult = checkGoodInfoParam(goodsInfoVO, false);
		if ("1".equals(checkResult)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		} else if ("2".equals(checkResult)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1005, "");
		}
		GoodsInfo info = new GoodsInfo();
		info.setLogoUrl(goodsInfoVO.getLogoUrl());
		info.setName(goodsInfoVO.getName());
		info.setOptTime(new Date());
		info.setOptUserId(goodsInfoVO.getOptUserId());
		Long maxSort = goodsInfoDao.findMaxSort();
		info.setSort(maxSort == null ? 0 : maxSort + 1);
		goodsInfoDao.save(info);
		List<GoodsDetailVO> detailList = goodsInfoVO.getDetailList();
		if (!CollectionUtils.isEmpty(detailList)) {
			for (GoodsDetailVO goodsDetailVO : detailList) {
				GoodsDetail detail = new GoodsDetail();
				BeanUtils.copyProperties(goodsDetailVO, detail);
				detail.setGoodsInfo(info);
				goodsDetailDao.save(detail);
			}
		}
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, null);
	}

	@Override
	public Response updateGoodsInfo(GoodsInfoVO goodsInfoVO) throws Exception {
		logger.debug("内购模块-修改商品详情，入参,goodsId:{}", JSON.toJSONString(goodsInfoVO));
		String checkResult = checkGoodInfoParam(goodsInfoVO, true);
		if ("1".equals(checkResult)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		} else if ("2".equals(checkResult)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1005, "");
		}
		GoodsInfo info = goodsInfoDao.findOne(goodsInfoVO.getId());
		if (info == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1003, "");
		}
		List<GoodsDetail> oldGoodsDetailList = info.getDetailList();
		info.setLogoUrl(goodsInfoVO.getLogoUrl());
		info.setName(goodsInfoVO.getName());
		info.setOptTime(new Date());
		info.setOptUserId(goodsInfoVO.getOptUserId());
		goodsInfoDao.save(info);
		Set<Long> oldSet = new HashSet<>();
		Set<Long> existSet = new HashSet<>();
		Map<Long, GoodsDetail> oldGoodsMap = new HashMap<>();
		Map<String, GoodsDetail> map = new HashMap<>();
		for (GoodsDetail goodsDetail : oldGoodsDetailList) {
			map.put(goodsDetail.getTypeName(), goodsDetail);
			oldGoodsMap.put(goodsDetail.getId(), goodsDetail);
			oldSet.add(goodsDetail.getId());
		}
		logger.debug("oldGoosMap:{}", JSON.toJSONString(oldGoodsMap));
		logger.debug("oldSet111:{}", JSON.toJSONString(oldSet));
		List<GoodsDetailVO> detailList = goodsInfoVO.getDetailList();
		if (!CollectionUtils.isEmpty(detailList)) {
			for (GoodsDetailVO goodsDetailVO : detailList) {
				GoodsDetail detail = map.get(goodsDetailVO.getTypeName());
				// 原始数据没有，需要新增
				if (detail == null) {
					detail = new GoodsDetail();
					BeanUtils.copyProperties(goodsDetailVO, detail);
					detail.setGoodsInfo(info);
				} else {
					detail.setInsidePrice(goodsDetailVO.getInsidePrice());
					detail.setOutPrice(goodsDetailVO.getOutPrice());
					detail.setSort(goodsDetailVO.getSort());
					existSet.add(detail.getId());
				}
				goodsDetailDao.save(detail);
			}
		}
		// 删除数据
		oldSet.removeAll(existSet);
		logger.debug("oldSet222:{}", JSON.toJSONString(oldSet));
		for (Long detailId : oldSet) {
			GoodsDetail detail = oldGoodsMap.get(detailId);
			if (detail != null) {
				goodsDetailDao.delete(detail);
				// 删除订购记录
				List<OrderRecord> recordList = orderRecordDao.findRecordByDetail(detail);
				OrderRecordInfo recordInfo = null;
				for (OrderRecord orderRecord : recordList) {
					recordInfo=orderRecord.getOrderRecordInfo();
				}
				orderRecordDao.deleteRecord(detail);
				if(recordInfo!=null){
					orderRecordInfoDao.delete(recordInfo);
				}
			}
		}
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, null);
	}

	private String checkGoodInfoParam(GoodsInfoVO vo, boolean isEdit) {
		if (StringUtils.isEmpty(vo.getLogoUrl()) || StringUtils.isEmpty(vo.getName())
				|| (isEdit && null == vo.getId())) {
			return "1";
		}
		if (!CollectionUtils.isEmpty(vo.getDetailList())) {
			Set<String> set = new HashSet<>();
			for (GoodsDetailVO detailVO : vo.getDetailList()) {
				if (StringUtils.isEmpty(detailVO.getTypeName()) || null == detailVO.getSort()) {
					return "1";
				}
				if (set.contains(detailVO.getTypeName())) {
					return "2";
				}
				set.add(detailVO.getTypeName());
			}
		}
		return "0";
	}

	@Override
	public Response deleteGoodsInfo(Long goodsId) throws Exception {
		logger.debug("内购模块-删除商品，入参,goodsId:{}", goodsId);
		if (null == goodsId) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		GoodsInfo goodsInfo = goodsInfoDao.findOne(goodsId);
		if (goodsInfo == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1003, "");
		}
		List<GoodsDetail> detailList = goodsInfo.getDetailList();
		goodsDetailDao.delete(goodsInfo.getDetailList());
		goodsInfoDao.delete(goodsInfo);
		// 删除订购信息
		if (!CollectionUtils.isEmpty(detailList)) {
			for (GoodsDetail goodsDetail : detailList) {
				orderRecordDao.deleteRecord(goodsDetail);
			}
		}
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response pageGoodsInfoForPt(int page, int rows) throws Exception {
		logger.debug("内购模块-管理平台查询商品列表，入参,page:{},rows:{}", page, rows);
		if (page < 1 || rows < 1) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		Page<GoodsInfo> pages = goodsInfoDao.findAll(new PageRequest(page - 1, rows, Direction.DESC, "sort"));
		JSONObject result = new JSONObject();
		result.put("data", pages.getContent());
		result.put("nums", pages.getTotalPages());
		logger.debug("内购模块-管理平台查询商品列表，出参,page:{},rows:{},result:{}", page, rows, result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	public Response pageGoodsInfoForClient(String userId) throws Exception {
		logger.debug("内购模块-客户端查询商品列表，入参,userId:{}", userId);
		String maxCount = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_MAX_COUNT);
		String endTimeStr = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_END_TIME);
		Date endTime = sdf.parse(endTimeStr);
		if (endTime.before(new Date())) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1009, "");
		}
		MemberInfoVO member = memberInfoInterface.findById(userId);
		if (member == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1004, "");
		}
		Iterable<GoodsInfo> goodsList = goodsInfoDao.findAll(new Sort(Direction.DESC, "sort"));
		JSONObject result = new JSONObject();
		result.put("data", goodsList);
		result.put("endTime", endTimeStr);
		result.put("userName", member.getMemberName());
		result.put("telNum", member.getTelNum());
		result.put("jobNum", member.getJobNum());
		result.put("maxCount", maxCount);
		logger.debug("内购模块-客户端查询商品列表，出参,result:{}", result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	public Response orderGoods(String userId,String recAddress, List<SaveOrderVO> orderList) throws Exception {
		logger.debug("内购模块-客户端预定商品，入参,userId:{},orderList:{}", userId, JSON.toJSONString(orderList));
		if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(orderList)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		String maxCount = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_MAX_COUNT);
		String endTimeStr = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_END_TIME);
		String everyCountStr=zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_MAX_EVERY_COUNT);
		Long everyCount=null;
		if(!StringUtils.isEmpty(everyCountStr)){
			everyCount=Long.parseLong(everyCountStr);
		}
		Date endTime = sdf.parse(endTimeStr);
		if (endTime.before(new Date())) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1009, "");
		}
		MemberInfoVO member = memberInfoInterface.findById(userId);
		if (member == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1004, "");
		}
		DepartMentVO dept = departMentInterface.findById(member.getDeptId());
		// 判断该用户是否已经过商品
		Long count = orderRecordDao.findUserOrderCount(member.getTelNum());
		if (count != null && count > 0) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1008, "");
		}
		Date now = new Date();
		OrderRecordInfo info=new OrderRecordInfo();
		info.setDeptName(dept == null ? "" : dept.getPartFullName());
		info.setJobNum(member.getJobNum());
		info.setOrderCount(count);
		info.setOrderTime(now);
		info.setTelNum(member.getTelNum());
		info.setUserId(member.getMemId());
		info.setUserName(member.getMemberName());
		info.setRecAddress(recAddress);
		orderRecordInfoDao.save(info);
		count = 0L;
		List<OrderRecord> list = new ArrayList<>();
		
		for (SaveOrderVO saveOrderVO : orderList) {
			if (null == saveOrderVO.getGoodsDetailId() || saveOrderVO.getOrderCount() == null
					|| saveOrderVO.getOrderCount() <= 0) {
				return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
			}
			if(everyCount!=null&&everyCount<saveOrderVO.getOrderCount()){
				return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1010, "");
			}
			count += count;
			OrderRecord record = new OrderRecord();
			GoodsDetail detail = goodsDetailDao.findOne(saveOrderVO.getGoodsDetailId());
			if (detail == null) {
				return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1002, "");
			}
			record.setDeptName(dept == null ? "" : dept.getPartFullName());
			record.setGoodsDetail(detail);
			record.setJobNum(member.getJobNum());
			record.setOrderCount(saveOrderVO.getOrderCount());
			record.setOrderTime(now);
			record.setTelNum(member.getTelNum());
			record.setUserId(member.getMemId());
			record.setUserName(member.getMemberName());
			record.setOrderRecordInfo(info);
			list.add(record);
		}
		if (count > Integer.parseInt(maxCount)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1007, "");
		}
		orderRecordDao.save(list);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response orderCancel(String userId) throws Exception {
		logger.debug("内购模块-客户端取消预订，入参,userId:{}", userId);
		if (StringUtils.isEmpty(userId)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		MemberInfoVO member = memberInfoInterface.findById(userId);
		if (member == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1004, "");
		}
		orderRecordDao.deleteByTelNum(member.getTelNum());
		orderRecordInfoDao.deleteByTelNum(member.getTelNum());
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response pageOrderInfoForPt(int page, int rows) throws Exception {
		logger.debug("内购模块-管理平台查询预定信息，入参,page:{},rows:{}", page, rows);
		if (page < 1 || rows < 1) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		JSONObject json = new JSONObject();
		List<OrderRecordInfoVO> innerList = new ArrayList<>();
		Page<OrderRecordInfo> pages = orderRecordInfoDao
				.findAll(new PageRequest(page - 1, rows, new Sort(Direction.DESC, "id")));
		List<OrderRecordInfo> recordInfoList = pages.getContent();
		for (OrderRecordInfo orderRecordInfo : recordInfoList) {
			OrderRecordInfoVO vo = new OrderRecordInfoVO();
			BeanUtils.copyProperties(orderRecordInfo, vo);
			vo.setOrderTime(sdf.format(orderRecordInfo.getOrderTime()));
			List<OrderRecord> recordList = orderRecordInfo.getRecordList();
			List<OrderRecordVO> recordVolist=new ArrayList<>();
			long count =0;
			for (OrderRecord orderRecord : recordList) {
				OrderRecordVO orderRecordVO=new OrderRecordVO();
				BeanUtils.copyProperties(orderRecord, orderRecordVO);
				orderRecordVO.setTypeName(orderRecord.getGoodsDetail().getTypeName());
				orderRecordVO.setInsidePrice(orderRecord.getGoodsDetail().getInsidePrice());
				orderRecordVO.setOutPrice(orderRecord.getGoodsDetail().getOutPrice());
				orderRecordVO.setSumPrice(mul(orderRecord.getOrderCount(), orderRecord.getGoodsDetail().getInsidePrice()));
				orderRecordVO.setName(orderRecord.getGoodsDetail().getGoodsInfo().getName());
				count+=orderRecord.getOrderCount();
				recordVolist.add(orderRecordVO);
			}
			vo.setOrderCount(count);
			vo.setRecordList(recordVolist);
			innerList.add(vo);
		}
		json.put("data", innerList);
		json.put("nums", pages.getTotalElements());
		logger.debug("内购模块-管理平台查询商品列表，出参,json:{}", json);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, json);
	}

	@Override
	public Response pageOrderInfoForClient(String userId) throws Exception {
		logger.debug("内购模块-客户端查询预定信息，入参,userId:{}", userId);
		if (StringUtils.isEmpty(userId)) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		MemberInfoVO member = memberInfoInterface.findById(userId);
		if (member == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1004, "");
		}
		JSONObject json = new JSONObject();
		List<OrderRecordVO> innerList = new ArrayList<>();
		List<OrderRecord> recordList = orderRecordDao.findByTelNumOrderByOrderTime(member.getTelNum());
		GoodsInfo info = null;
		OrderRecordInfo orderRecordInfo=null;
		for (OrderRecord orderRecord : recordList) {
			if(null==orderRecordInfo){
				orderRecordInfo=orderRecord.getOrderRecordInfo();
			}
			if (info == null) {
				info = orderRecord.getGoodsDetail().getGoodsInfo();
			}
			OrderRecordVO vo = new OrderRecordVO();
			BeanUtils.copyProperties(orderRecord, vo);
			vo.setOrderTime(sdf.format(orderRecord.getOrderTime()));
			vo.setName(orderRecord.getGoodsDetail().getGoodsInfo().getName());
			vo.setTypeName(orderRecord.getGoodsDetail().getTypeName());
			vo.setInsidePrice(orderRecord.getGoodsDetail().getInsidePrice());
			vo.setOutPrice(orderRecord.getGoodsDetail().getOutPrice());
			vo.setSumPrice(mul(orderRecord.getOrderCount(), orderRecord.getGoodsDetail().getInsidePrice()));
			innerList.add(vo);
		}
		json.put("data", innerList);
		json.put("jobNum", member.getJobNum());
		json.put("telNum", member.getTelNum());
		json.put("userName", member.getMemberName());
		json.put("recAddress",orderRecordInfo==null?"": orderRecordInfo.getRecAddress());
		String maxCount = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_MAX_COUNT);
		String endTimeStr = zookeeperInterface.getPropertiesByNodePath(Constants.ZK_INSIDE_END_TIME);
		json.put("maxCount", maxCount);
		json.put("endTime", endTimeStr);
		logger.debug("内购模块-客户端查询预定信息，出参,json:{}", json);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, json);
	}

	private double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	@Override
	public Response checkOrderRecord(Long goodsId, Long detailId) throws Exception {
		logger.debug("内购模块-校验商品是否已经被预定，入参,goodsId:{},detailId:{}", goodsId, detailId);
		if (null == goodsId) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1001, "");
		}
		GoodsInfo info = goodsInfoDao.findOne(goodsId);
		if (info == null) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1003, "");
		}

		List<GoodsDetail> detailList = new ArrayList<>();
		if (null != detailId) {
			GoodsDetail detail = goodsDetailDao.findOne(detailId);
			detailList.add(detail);
		} else {
			detailList.addAll(info.getDetailList());
		}
		if (!CollectionUtils.isEmpty(detailList)) {
			for (GoodsDetail goodsDetail : detailList) {
				long count = orderRecordDao.findRecordCount(goodsDetail);
				if (count > 0) {
					return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1006, "");
				}
			}
		}
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}
}
