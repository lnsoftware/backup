/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.clientversion.impl.services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.dubbo.config.annotation.Service;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.sundry.clientversion.api.interfaces.ClientVersionInterface;

/**
 * V特权接口实现类
 * 
 * @author yujun
 * 
 * @Date 2015-6-10
 */
@Service(cluster = "failfast", timeout = 180000)
public class ClientVersionService implements ClientVersionInterface {

    private static final Logger logger = LoggerFactory.getLogger(ClientVersionService.class);

    @Autowired
    private DruidDataSource dataSource;
    
    @Autowired
    private DatabaseInterface databaseInterface;

    /**
     * 根据手机号查询客户端版本信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public int selectTelNumAndVersionCount(String telNum) {
        logger.debug("根据手机号查询客户端版本信息,telNum:{}", telNum);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT COUNT(0) FROM vwt_telnum_versions WHERE telNum=?");
            ps.setString(1, telNum);
            rs = ps.executeQuery();
            int num = 0;
            while (rs.next()) {
                num = rs.getInt(1);
            }
            logger.debug("根据手机号查询客户端版本信息,telNum:{},num:{}", telNum, num);
            return num;
        } catch (Exception e) {
            logger.error("根据手机号查询客户端版本信息异常,telNum:{}", telNum, e);
            return 0;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * 保存客户端版本信息
     * 
     * @param telNum
     * @param version
     * @return
     * @Description:
     */
    public boolean insertTelNumAndVersion(String telNum, String version) {
        logger.debug("保存客户端版本信息,telNum:{},version:{}", telNum, version);
        if (null == telNum || telNum.equals("") || null == version || version.equals(""))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Long id=databaseInterface.generateId("vwt_telnum_versions", "id");
            con = dataSource.getConnection();
            ps = con.prepareStatement("insert into vwt_telnum_versions (id,telNum,versions) VALUES(?,?,?)");
            ps.setLong(1, id);
            ps.setString(2, telNum);
            ps.setString(3, version);
            ps.execute();
        } catch (SQLException e) {
            logger.error("保存客户端版本信息异常,telNum:{},version:{}", telNum, version, e);
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
        return true;

    }

    /**
     * 删除客户端版本信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public boolean deleteTelNumAndVersion(String telNum) {
        logger.debug("删除客户端版本信息,telNum:{}", telNum);
        if (null == telNum || telNum.equals(""))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("delete from vwt_telnum_versions where telNum = ?");
            ps.setString(1, telNum);
            ps.execute();
        } catch (SQLException e) {
            logger.error("删除客户端版本信息,telNum:{}", telNum, e);
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
        return true;

    }

    /**
     * 记录登录客户端设备信息
     * 
     * @param str
     * @return
     * @Description:
     */
    public boolean addLogonLog(String userName, String clientType, String clientVersion, String clientModel) {
        logger.debug("记录登录客户端设备信息,userName:{},clientType:{},clientVersion:{},clientModel:{}", userName, clientType, clientVersion, clientModel);
        if (null == userName || userName.equals("") || null == clientType || clientType.equals("") || null == clientVersion || clientVersion.equals("") || null == clientModel
                || "".equals(clientModel))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat  simple=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ps = con.prepareStatement("INSERT INTO vwt_logon_log (userName,clientType,logonTime,clientVersion,clientModel) values (?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?)");
            ps.setString(1, userName);
            ps.setString(2, clientType);
            ps.setString(3, simple.format(now));
            ps.setString(4, clientVersion);
            ps.setString(5, clientModel);
            ps.execute();
        } catch (SQLException e) {
            logger.error("记录登录客户端设备信息异常,userName:{},clientType:{},clientVersion:{},clientModel:{}", userName, clientType, clientVersion, clientModel, e);
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

}
