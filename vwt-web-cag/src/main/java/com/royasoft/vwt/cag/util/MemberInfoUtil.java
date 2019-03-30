package com.royasoft.vwt.cag.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.royasoft.vwt.soa.business.blackLlist.api.interfaces.BlackListInterface;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corp.api.vo.CorpVO;
import com.royasoft.vwt.soa.uic.member.api.interfaces.CWTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.XXTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.vo.MemberInfoVO;

@Component
public class MemberInfoUtil {

    private final static Logger logger = LoggerFactory.getLogger(MemberInfoUtil.class);

    @Autowired
    private MemberInfoInterface memberInfoInterface;
    @Autowired
    private XXTMemberInfoInterface xxtMemberInfoInterface;
    @Autowired
    private CWTMemberInfoInterface cwtMemberInfoInterface;
    @Autowired
    private HLWMemberInfoInterface hlwMemberInfoInterface;
    @Autowired
    private CorpInterface corpInterface;
    @Autowired
    private BlackListInterface blackListInterface;

    /**
     * 根据手机号和渠道号查询用户信息
     * 
     * @param telNum
     * @param fromChannel
     * @return
     * @Description:
     */
    public List<MemberInfoVO> findMemberInfosByTelNum(String telNum, Long fromChannel) {
        logger.debug("根据手机号和渠道号查询用户信息,telNum:{},fromChannel:{}", telNum, fromChannel);
        if (null == telNum || "".equals(telNum) || null == fromChannel || (fromChannel != 1L && fromChannel != 4L && fromChannel != 5L && fromChannel != 6L && fromChannel != 7L))
            return null;
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findByTelNum(telNum);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findCWTMemberByTelNum(telNum);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findXXTMemberByTelNum(telNum);
        else
            return hlwMemberInfoInterface.findHLWMemberByTelNum(telNum);
    }

    /**
     * 根据用户id和渠道号查询用户信息
     * 
     * @param userId
     * @param fromChannel
     * @return
     * @Description:
     */
    public MemberInfoVO findMemberInfoById(String userId, Long fromChannel) {
        logger.debug("根据用户id和渠道号查询用户信息,userId:{},fromChannel:{}", userId, fromChannel);
        if (null == userId || "".equals(userId) || null == fromChannel || (fromChannel != 1L && fromChannel != 4L && fromChannel != 5L && fromChannel != 6L && fromChannel != 7L))
            return null;
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findById(userId);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findCWTById(userId);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findXXTById(userId);
        else
            return hlwMemberInfoInterface.findHLWById(userId);
    }

    /**
     * 根据用户id查询用户信息
     * 
     * @param userId
     * @return
     * @Description:
     */
    public MemberInfoVO findMemberInfoById(String userId) {
        logger.debug("根据用户id查询用户信息,userId:{}", userId);
        if (null == userId || "".equals(userId))
            return null;
        MemberInfoVO memberInfoVO = null;
        memberInfoVO = memberInfoInterface.findById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        memberInfoVO = cwtMemberInfoInterface.findCWTById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        memberInfoVO = xxtMemberInfoInterface.findXXTById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        memberInfoVO = hlwMemberInfoInterface.findHLWById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        return null;
    }

