package com.royasoft.vwt.soa.sundry.memberactive.impl.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.dubbo.config.annotation.Service;
import com.royasoft.vwt.soa.sundry.memberactive.api.interfaces.MemberActiveInterface;

@Service(cluster = "failfast", timeout = 180000)
public class MemberActiveService implements MemberActiveInterface {

    private static final Logger logger = LoggerFactory.getLogger(MemberActiveService.class);

    @Autowired
    private DruidDataSource dataSource;

    /**
     * 保存激活信息
     * 
     * @param memberId
     * @param corpId
     * @return
     * @Description:
     */
    public boolean saveMemberActive(String memberId, String corpId) {
        logger.debug("保存激活信息,memberId:{},corpId:{}", memberId, corpId);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("insert into vwt_menber_active_info (menberId,pk_corp) values (?,?)");
            ps.setString(1, memberId);
            ps.setString(2, corpId);
            ps.execute();
        } catch (SQLException e) {
            logger.error("保存激活信息异常", e);
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
