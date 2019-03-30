package com.royasoft.vwt.soa.sundry.vwtshare.api.interfaces;

import java.util.List;
import java.util.Map;

public interface VwtShareInterface {

    /**
     * 查询一条分享成功表
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public Map<String, Object> selectOneVwtShare(String telNum);

    /**
     * 根据手机号查询所属地市
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public String selectRegionByTelNum(String telNum);

    /**
     * 修改分享成功表
     * 
     * @param by_region
     * @param active_time
     * @param type
     * @return
     * @Description:
     */
    public boolean updateVwtShare(String id, String by_region, String active_time, String type);

    /**
     * 查询营销活动
     * 
     * @param type
     * @param nowTime
     * @return
     * @Description:
     */
    public Map<String, Object> getMarketActivity(String type, String nowTime);

    /**
     * 获取所有移动手机号码段
     * 
     * @return
     * @Description:
     */
    public List<String> getAllMobileNum();

    /**
     * 获取所有许可范围内手机号
     * 
     * @return
     * @Description:
     */
    public List<String> getAllRangeMobileNum();

    /**
     * 保存营销活动参与记录
     * 
     * @param smsId
     * @param type
     * @param telNum
     * @param optTime
     * @return
     * @Description:
     */
    public boolean insertMarketSmsCount(String smsId, String type, String telNum, String optTime);

    /**
     * 保存掌厅下载计数
     * 
     * @param by_region
     * @param active_time
     * @param type
     * @return
     * @Description:
     */
    public boolean saveVwtDownloadCount(String dicth, String clientType);

}
