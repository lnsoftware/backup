/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.logmanager.api.interfaces;

import java.util.Map;

import com.royasoft.vwt.soa.sundry.logmanager.api.vo.LogManagerVo;



/**
 * 信息反馈接口
 * 
 * @author jiangft
 * 
 * @Date 2016-3-31
 */
public interface LogManagerInterface {

    /**
     * 查询信息反馈，分页显示<br>
     * 
     * @param page 需要显示的页码
     * @param rows 每页显示多少条
     * @param conditions 查询条件 以EQ_(等于), LIKE_(模糊查询), GT_(大于), LT_(小于), GTE_(大于等于), LTE_(小于等于)开头 时间类型 开始时间 以start_time_开头 结束时间以end_time_开头 时间格式不做要求
     * 
     * @param sortMap 排序的map key为排序的字段 value为true 升序 false 降序
     * @return
     */
    public Map<String, Object> findLogManagerOfPage(int page, int rows, Map<String, Object> conditions, Map<String, Boolean> sortMap);


    
    /**
     * 根据主键id查询信息反馈
     * 
     * @param id
     * @return
     */
    public LogManagerVo findLogManagerById(String id);    
    
    /**
     * 保存记录
     * @param logManagerVO
     * @return 对象:成功.null:失败
     */
    public LogManagerVo saveLogManager(LogManagerVo logManagerVO);
}
