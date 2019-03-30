/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.vprivilege.api.interfaces;

import java.util.List;
import java.util.Map;

/**
 * V特权,由dubbo服务中心实现对应接口功能
 * 
 * @author yujun
 */
public interface VprivilegeInterface {

    /**
     * 查询V特权标题
     * 
     * 
     * @return
     */
    public List<Map<String, Object>> getAllVPrivilegeTitile();
    
    /**
     * 查询V特权图片
     * 
     * 
     * @return
     */
    public List<Map<String, Object>> getAllVPrivilegePictures();

    /**
     * 记录V特权用户点击记录数
     * 
     * @param titleId 点击特权栏目id
     * @param picId 点击特权图片id
     * @param telNum 手机号
     * @param corpId 企业id
     * @param count 点击次数
     * @param oldCount 点击次数
     * @return
     */
    public boolean saveVPrivilegeHis(String titleId, String picId, String telNum, String corpId);
}
