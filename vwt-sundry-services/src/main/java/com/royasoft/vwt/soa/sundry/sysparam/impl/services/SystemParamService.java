package com.royasoft.vwt.soa.sundry.sysparam.impl.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.dubbo.config.annotation.Service;
import com.royasoft.vwt.soa.sundry.sysparam.api.interfaces.SystemParamInterface;

@Service(cluster = "failfast", timeout = 180000)
public class SystemParamService implements SystemParamInterface {

    private static final Logger logger = LoggerFactory.getLogger(SystemParamService.class);

    @Autowired
    private DruidDataSource dataSource;

    /**
     * 条件查询企业参数
     * 
     * @param corpId
     * @param parameterValue
     * @return
     * @Description:
     */
    public List<Map<String, Object>> getSysCorpParamter(String corpId, String parameterValue) {
        logger.debug("条件查询企业参数,corpId:{},parameterValue:{}", corpId, parameterValue);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT * FROM vwt_corp_parameter WHERE corpId =? and parameterValue=?");
            ps.setString(1, corpId);
            ps.setString(2, parameterValue);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", rs.getLong(1));
                map.put("corpId", rs.getString(2));
                map.put("parameterValue", rs.getString(3));
                map.put("setTime", rs.getString(4));
                map.put("times", rs.getString(5));
                list.add(map);
            }
        } catch (SQLException e) {
            logger.error("条件查询企业参数异常",e);
            return null;
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
        return list;
    }

    /**
     * 获取所有系统参数
     * 
     * @return
     * @Description:
     */
    public List<Map<String, Object>> getAllSysParamter() {
        logger.debug("获取所有系统参数");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT * FROM vwt_sys_parameter");
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", rs.getLong(1));
                map.put("parameterCode", rs.getString(2));
                map.put("parameterValue", rs.getString(3));
                map.put("times", rs.getString(4));
                list.add(map);
            }
        } catch (SQLException e) {
            return null;
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
        return list;
    }

}
