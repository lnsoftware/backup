package com.royasoft.vwt.soa.sundry.unregisteRemind.impl.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.royasoft.vwt.soa.sundry.unregisteRemind.api.interfaces.UnregisteRemindInterface;
import com.royasoft.vwt.soa.sundry.unregisteRemind.api.vo.UnregisteRemindVO;
import com.royasoft.vwt.soa.sundry.unregisteRemind.impl.dao.UnregisteRemindDao;
import com.royasoft.vwt.soa.sundry.unregisteRemind.impl.entity.UnregisteRemind;

/**
 * 未激活人员短信提醒业务处理类
 * 
 * @author daizl
 *
 */
@Transactional(readOnly = false)
@Service(cluster = "failfast", timeout = 180000)
public class UnregisteRemindService implements UnregisteRemindInterface {
    
    private Logger logger=LoggerFactory.getLogger(UnregisteRemindService.class);
    
    @Autowired
    private UnregisteRemindDao unregisteRemindDao;
    
    /**
     * 根据企业id查询
     * 
     * @param corpId
     * @return
     */
    @Override
    public UnregisteRemindVO findByCorpId(String corpId) {
        logger.debug("根据企业id查询未激活提醒信息,corpid:{}",corpId);
        try {
            if(StringUtils.isEmpty(corpId))
                return null;
            UnregisteRemind unregisteRemind = unregisteRemindDao.findByCorpId(corpId);
            if(unregisteRemind==null)
                return null;
            
            return transEntityToVo(unregisteRemind);
        } catch (Exception e) {
            logger.error("根据企业id查询未激活提醒信息异常,corpid:{},e:{}",corpId,e);
            return null;
        }
    }

    /**
     * 保存信息
     * 
     * @param unregisteRemindVO
     * @return
     */
    @Override
    public UnregisteRemindVO save(UnregisteRemindVO unregisteRemindVO) {
        logger.debug("保存未激活提醒信息,vo:{}",JSON.toJSONString(unregisteRemindVO));
        try {
            UnregisteRemind unregisteRemind = transVoToEntity(unregisteRemindVO);
            if(unregisteRemind==null)
                return null;
            
            unregisteRemind=unregisteRemindDao.save(unregisteRemind);
            if(unregisteRemind==null)
                return null;
            
            return transEntityToVo(unregisteRemind);
        } catch (Exception e) {
            logger.error("保存未激活提醒信息异常,vo:{},e:{}",JSON.toJSONString(unregisteRemindVO),e);
            return null;
        }
    }

    /**
     * vo对象转为实体类
     * 
     * @param FileVO
     * @return
     */
    private UnregisteRemind transVoToEntity(UnregisteRemindVO vo) {
        UnregisteRemind entity = new UnregisteRemind();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * 将实体类转换成vo对象
     * 
     * @param entity
     */
    private UnregisteRemindVO transEntityToVo(UnregisteRemind entity) {
        UnregisteRemindVO vo = new UnregisteRemindVO();
        try {
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            logger.error("实体对象转换VO异常", e);
            return null;
        }
    }
    
}
