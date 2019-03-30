package com.royasoft.vwt.soa.sundry.addresscheck.impl.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.dubbo.config.annotation.Service;
import com.royasoft.vwt.soa.sundry.addresscheck.api.interfaces.AddressCheckInterface;

@Service(cluster = "failfast", timeout = 180000)
public class AddressCheckService implements AddressCheckInterface {

    private static final Logger logger = LoggerFactory.getLogger(AddressCheckService.class);

    @Autowired
    private DruidDataSource dataSource;

    /**
     * 验证通讯录是否正确
     * 
     * @param corpId
     * @param operationTime
     * @param sum
     * @return
     */
    @Override
    public boolean validateMemberData(String corpId, String memberTime, String enterTime, int memberCount, int enterCount) {
        logger.debug("验证通讯录是否正确,corpId:{},operationTime{},enterTime{},memberCount{},enterCount{}", corpId, memberTime, enterTime, memberCount, enterCount);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            int memberIdCount = 0;

            int enterIdCount = 0;

            con = dataSource.getConnection();
            ps = con.prepareStatement("select count(*) from vwt_member_info where corpid =? and operation_time <=to_date(?,'yyyy-mm-dd hh24:mi:ss')");
            ps.setString(1, corpId);
            ps.setString(2, memberTime);
            rs = ps.executeQuery();
            if (rs.next()) {
                memberIdCount = rs.getInt(1);
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("select count(0) from vwt_dept  where corpId =? and act_time <=to_date(?,'yyyy-mm-dd hh24:mi:ss')");
            ps.setString(1, corpId);
            ps.setString(2, enterTime);
            rs = ps.executeQuery();
            if (rs.next()) {
                enterIdCount = rs.getInt(1);
            }
            logger.debug("通讯录count{},部门count{}", memberIdCount, enterIdCount);

            if (memberCount == memberIdCount && enterCount == enterIdCount)
                return false;
            else
                return true;
        } catch (Exception e) {
            logger.error("验证通讯录是否正确,corpId:{},operationTime{},enterTime{},memberCount{},enterCount{}", corpId, memberTime, enterTime, memberCount, enterCount, e);
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
    }

}
