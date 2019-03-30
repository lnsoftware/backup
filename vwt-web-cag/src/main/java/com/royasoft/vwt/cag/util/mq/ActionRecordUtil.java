package com.royasoft.vwt.cag.util.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;

@Component
public class ActionRecordUtil {
    /** 实体类命名空间 */
    private static final String actionEntityNameSpace = "ROYASOFT:VWT:ENTITY:ACTION:";

    /** 缓存命名空间, 用于生成自增长id */
    private static final String actionIdNameSpace = "ROYASOFT:VWT:ID:ACTION";

    @Autowired
    private ImRedisInterface imRedisInterface;

    // private HbaseService hbaseService;
    // private static RedisInterface redisInterface;
    // public static RedisInterface getRedisInterface() {
    // return redisInterface;
    // }
    //
    // public static void setRedisInterface(RedisInterface redisInterface) {
    // ActionRecordUtil.redisInterface = redisInterface;
    // }

    public Long save(RedisAction record) throws Exception {
        Long id = imRedisInterface.incr(actionIdNameSpace);
        record.setId(id);
        imRedisInterface.setString(actionEntityNameSpace + id, JSON.toJSONString(record), 7 * 24 * 60 * 60);
        // HbaseService.saveActionMessage(actionEntityNameSpace + id, JSON.toJSONString(record));
        return id;
    }
    
    /**
     * 提供给V网通帮助 服务号使用
     * 该方法与save的区别在于入redis时没有生命周期
     * @param record
     * @return
     * @throws Exception
     */
    public Long saveNoClear(RedisAction record) throws Exception {
        Long id = imRedisInterface.incr(actionIdNameSpace);
        record.setId(id);
        imRedisInterface.setString(actionEntityNameSpace + id, JSON.toJSONString(record));
        return id;
    }
}
