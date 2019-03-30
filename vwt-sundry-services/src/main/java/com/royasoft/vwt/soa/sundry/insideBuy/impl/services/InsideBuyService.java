package com.royasoft.vwt.soa.sundry.insideBuy.impl.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.core.tools.common.dao.QueryUtils;
import com.royasoft.vwt.soa.sundry.insideBuy.api.interfaces.InsideBuyInterface;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.ActivityDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.GoodsInfoDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.GoodsModelDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.GoodsTypeDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.OrderDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.dao.ShoppingCartDao;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Activity;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsInfo;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsModel;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.GoodsType;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.Order;
import com.royasoft.vwt.soa.sundry.insideBuy.impl.entity.ShoppingCart;
import com.royasoft.vwt.soa.sundry.utils.Response;
import com.royasoft.vwt.soa.sundry.utils.ResponseInfoConstant;
import com.royasoft.vwt.soa.sundry.utils.ResponsePackUtil;

/**
 * 山东 内购需求（二期）
 * 
 * @Author: yucong
 * @Since: 2019年3月25日
 */
@Transactional
@Service(cluster = "failfast", timeout = 180000)
public class InsideBuyService implements InsideBuyInterface {

	private Logger logger = LoggerFactory.getLogger(InsideBuyService.class);

	@Resource
	private EntityManager entityManager;
	@Autowired
	private ShoppingCartDao shoppingCartDao;
	@Autowired
	private ActivityDao activityDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private GoodsModelDao goodsModelDao;
	@Autowired
	private GoodsInfoDao goodsInfoDao;
	@Autowired
	private GoodsTypeDao goodsTypeDao;

	@Override
	public Response getGoodsList(Long phoneId) throws Exception {
		logger.debug("内购-获取商品列表，入参，phoneId:{}", phoneId);

		// 查询当前的活动
		List<Activity> activities = activityDao.findActivityByEndTime(new Date());
		if (CollectionUtils.isEmpty(activities)) {
			logger.error("内购-获取产品列表，活动结束");
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1011, "");
		}

		// TODO 默认获取第一个，但是业务逻辑待优化
		Activity activity = activityDao.findActivityByEndTime(new Date()).get(0);

		// 编写获取产品列表SQL
		StringBuffer sql = new StringBuffer();
		sql.append("select type.goods_type, ");
		sql.append("info.goods_name,info.description, info.picture, info.model_num, info.limit_count, ");
		sql.append("model.discount_price, model.market_price ");
		sql.append("from vwt_ins_goods_info info ");
		sql.append("join vwt_ins_goods_type type on info.goods_type_id = type.id ");
		sql.append("join vwt_ins_goods_model model on model.goods_info_id = info.id ");
		sql.append("where info.status = 1 order by type.type_order, info.goods_num, info.update_time");

		// 查询产品列表
		List<Map<String, Object>> list = QueryUtils.queryForMap(entityManager, sql.toString(),
				new HashMap<String, Object>());
		if (CollectionUtils.isEmpty(list)) {
			logger.error("内购-获取商品列表，列表为空，SQL:{}", sql.toString());
		}
		// 产品列表数据封装
		List<Object> data = goodsListdataProcess(list);

		// 根据用户手机号查询购物车数量
		Integer count = shoppingCartDao.findNumOfCartByPhoneId(phoneId);
		logger.debug("内购-获取商品列表，购物车数量count:{}", count);

		// 结果集
		JSONObject result = new JSONObject();
		result.put("data", data);
		result.put("shoppingCart", count);
		result.put("activity", activity.getStartIntroduce());

