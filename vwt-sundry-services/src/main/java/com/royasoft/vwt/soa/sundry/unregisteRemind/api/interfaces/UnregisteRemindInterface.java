package com.royasoft.vwt.soa.sundry.unregisteRemind.api.interfaces;

import com.royasoft.vwt.soa.sundry.unregisteRemind.api.vo.UnregisteRemindVO;

/**
 * 未激活提醒业务处理
 * @author daizl
 *
 */
public interface UnregisteRemindInterface {
    
    /**
     * 根据企业id查询
     * @param corpId
     * @return
     */
    public UnregisteRemindVO findByCorpId(String corpId);
    
    /**
     * 保存信息
     * @param unregisteRemindVO
     * @return
     */
    public UnregisteRemindVO save(UnregisteRemindVO unregisteRemindVO);
}
