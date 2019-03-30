/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.logmanager.impl.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.common.orm.DynamicSpecifications;
import com.royasoft.vwt.common.orm.SearchFilter;
import com.royasoft.vwt.common.util.DateFormat;
import com.royasoft.vwt.soa.sundry.logmanager.api.interfaces.LogManagerInterface;
import com.royasoft.vwt.soa.sundry.logmanager.api.vo.LogManagerVo;
import com.royasoft.vwt.soa.sundry.logmanager.impl.dao.LogManagerDao;
import com.royasoft.vwt.soa.sundry.logmanager.impl.entity.LogManager;

/**
 * 日志管理
 * 
 * @author jiangft
 * 
 * @Date 2016-4-6
 */
@Transactional(readOnly = false)
@Service(cluster = "failfast", timeout = 60000)
public class LogManagerService implements LogManagerInterface {

    private final Logger logger = LoggerFactory.getLogger(LogManagerService.class);

    @Autowired
    private LogManagerDao logManagerDao;

    /**
     * 查询素材信息，分页显示<br>
     * 
     * @param page 需要显示的页码
     * @param rows 每页显示多少条
     * @param conditions 查询条件 以EQ_(等于), LIKE_(模糊查询), GT_(大于), LT_(小于), GTE_(大于等于), LTE_(小于等于)开头 时间类型 开始时间 以start_time_开头 结束时间以end_time_开头 时间格式不做要求
     * 
     * @param sortMap 排序的map key为排序的字段 value为true 升序 false 降序
     * @return
     */
    public Map<String, Object> findLogManagerOfPage(int page, int rows, Map<String, Object> conditions, Map<String, Boolean> sortMap) {
        logger.debug("分页查询日志列表, page{}, rows{},conditions{},sortMap{}", page, rows, conditions, sortMap);
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            // 对conditions中的日期类型 format
            conditions = DateFormat.formatDate(conditions);

            PageRequest pageRequest = buildPageRequest(page, rows, sortMap);

            Map<String, SearchFilter> filters = SearchFilter.parse(conditions);

            Specification<LogManager> spec = DynamicSpecifications.bySearchFilter(filters.values(), LogManager.class);
            Page<LogManager> pages = logManagerDao.findAll(spec, pageRequest);
            List<LogManager> list = pages.getContent();

            map.put("total", pages.getTotalElements());
            map.put("content", transEntityToVo(list));
            return map;
        } catch (Exception e) {
            logger.error("分页查询日志列表异常", e);
        }
        return null;
    }

    /**
     * 构建分页请求
     * 
     * @param pageNumber
     * @param pagzSize
     * @param sortType
     * @return
     */
    private PageRequest buildPageRequest(int pageNumber, int pagzSize, Map<String, Boolean> sortMap) {
        Sort sort = null;
        if (sortMap == null)
            sort = new Sort(Direction.ASC, "id");
        else {
            Iterator<String> keys = sortMap.keySet().iterator();
            List<Order> orderList = new ArrayList<Sort.Order>();
            while (keys.hasNext()) {
                String key = keys.next();
                boolean value = Boolean.valueOf(sortMap.get(key));
                orderList.add(new Order(value == true ? Direction.ASC : Direction.DESC, key));
            }
            sort = new Sort(orderList);
        }
        return new PageRequest(pageNumber - 1, pagzSize, sort);

    }

    @Override
    public LogManagerVo saveLogManager(LogManagerVo logManagerVo) {
        logger.debug("保存日志上传信息,vo对象:{}", JSONObject.toJSONString(logManagerVo));
        if (logManagerVo == null)
            return null;
        try {
            LogManager logManager = logManagerDao.save(transVoToEntity(logManagerVo));
            if(null==logManager){
                logger.error("保存日志上传信息失败{}");
                return null;
            }
            
            logger.debug("保存日志上传信息成功",JSON.toJSONString(logManager));
            return transEntityToVo(logManager);
        } catch (Exception e) {
            logger.error("保存日志上传信息异常{}",e);
        }
        return null;
    }
    
    /**
     * 将实体集合转成vo集合
     * 
     * @param entity
     * @return
     */
    private List<LogManagerVo> transEntityToVo(List<LogManager> entity) {
        List<LogManagerVo> vo_list = new ArrayList<LogManagerVo>();
        for (LogManager Customer : entity) {
            vo_list.add(transEntityToVo(Customer));
        }
        return vo_list;
    }

    /**
     * 将实体类转换成vo对象
     * 
     * @param entity
     */
    private LogManagerVo transEntityToVo(LogManager entity) {
        LogManagerVo vo = new LogManagerVo();
        try {
            BeanUtils.copyProperties(entity, vo);
        } catch (Exception e) {
            logger.error("实体对象转换VO异常", e);
        }
        return vo;
    }
    
    /**
     * 将VO转换成实体对象
     * 
     * @param entity
     */
    private LogManager transVoToEntity(LogManagerVo logManagerVo) {
        LogManager logManager=new LogManager();
        try {
            BeanUtils.copyProperties(logManagerVo, logManager);
        } catch (Exception e) {
            logger.error("VO转换实体对象异常", e);
        }
        return logManager;
    }
    
    /**
     * 根据主键id查询信息反馈
     * 
     * @param id 
     * @return
     */
    public LogManagerVo findLogManagerById(String id){
        logger.debug("根据主键id查询日志{}", id);
        if (StringUtils.isEmpty(id))
            return null;

        LogManager reg= logManagerDao.findByLogid(id);
        if (reg == null)
            return null;
        return transEntityToVo(reg);
    }    
    
}