		logger.debug("内购模块-获取商品列表，出参，result:{}", result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	public Response getGoodsDetail(String modelNum) throws Exception {
		logger.debug("内购-获取单个商品详情，入参，modelNum:{}", modelNum);

		// 编写获取单个产品详情SQL
		StringBuffer sql = new StringBuffer();
		sql.append("select i.goods_name,i.description, i.picture, i.brand, i.id as goods_info_id, i.limit_count, ");
		sql.append("m.discount_price, m.market_price, m.goods_model, m.inventory, m.id as goods_model_id ");
		sql.append("from vwt_ins_goods_info i ");
		sql.append("join vwt_ins_goods_model m on m.goods_info_id = i.id ");
		sql.append("where i.status = 1 and i.model_num = :model_num");

		// 根据modelNum查询单个商品下的全部型号
		Map<String, Object> param = new HashMap<>();
		param.put("model_num", modelNum);
		List<Map<String, Object>> list = QueryUtils.queryForMap(entityManager, sql.toString(), param);
		if (CollectionUtils.isEmpty(list)) {
			logger.error("内购-获取单个商品详情，列表为空，SQL:{}", sql.toString());
		}

		// 获取全部型号
		List<String> goodsModel = new ArrayList<>();
		for (Map<String, Object> map : list) {
			goodsModel.add(String.valueOf(map.get("goods_model")));
		}

		// 结果封装
		JSONObject result = new JSONObject();
		result.put("data", list);
		result.put("model", goodsModel);

		logger.debug("内购模块-获取单个商品详情，出参，result:{}", result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	public Response getShoppingCartList(Long phoneId) throws Exception {
		logger.debug("内购-获取购物车列表，入参，phoneId:{}", phoneId);

		// 编写获取购物车列表SQL
		StringBuffer sql = new StringBuffer();
		sql.append("select cart.goods_count, ");
		sql.append("info.description, info.goods_name, info.model_num, info.picture, info.status, ");
		sql.append("model.inventory, model.discount_price, model.goods_model ");
		sql.append("from vwt_ins_shopping_cart cart ");
		sql.append("join vwt_ins_goods_info info on cart.goods_info_id = info.id ");
		sql.append("join vwt_ins_goods_model model on cart.goods_model_id = model.id ");
		sql.append("where cart.phone_id = :phone_id");

		// 根据phoneId查询单个商品下的全部型号
		Map<String, Object> param = new HashMap<>();
		param.put("phone_id", phoneId);
		List<Map<String, Object>> result = QueryUtils.queryForMap(entityManager, sql.toString(), param);
		if (CollectionUtils.isEmpty(result)) {
			logger.error("内购-获取购物车列表，列表为空，SQL:{}", sql.toString());
		}
		logger.debug("内购-获取购物车列表，出参，result:{}", result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, JSON.toJSONString(result));
	}

	@Override
	@Transactional
	public Response saveShoppingCart(Long phoneId, Long goodsInfoId, Long goodsModelId, Integer goodsCount)
			throws Exception {
		logger.debug("内购-购物车添加功能，入参，phoneId:{}，goodsInfoId:{}，goodsModelId:{}，goodsCount:{}", phoneId, goodsInfoId,
				goodsModelId, goodsCount);
		// 根据ID查询购物新是否已有商品
		ShoppingCart cart = shoppingCartDao.findByPhoneInfoModelId(phoneId, goodsInfoId, goodsModelId);
		if (cart == null) {
			cart = new ShoppingCart();
			GoodsInfo goodsInfo = new GoodsInfo();
			goodsInfo.setId(goodsInfoId);
			GoodsModel goodsModel = new GoodsModel();
			goodsModel.setId(goodsModelId);

			cart.setPhoneId(phoneId);
			cart.setGoodsCount(goodsCount);
			cart.setGoodsInfo(goodsInfo);
			cart.setGoodsModel(goodsModel);
		} else {
			cart.setGoodsCount(cart.getGoodsCount() + goodsCount);
		}
		shoppingCartDao.save(cart);
		logger.debug("内购-购物车添加功能，添加成功");
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	@Transactional
	public Response updateShoppingCart(Long cartId, Integer goodsCount) throws Exception {
		logger.debug("内购-购物车更新功能，入参，cartId:{}，goodsCount:{}", cartId, goodsCount);
		if (goodsCount == 0) {
			shoppingCartDao.deleteByCartId(cartId);
		} else {
			shoppingCartDao.updateGoodsCountByCartId(cartId, goodsCount);
		}
		logger.debug("内购-购物车更新功能，更新成功");
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	@Transactional
	public Response deleteShoppingCart(Long cartId) throws Exception {
		logger.debug("内购-购物车删除功能，入参，cartId:{}", cartId);
		shoppingCartDao.deleteByCartId(cartId);
		logger.debug("内购-购物车删除功能，删除成功");
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response getOrderList(Long phoneId) throws Exception {
		logger.debug("内购-获取订单列表，入参，phoneId:{}", phoneId);

		// 编写订单查询SQL
		StringBuffer sql = new StringBuffer();
		sql.append("select i.description, i.goods_name, i.model_num, i.picture, i.status, ");
		sql.append("m.discount_price, m.goods_model, m.inventory, o.phone_num, o.username");
		sql.append("o.address, o.business_hall, o.create_time, o.goods_count, o.order_group, o.phone_id ");
		sql.append("from vwt_ins_order o ");
		sql.append("join vwt_ins_goods_info i on o.goods_info_id = i.id ");
		sql.append("join vwt_ins_goods_model m on o.goods_model_id = m.id ");
		sql.append("where o.phone_id = :phone_id order by o.create_time");

		Map<String, Object> map = new HashMap<>();
		map.put("phone_id", phoneId);
		List<Map<String, Object>> list = QueryUtils.queryForMap(entityManager, sql.toString(), map);
		if (CollectionUtils.isEmpty(list)) {
			logger.error("内购-获取订单列表，列表为空，SQL:{}", sql.toString());
		}

		// 订单数据封装
		List<Object> data = orderListdataProcess(list);

		// 结果集
		JSONObject result = new JSONObject();
		result.put("data", data);

		logger.debug("内购模块-获取订单列表，出参，result:{}", result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	@Transactional
	public Response deleteShoppingCart(List<JSONObject> list) {
		logger.debug("内购-订单删除功能，入参，list:{}", list);

		// 根据orderGroup批量删除
		int count = orderDao.deleteByOrderGroup(list.get(0).getString("orderGroup"));
		logger.debug("内购-订单删除功能，从订单中删除了 {} 条", count);

		// 根据 goodsModelId 找到对应的产品型号，在库存上加 goodsCount
		for (JSONObject map : list) {
			goodsModelDao.addInventoryById(map.getLong("goodsModelId"), map.getInteger("goodsCount"));
			logger.debug("内购-订单删除功能，订单已删除，在对应的库存里加上返回数量，goodsModelId：{},goodsCount {}", map.getLong("goodsModelId"),
					map.getInteger("goodsCount"));
		}
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	@Transactional
	public Response saveOrder(List<Order> list) throws Exception {
		logger.debug("内购-订单预约功能，入参，list:{}", list);
		String code = null;

		// 循环每个订单的限制条件，满足则保存
		for (Order order : list) {
			// 查出各种限制条件，只有 inventory 是变值，所以此处存在一个隐藏的BUG，查询库存不足时，有人取消订单将数量返回给库存，此时用户却被告知库存不足
			Map<String, Object> limit = queryAllLimitCount(order.getGoodsInfo().getId(), order.getGoodsModel().getId());
			int preBuyCount = order.getGoodsCount();
			int orderedCount = Integer.parseInt(String.valueOf(limit.get("orderedCount")));
			int actLimitCount = Integer.parseInt(String.valueOf(limit.get("actLimitCount")));
			int inventory = Integer.parseInt(String.valueOf(limit.get("inventory")));
			int modelLimitCount = Integer.parseInt(String.valueOf(limit.get("modelLimitCount")));
			logger.error(
					"内购-订单预约功能，限制条件 preBuyCount:{},orderedCount:{},actLimitCount:{},inventory:{},modelLimitCount:{}",
					preBuyCount, orderedCount, actLimitCount, inventory, modelLimitCount);

			if (preBuyCount > modelLimitCount || (preBuyCount + orderedCount) > modelLimitCount) {
				logger.error("内购-订单预约功能，该机型限购数量 limitCount：{}", modelLimitCount);
				throw new Exception("-92025");
			}
			if (preBuyCount > inventory) {
				logger.error("内购-订单预约功能，该型号库存不足 inventory：{}", inventory);
				throw new Exception("-92026");
			}
			if (preBuyCount > actLimitCount || (preBuyCount + orderedCount) > actLimitCount) {
				logger.error("内购-订单预约功能，购买总数量超过活动限购数量 actLimitCount：{}", actLimitCount);
				throw new Exception("-92027");
			}

			// 如果条件都满足，就先从库存减去 preBuyCount，再将订单插入数据库
			// 如果删除失败就意味着库存不足，或者其他网络原因
			int count = goodsModelDao.minusInventoryById(order.getGoodsModel().getId(), preBuyCount);
			if (count == 0) {
				logger.error("内购-订单预约功能，该型号库存不足 inventory：{}", inventory);
				throw new Exception("-92026");
			}
			orderDao.save(order);
		}
		return ResponsePackUtil.buildResponse(code, "");
	}

	// 保存订单时，查出各种限制条件
	private Map<String, Object> queryAllLimitCount(Long goodsInfoId, Long goodsModelId) {
		StringBuffer sql = new StringBuffer();

		sql.append("select ");
		sql.append("SUM(o.goods_count) as orderedCount, ");
		sql.append("(select a.limit_total as actCount from vwt_ins_activity a) as actLimitCount, ");
		sql.append("(select m.inventory from vwt_ins_goods_model m where m.id = :goodsModelId) as inventory, ");
		sql.append("(select i.limit_count from vwt_ins_goods_info i where i.id = :goodsInfoId) modelLimitCount ");
		sql.append("from vwt_ins_order o where o.phone_id = 18752334498 and o.goods_info_id = 1");

		Map<String, Object> map = new HashMap<>();
		map.put("goodsInfoId", goodsInfoId);
		map.put("goodsModelId", goodsModelId);
		Map<String, Object> singleForMap = QueryUtils.querySingleForMap(entityManager, sql.toString(), map);
		return singleForMap;
	}

	/**
	 * <li>首先对同一个产品下的不同型号去重，默认展示第一个产品价格</li>
	 * <li>然后对不同产品进行分类存放，存放顺序不能变</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	private List<Object> goodsListdataProcess(List<Map<String, Object>> list) {
		List<Object> data = new ArrayList<>();
		if (CollectionUtils.isEmpty(list)) {
			return data;
		}

		// 获取产品名称
		List<String> goodsNames = new ArrayList<>();
		for (Map<String, Object> map : list) {
			String goods_name = String.valueOf(map.get("goods_name"));
			if (!goodsNames.contains(goods_name)) {
				goodsNames.add(goods_name);
			}
		}
		// 过滤list集合中同一产品下的不同品牌，默认选择第一个
		List<Map<String, Object>> removeSameModel = new ArrayList<>();
		for (String goodsName : goodsNames) {
			for (Map<String, Object> map : list) {
				if (goodsName.equals(map.get("goods_name"))) {
					removeSameModel.add(map);// 默认选择第一个
					break;
				}
			}
		}

		// 获取类型名称
		List<String> goodsTypes = new ArrayList<>();
		for (Map<String, Object> map : list) {
			String goods_type = String.valueOf(map.get("goods_type"));
			if (!goodsTypes.contains(goods_type)) {
				goodsTypes.add(goods_type);
			}
		}
		// 对removeSameModel集合中的产品进行分类，分类顺序不变
		List<Map<String, Object>> groupByType = null;
		for (String goodsType : goodsTypes) {
			groupByType = new ArrayList<>();
			for (Map<String, Object> map : removeSameModel) {
				if (goodsType.equals(map.get("goods_type"))) {
					groupByType.add(map);
				}
			}
			data.add(groupByType);
		}
		return data;
	}

	/**
	 * <li>根据 orderGroup 对列表进行分组</li>
	 *
	 * @Author: yucong
	 * @Since: 2019年3月27日
	 */
	private List<Object> orderListdataProcess(List<Map<String, Object>> list) {
		List<Object> data = new ArrayList<>();
		if (CollectionUtils.isEmpty(list)) {
			return data;
		}

		// 获取 order_group
		List<String> orderGroups = new ArrayList<>();
		for (Map<String, Object> map : list) {
			String order_group = String.valueOf(map.get("order_group"));
			if (!orderGroups.contains(order_group)) {
				orderGroups.add(order_group);
			}
		}
		// 对list集合进行分类，分类顺序不变
		List<Map<String, Object>> groupByUUID = null;
		for (String orderGroup : orderGroups) {
			groupByUUID = new ArrayList<>();
			for (Map<String, Object> map : list) {
				if (orderGroup.equals(map.get("order_group"))) {
					groupByUUID.add(map);
				}
			}
			data.add(groupByUUID);
		}
		return data;
	}

	/************************** Manager-Service-Start ***************************/
	@Override
	public Response getGoodsListForManager(int page, int rows) {
		logger.debug("内购模块-管理平台查询商品列表，入参，page:{}，rows:{}", page, rows);
		Page<GoodsInfo> pages = goodsInfoDao.getGoodsListForManager(new PageRequest(page - 1, rows));
		JSONObject result = new JSONObject();
		result.put("data", pages.getContent());
		result.put("nums", pages.getTotalPages());
		logger.debug("内购模块-管理平台查询商品列表，出参,page:{},rows:{},result:{}", page, rows, result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	@Transactional
	public Response saveGoodsForManager(GoodsInfo goodsInfo) {
		logger.debug("内购模块-管理平台商品添加功能，入参，goodsInfo:{}", goodsInfo);
		goodsInfoDao.save(goodsInfo);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response getGoodsInfoForManager(Long goodsInfoId) {
		logger.debug("内购模块-管理平台获取单个商品详情，入参，goodsInfoId:{}", goodsInfoId);
		GoodsInfo goodsInfo = goodsInfoDao.findOne(goodsInfoId);
		logger.debug("内购模块-管理平台获取单个商品详情，出参，goodsInfo:{}", JSON.toJSON(goodsInfo));
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, JSON.toJSON(goodsInfo));
	}

	@Override
	@Transactional
	public Response updateGoodsForManager(GoodsInfo goodsInfo) {
		logger.debug("内购模块-管理平台商品编辑功能，入参，goodsInfo:{}", goodsInfo);
		goodsInfoDao.save(goodsInfo);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	@Transactional
	public Response deleteGoodsForManager(Long goodsInfoId) {
		logger.debug("内购模块-管理平台商品删除功能，入参，goodsInfoId:{}", goodsInfoId);
		int count = orderDao.findAllByGoodsInfoId(goodsInfoId);
		if (count > 0) {
			return ResponsePackUtil.buildResponse(ResponseInfoConstant.FAIL1012, "");
		}
		goodsInfoDao.delete(goodsInfoId);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response getTypeListForManager(int page, int rows) {
		logger.debug("内购模块-管理平台查询商品类型列表，入参，page:{}，rows:{}", page, rows);
		Page<GoodsType> pages = goodsTypeDao.findAll(new PageRequest(page - 1, rows, Direction.ASC, "typeOrder"));
		JSONObject result = new JSONObject();
		result.put("data", pages.getContent());
		result.put("nums", pages.getTotalPages());
		logger.debug("内购模块-管理平台查询商品类型列表，出参,page:{},rows:{},result:{}", page, rows, result);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, result);
	}

	@Override
	@Transactional
	public Response saveTypeForManager(GoodsType goodsType) {
		logger.debug("内购模块-管理平台商品类型添加功能，入参，goodsType：{}", goodsType);
		goodsTypeDao.save(goodsType);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response getTypeDetailForManager(Long goodsTypeId) {
		logger.debug("内购模块-管理平台获取单个商品类型详情，入参，goodsTypeId:{}", goodsTypeId);
		GoodsType goodsType = goodsTypeDao.findOne(goodsTypeId);
		logger.debug("内购模块-管理平台获取单个商品类型详情，出参，goodsType:{}", JSON.toJSON(goodsType));
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, JSON.toJSON(goodsType));
	}

	@Override
	@Transactional
	public Response updateTypeForManager(GoodsType goodsType) {
		logger.debug("内购模块-管理平台商品类型编辑功能，入参，goodsType:{}", goodsType);
		goodsTypeDao.save(goodsType);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	@Transactional
	public Response deleteTypeForManager(Long goodsTypeId) {
		logger.debug("内购模块-管理平台商品类型删除功能，入参，goodsTypeId:{}", goodsTypeId);
		goodsTypeDao.delete(goodsTypeId);
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, "");
	}

	@Override
	public Response getActivityDetail() {
		// 活动查询
		List<Activity> list = activityDao.findAll();

		// 产品限购查询
		StringBuffer sql = new StringBuffer();
		sql.append("select i.id, i.model_num, i.limit_count from vwt_ins_goods_info i where i.limit_count > 0");
		List<Map<String, Object>> buyLimit = QueryUtils.queryForMap(entityManager, sql.toString(),
				new HashMap<String, Object>());

		// 结果集封装
		JSONObject jsonObject = new JSONObject();
		if (CollectionUtils.isEmpty(list)) {
			jsonObject.put("activity", "");
			jsonObject.put("limitTotal", "");
		} else {
			jsonObject.put("activity", list.get(0));
			jsonObject.put("limitTotal", list.get(0).getLimitTotal());
		}
		jsonObject.put("buyLimit", buyLimit);

		logger.debug("内购模块-管理平台获取活动详情，出参，jsonObject:{}", jsonObject.toString());
		return ResponsePackUtil.buildResponse(ResponseInfoConstant.SUCC, jsonObject.toString());
	}

}