    /**
     * 根据手机号查询用户信息
     * 
     * @param telNum
     * @return
     * @Description:
     */
    public List<MemberInfoVO> findMemberInfosByTelNum(String telNum) {
        logger.debug("根据手机号查询用户信息,telNum:{}", telNum);
        if (null == telNum || "".equals(telNum))
            return null;
        List<MemberInfoVO> memberInfoVOs = new ArrayList<MemberInfoVO>();
        List<MemberInfoVO> vwtList = memberInfoInterface.findByTelNum(telNum);
        if (null != vwtList && !vwtList.isEmpty())
            memberInfoVOs.addAll(vwtList);
        List<MemberInfoVO> cwtList = cwtMemberInfoInterface.findCWTMemberByTelNum(telNum);
        if (null != cwtList && !cwtList.isEmpty())
            memberInfoVOs.addAll(cwtList);
        List<MemberInfoVO> xxtList = xxtMemberInfoInterface.findXXTMemberByTelNum(telNum);
        if (null != xxtList && !xxtList.isEmpty())
            memberInfoVOs.addAll(xxtList);
        List<MemberInfoVO> hlwList = hlwMemberInfoInterface.findHLWMemberByTelNum(telNum);
        if (null != hlwList && !hlwList.isEmpty())
            memberInfoVOs.addAll(hlwList);
        logger.debug("根据手机号查询用户信息(返回),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        return memberInfoVOs;
    }

    /**
     * 根据手机号和企业id查询用户信息
     * 
     * @param corpId
     * @param telNum
     * @return
     * @Description:
     */
    public List<MemberInfoVO> findMemberInfoVoByCorpIdAndTelNum(String corpId, String telNum) {
        logger.debug("根据手机号和企业id查询用户信息,telNum:{},corpId:{}", telNum, corpId);
        if (null == telNum || "".equals(telNum) || null == corpId || "".equals(corpId))
            return null;
        List<MemberInfoVO> memberInfoVOs = new ArrayList<MemberInfoVO>();
        List<MemberInfoVO> vwtList = memberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != vwtList && !vwtList.isEmpty())
            memberInfoVOs.addAll(vwtList);
        List<MemberInfoVO> cwtList = cwtMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != cwtList && !cwtList.isEmpty())
            memberInfoVOs.addAll(cwtList);
        List<MemberInfoVO> xxtList = xxtMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != xxtList && !xxtList.isEmpty())
            memberInfoVOs.addAll(xxtList);
        List<MemberInfoVO> hlwList = hlwMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != hlwList && !hlwList.isEmpty())
            memberInfoVOs.addAll(hlwList);
        logger.debug("根据手机号和企业id查询用户信息(返回),telNum:{},memberInfoVOs:{}", telNum, JSON.toJSONString(memberInfoVOs));
        return memberInfoVOs;
    }

    /**
     * 查询通讯录表数据 acttime在date之后的数据
     * 
     * @param date
     * @param corpId
     * @return
     * @Description:
     */
    public Long getMemberInfoCount(String date, String corpId) {
        logger.debug("查询通讯录表数据 acttime在date之后的数据,date:{},corpId:{}", date, corpId);
        if (null == corpId || "".equals(corpId))
            return 0L;
        CorpVO corpVO = corpInterface.findById(corpId);
        logger.debug("查询通讯录表数据 acttime在date之后的数据,date:{},corpId:{},corpVO:{}", date, corpId, JSON.toJSONString(corpVO));
        if (null == corpVO)
            return 0L;
        Long fromChannel = corpVO.getFromchannel();
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.getMemberInfoCount(date, corpId);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.getMemberInfoCount(date, corpId);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.getMemberInfoCount(date, corpId);
        else
            return hlwMemberInfoInterface.getMemberInfoCount(date, corpId);
    }

    /**
     * 根据企业id查询最大人员表删除时间
     * 
     * @param date
     * @param corpId
     * @return
     * @Description:
     */
    public Date findMaxDeleteTime(String corpId) {
        logger.debug("根据企业id查询最大人员表删除时间,corpId:{}", corpId);
        if (null == corpId || "".equals(corpId))
            return null;
        CorpVO corpVO = corpInterface.findById(corpId);
        if (null == corpVO)
            return null;
        Long fromChannel = corpVO.getFromchannel();
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findMaxDeleteTime(corpId);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findMaxDeleteTime(corpId);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findMaxDeleteTime(corpId);
        else
            return hlwMemberInfoInterface.findMaxDeleteTime(corpId);
    }

    /**
     * 分页获取通讯录信息
     * 
     * @param page
     * @param rows
     * @param conditions
     * @return
     */
    public Map<String, Object> findAddressBookByPage(int page, int rows, Map<String, Object> conditions) {
        logger.debug("分页查询通讯录信息, page{}, rows{},conditions{},sortMap{}", page, rows, conditions);
        if (conditions.get("EQ_corpId") == null)
            return null;
        CorpVO corpVO = corpInterface.findById(conditions.get("EQ_corpId").toString());
        if (null == corpVO)
            return null;
        Long fromChannel = corpVO.getFromchannel();
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findAddressBookByPage(page, rows, conditions);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
        else
            return hlwMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
    }

    /**
     * 根据企业id 操作时间返回通讯录列表
     * 
     * @param corpId
     * @param operationTime
     * @return
     */
    public Map<String, MemberInfoVO> findByOperationTimeAndCorpId(String corpId, Date operationTime) {
        logger.debug("根据企业id 操作时间返回通讯录列表,corpId:{},operationTime:{}", corpId, operationTime);
        if (null == corpId || "".equals(corpId) || null == operationTime)
            return null;
        CorpVO corpVO = corpInterface.findById(corpId);
        if (null == corpVO)
            return null;
        Long fromChannel = corpVO.getFromchannel();
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
        else
            return hlwMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
    }

    /**
     * 分页查询通讯录历史表
     * 
     * @param page
     * @param rows
     * @param conditions
     * @param sortMap
     * @return
     */
    public Map<String, Object> findMemberInfoHisAllByPage(int page, int rows, Map<String, Object> conditions, Map<String, Boolean> sortMap) {
        logger.debug("分页查询memberinfoHis信息, page{}, rows{},conditions{},sortMap{}", page, rows, conditions, sortMap);
        if (conditions.get("EQ_corpId") == null)
            return null;
        CorpVO corpVO = corpInterface.findById(conditions.get("EQ_corpId").toString());
        if (null == corpVO)
            return null;
        Long fromChannel = corpVO.getFromchannel();
        if (fromChannel == 1L || fromChannel == 6L)
            return memberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else if (fromChannel == 4L)
            return cwtMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else if (fromChannel == 5L)
            return xxtMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else
            return hlwMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
    }

    public MemberInfoVO saveMemberInfo(MemberInfoVO memberInfoVO, Long fromChannel) {
        if (null == memberInfoVO)
            return null;
        if (fromChannel == 1L || fromChannel == 6L)
            // return memberInfoInterface.save(memberInfoVO);
            return memberInfoInterface.saveNoChangeOpttime(memberInfoVO);
        else if (fromChannel == 4L)
            return null;
        // cwtMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else if (fromChannel == 5L)
            return null;
        // xxtMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else
            // return hlwMemberInfoInterface.save(memberInfoVO);
            return hlwMemberInfoInterface.saveNoChangeOpttime(memberInfoVO);
    }

    /**
     * 根据部门ID 查询 部门下人员数
     * 
     * @param deptNum
     * @return
     */
    public Integer findMemberCountByDeptNum(String deptNum) {
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("EQ_deptId", deptNum);
        List<MemberInfoVO> memberList = hlwMemberInfoInterface.findHLWMemberInfoByCondition(conditions, null);
        logger.debug("根据部门ID 查询 部门下人员数deptNum:{},result={}", deptNum, memberList);
        if (null == memberList) {
            return 0;
        }
        return memberList.size();

    }

    /**
     * 校验用户是否在黑名单中
     * 
     * @param corpId
     * @param telnum
     * @return
     */
    public boolean checkIsInBlackList(String corpId, String telnum) {
        try {
            if (null != blackListInterface.findByCorpIdAndTelnum(corpId, telnum))
                return true;
        } catch (Exception e) {
            logger.error("判断用户是否在黑名单异常:e{}", e);
        }
        return false;
    }
}
