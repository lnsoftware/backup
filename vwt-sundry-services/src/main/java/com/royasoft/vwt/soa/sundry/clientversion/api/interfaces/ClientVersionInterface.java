/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.clientversion.api.interfaces;

/**
 * 客户端版本号接口
 *
 * @Author:MB
 * @Since:2016年3月31日
 */
public interface ClientVersionInterface {

    /**
     * 根据手机号查询客户端版本信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public int selectTelNumAndVersionCount(String telNum);

    /**
     * 保存客户端版本信息
     * 
     * @param telNum
     * @param version
     * @return
     * @Description:
     */
    public boolean insertTelNumAndVersion(String telNum, String version);

    /**
     * 删除客户端版本信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public boolean deleteTelNumAndVersion(String telNum);

    /**
     * 记录登录客户端设备信息
     * 
     * @param str
     * @return
     * @Description:
     */
    public boolean addLogonLog(String userName, String clientType, String clientVersion, String clientModel);
}
