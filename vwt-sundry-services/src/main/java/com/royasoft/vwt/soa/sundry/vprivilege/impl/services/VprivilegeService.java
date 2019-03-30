/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry.vprivilege.impl.services;

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
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.sundry.vprivilege.api.interfaces.VprivilegeInterface;

/**
 * V特权接口实现类
 * 
 * @author yujun
 * 
 * @Date 2015-6-10
 */
@Service(cluster = "failfast", timeout = 180000)
public class VprivilegeService implements VprivilegeInterface {

    private static final Logger logger = LoggerFactory.getLogger(VprivilegeService.class);

    @Autowired
    private RedisInterface redisInterface;

    @Autowired
    private DruidDataSource dataSource;

    /**
     * 查询V特权标题
     * 
     * 
     * @return
     */
    @Override
    public List<Map<String, Object>> getAllVPrivilegeTitile() {
        logger.debug("查询V特权所有标题");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(0);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select id,title,url from vwt_v_privilege_title order by sort ");
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", rs.getString(1));
                map.put("title", rs.getString(2));
                map.put("url", rs.getString(3));
                list.add(map);
            }
            logger.debug("查询V特权所有标题");
            return list;
        } catch (Exception e) {
            logger.error("查询V特权所有标题异常", e);
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
     * 查询V特权图片
     * 
     * 
     * @return
     */
    @Override
    public List<Map<String, Object>> getAllVPrivilegePictures() {
        logger.debug("查询V特权所有图片");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(0);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select id,title_id,title,pc_src,url from vwt_v_privilege_pictures order by sort ");
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", rs.getString(1));
                map.put("title_id", rs.getString(2));
                map.put("title", rs.getString(3));
                map.put("pc_src", rs.getString(4));
                map.put("url", rs.getString(5));
                list.add(map);
            }
            logger.debug("查询V特权所有图片");
            return list;
        } catch (Exception e) {
            logger.error("查询V特权所有图片异常", e);
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
    @SuppressWarnings("resource")
    public boolean saveVPrivilegeHis(String titleId, String picId, String telNum, String corpId) {
        logger.debug("记录V特权用户点击记录数,titleId:{},picId:{},telNum:{},corpId:{}", titleId, picId, telNum, corpId);
        if (null == telNum || telNum.equals("") || null == titleId || titleId.equals("") || null == picId || picId.equals("") || null == corpId || corpId.equals(""))
            return false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select count from vwt_v_privilege_his where pic_id=? and telnum=? and corpid=?");
            ps.setString(1, picId);
            ps.setString(2, telNum);
            ps.setString(3, corpId);
            rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
            logger.debug("根据图片id,手机号,企业id查询v特权记录数,picId:{},telNum:{},corpId:{}", picId, telNum, corpId, JSONObject.toJSONString(count));

            if (0 == count) {
                ps = con.prepareStatement("insert into vwt_v_privilege_his  VALUES(?,?,?,?,1,sysdate)");
                ps.setString(1, titleId);
                ps.setString(2, picId);
                ps.setString(3, telNum);
                ps.setString(4, corpId);
                ps.execute();
                logger.debug("新增V特权用户点击记录数,titleId:{},picId:{},telNum:{},corpId:{}", titleId, picId, telNum, corpId);
            } else {
                ps = con.prepareStatement("update vwt_v_privilege_his set count=?,updatetime=sysdate WHERE pic_id=? and telnum =? and corpid=? and count=?");
                ps.setInt(1, count + 1);
                ps.setString(2, picId);
                ps.setString(3, telNum);
                ps.setString(4, corpId);
                ps.setInt(5, count);
                ps.execute();
                logger.debug("增加V特权用户点击记录数,picId:{},telNum:{},corpId:{}", picId, telNum, corpId);
            }
        } catch (SQLException e) {
            logger.error("记录V特权用户点击记录数异常,titleId:{},picId:{},telNum:{},corpId:{},version:{}", titleId, picId, telNum, corpId, e);
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
