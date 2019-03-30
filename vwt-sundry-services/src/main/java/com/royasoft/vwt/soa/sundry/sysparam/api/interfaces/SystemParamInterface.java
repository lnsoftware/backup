package com.royasoft.vwt.soa.sundry.sysparam.api.interfaces;

import java.util.List;
import java.util.Map;

public interface SystemParamInterface {

    /**
     * 条件查询企业参数
     * 
     * @param corpId
     * @param parameterValue
     * @return
     * @Description:
     */
    public List<Map<String, Object>> getSysCorpParamter(String corpId, String parameterValue);

    /**
     * 获取所有系统参数
     * 
     * @return
     * @Description:
     */
    public List<Map<String, Object>> getAllSysParamter();

}
