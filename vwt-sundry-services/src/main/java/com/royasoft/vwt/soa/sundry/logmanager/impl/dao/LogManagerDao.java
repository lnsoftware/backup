/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.logmanager.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.royasoft.vwt.soa.sundry.logmanager.impl.entity.LogManager;



/**
 * 日志管理DAO
 * 
 * @author jiangft
 * 
 * @Date 2016-4-6
 */
public interface LogManagerDao extends PagingAndSortingRepository<LogManager, String>, JpaSpecificationExecutor<LogManager> {

    /**
     * 根据主键id查询素材
     * 
     * @param id 
     * @return
     */
    public LogManager findByLogid(String id);
}
