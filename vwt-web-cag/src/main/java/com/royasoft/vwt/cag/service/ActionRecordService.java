/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.royasoft.vwt.cag.vo.RedisAction;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;

@Component
public class ActionRecordService {
    /** 实体类命名空间 */
    private static final String actionEntityNameSpace = "ROYASOFT:VWT:ENTITY:ACTION:";

    /** 缓存命名空间, 用于生成自增长id */
    private static final String actionIdNameSpace = "ROYASOFT:VWT:ID:ACTION";

    @Autowired
    private ImRedisInterface imRedisInterface;

    public Long save(RedisAction record) throws Exception {
        Long id = imRedisInterface.incr(actionIdNameSpace);
        record.setId(id);
        imRedisInterface.setString(actionEntityNameSpace + id, JSON.toJSONString(record), 7 * 24 * 60 * 60);
        return id;
    }

}
