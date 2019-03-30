package com.royasoft.vwt.soa.sundry.surfernews.impl.services;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.royasoft.vwt.soa.sundry.surfernews.api.interfaces.SurferNewsInterface;
import com.royasoft.vwt.soa.sundry.surfernews.impl.dao.SurferDao;
import com.royasoft.vwt.soa.sundry.surfernews.impl.entity.Surfer;

/**
 * 冲浪新闻 业务处理
 * 
 * @author daizl
 *
 */
@Transactional(readOnly = false)
@Service(cluster = "failfast", timeout = 60000)
public class SurferNewsService implements SurferNewsInterface {
    @Autowired
    private SurferDao surferDao;

    public void saveSurferInfo(String uuid, Timestamp t, String content, String source) {
        Surfer surfer = new Surfer();
        surfer.setId(uuid);
        surfer.setTime(t);
        surfer.setMessage(content);
        surfer.setSource(source);
        surferDao.save(surfer);
    }
}
