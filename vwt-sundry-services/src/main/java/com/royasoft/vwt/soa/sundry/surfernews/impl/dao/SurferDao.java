package com.royasoft.vwt.soa.sundry.surfernews.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.royasoft.vwt.soa.sundry.surfernews.impl.entity.Surfer;

/**
 * 冲浪新闻dao
 * @author daizl
 *
 */
public interface SurferDao extends PagingAndSortingRepository<Surfer, String>, JpaSpecificationExecutor<Surfer> {

}
