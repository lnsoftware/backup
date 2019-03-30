package com.royasoft.vwt.soa.sundry.unregisteRemind.impl.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.royasoft.vwt.soa.sundry.unregisteRemind.impl.entity.UnregisteRemind;

public interface UnregisteRemindDao extends PagingAndSortingRepository<UnregisteRemind, String>, JpaSpecificationExecutor<UnregisteRemind> {
    public UnregisteRemind findByCorpId(String corpId);
}
