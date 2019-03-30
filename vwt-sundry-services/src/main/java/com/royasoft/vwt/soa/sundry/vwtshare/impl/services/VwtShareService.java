package com.royasoft.vwt.soa.sundry.vwtshare.impl.services;

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
import com.royasoft.vwt.soa.sundry.vwtshare.api.interfaces.VwtShareInterface;

/**
 * vwt分享相关实现
 *
 * @Author:MB
 * @Since:2016年4月21日
 */
@Service(cluster = "failfast", timeout = 180000)
public class VwtShareService implements VwtShareInterface {

    private static final Logger logger = LoggerFactory.getLogger(VwtShareService.class);

    @Autowired
    private DruidDataSource dataSource;

    /**
     * 查询一条分享成功表
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public Map<String, Object> selectOneVwtShare(String telNum) {
        logger.debug("查询一条分享成功表,telNum:{}", telNum);
        if (null == telNum || "".equals(telNum))
            return null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select * from vwt_share where type=0 and by_inviter=? order by insert_time");
            ps.setString(1, telNum);
            rs = ps.executeQuery();
            logger.debug("查询一条分享成功表,telNum:{},rs:{}", telNum, rs.getRow());
            while (rs.next()) {
                map = new HashMap<String, Object>();
                map.put("id", rs.getString(1));
                map.put("inviter", rs.getString(2));
                map.put("inviter_region", rs.getString(3));
                map.put("channel", rs.getString(4));
                map.put("insert_time", rs.getString(5));
                map.put("by_inviter", rs.getString(6));
                map.put("by_region", rs.getString(7));
                map.put("active_time", rs.getString(8));
                map.put("type", rs.getInt(9));
                return map;
            }
            return null;
        } catch (SQLException e) {
            logger.error("查询一条分享成功表,查询异常", e);
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
    }

    /**
     * 根据手机号查询所属地市
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public String selectRegionByTelNum(String telNum) {
        return null;
    }

    /**
     * 修改分享成功表
     * 
     * @param by_region
     * @param active_time
     * @param type
     * @return
     * @Description:
     */
    public boolean updateVwtShare(String id, String by_region, String active_time, String type) {
        logger.debug("修改分享成功表,by_region:{},active_time:{},type:{}", by_region, active_time, type);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("update vwt_share set by_region=?,active_time=?,type=? where id=?");
            ps.setString(1, by_region);
            ps.setString(2, active_time);
            ps.setInt(3, Integer.valueOf(type));
            ps.setString(4, id);
            ps.execute();
        } catch (SQLException e) {
            logger.error("修改分享成功表异常", e);
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
     * 查询营销活动
     * 
     * @param type
     * @param nowTime
     * @return
     * @Description:
     */
    public Map<String, Object> getMarketActivity(String type, String nowTime) {
        logger.debug("查询营销活动,type:{},nowTime:{}", type, nowTime);
        if (null == type || "".equals(type) || null == nowTime || "".equals(nowTime))
            return null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select id,allowSms,smsContent from vwt_market_sms where ?>=startTime and endTime>? and type =?");
            ps.setString(1, nowTime);
            ps.setString(2, nowTime);
            ps.setString(3, type);
            rs = ps.executeQuery();
            logger.debug("查询营销活动,type:{},nowTime:{},rs:{}", type, nowTime, rs.getRow());
            while (rs.next()) {
                map = new HashMap<String, Object>();
                map.put("id", rs.getString(1));
                map.put("allowSms", rs.getString(2));
                map.put("smsContent", rs.getString(3));
            }
        } catch (SQLException e) {
            logger.error("查询营销活动异常", e);
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
        return map;
    }

    /**
     * 获取所有移动手机号码段
     * 
     * @return
     * @Description:
     */
    public List<String> getAllMobileNum() {
        logger.debug("获取所有移动手机号码段");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select numberRange from vwt_cities_number WHERE 1=1");
            rs = ps.executeQuery();
            logger.debug("获取所有移动手机号码段,rs:{}", rs.getRow());
            if (rs.next()) {
                list = new ArrayList<String>();
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }

        } catch (SQLException e) {
            logger.error("获取所有移动手机号码段异常", e);
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
     * 获取所有许可范围内手机号
     * 
     * @return
     * @Description:
     */
    public List<String> getAllRangeMobileNum() {
        logger.debug("获取所有许可范围内手机号");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select a.numberRange from vwt_cities_number a left join vwt_cities_name_number b on a.cityCode=b.cityCode where b.isUse=1");
            rs = ps.executeQuery();
            logger.debug("获取所有许可范围内手机号,rs:{}", rs.getRow());
            if (rs.next()) {
                list = new ArrayList<String>();
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }

        } catch (SQLException e) {
            logger.error("获取所有许可范围内手机号异常", e);
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
     * 保存营销活动参与记录
     * 
     * @param smsId
     * @param type
     * @param telNum
     * @param optTime
     * @return
     * @Description:
     */
    public boolean insertMarketSmsCount(String smsId, String type, String telNum, String optTime) {
        logger.debug("保存营销活动参与记录,smsId:{},type:{},telNum:{},optTime:{}", smsId, type, telNum, optTime);
        if (null == smsId || "".equals(smsId) || null == type || "".equals(type) || null == telNum || "".equals(telNum) || null == optTime || "".equals(optTime))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("insert into vwt_market_sms_count (smsId,type,telNum,optTime) values (?,?,?,?)");
            ps.setString(1, smsId);
            ps.setString(2, type);
            ps.setString(3, telNum);
            ps.setString(4, optTime);
            ps.execute();
        } catch (SQLException e) {
            logger.error("保存营销活动参与记录异常", e);
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
     * 保存掌厅下载计数
     * 
     * @param by_region
     * @param active_time
     * @param type
     * @return
     * @Description:
     */
    public boolean saveVwtDownloadCount(String dicth, String clientType) {
        logger.debug("保存掌厅下载计数,dicth:{},clientType:{}", dicth, clientType);
        if (null == dicth || "".equals(dicth) || null == clientType || "".equals(clientType))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        try {
            con = dataSource.getConnection();

            ps1 = con.prepareStatement("select count(0) from vwt_download_count where ditch=?");
            
            ps1.setString(1, dicth);
            rs1 = ps1.executeQuery();
            while (rs1.next()) {
                int count = rs1.getInt(1);
                if (count == 0) {
                    if (clientType.equalsIgnoreCase("ios")) {
                        ps = con.prepareStatement("INSERT INTO vwt_download_count (ditch,downloadapk,downloadios) values (?,0,1) ");
                    } else {
                        ps = con.prepareStatement("INSERT INTO vwt_download_count (ditch,downloadapk,downloadios) values (?,1,0) ");
                    }
                } else {
                    if (clientType.equalsIgnoreCase("ios")) {
                        ps = con.prepareStatement(" UPDATE vwt_download_count set downloadios=downloadios+1 where ditch=?");
                    } else {
                        ps = con.prepareStatement(" UPDATE vwt_download_count set downloadapk=downloadapk+1 where ditch=?");
                    }
                }
                ps.setString(1, dicth);
                ps.execute();
            }
        } catch (SQLException e) {
            logger.error("保存掌厅下载计数异常", e);
            return false;
        } finally {
            try {
                if (rs1 != null)
                    rs1.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
                if (ps1 != null)
                    ps1.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

}
