package com.royasoft.vwt.controller.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
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
    private XXTMemberInfoInterface XXTMemberInfoInterface;
    @Autowired
    private CWTMemberInfoInterface CWTMemberInfoInterface;
    @Autowired
    private HLWMemberInfoInterface HLWMemberInfoInterface;
    @Autowired
    private CorpInterface corpInterface;

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
            return CWTMemberInfoInterface.findCWTMemberByTelNum(telNum);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findXXTMemberByTelNum(telNum);
        else
            return HLWMemberInfoInterface.findHLWMemberByTelNum(telNum);
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
            return CWTMemberInfoInterface.findCWTById(userId);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findXXTById(userId);
        else
            return HLWMemberInfoInterface.findHLWById(userId);
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
        memberInfoVO = CWTMemberInfoInterface.findCWTById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        memberInfoVO = XXTMemberInfoInterface.findXXTById(userId);
        if (null != memberInfoVO)
            return memberInfoVO;
        memberInfoVO = HLWMemberInfoInterface.findHLWById(userId);
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
        List<MemberInfoVO> CWTList = CWTMemberInfoInterface.findCWTMemberByTelNum(telNum);
        if (null != CWTList && !CWTList.isEmpty())
            memberInfoVOs.addAll(CWTList);
        List<MemberInfoVO> XXTList = XXTMemberInfoInterface.findXXTMemberByTelNum(telNum);
        if (null != XXTList && !XXTList.isEmpty())
            memberInfoVOs.addAll(XXTList);
        List<MemberInfoVO> HLWList = HLWMemberInfoInterface.findHLWMemberByTelNum(telNum);
        if (null != HLWList && !HLWList.isEmpty())
            memberInfoVOs.addAll(HLWList);
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
        List<MemberInfoVO> CWTList = CWTMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != CWTList && !CWTList.isEmpty())
            memberInfoVOs.addAll(CWTList);
        List<MemberInfoVO> XXTList = XXTMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != XXTList && !XXTList.isEmpty())
            memberInfoVOs.addAll(XXTList);
        List<MemberInfoVO> HLWList = HLWMemberInfoInterface.findMemberInfoVoByCorpIdAndTelNum(corpId, telNum);
        if (null != HLWList && !HLWList.isEmpty())
            memberInfoVOs.addAll(HLWList);
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
            return CWTMemberInfoInterface.getMemberInfoCount(date, corpId);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.getMemberInfoCount(date, corpId);
        else
            return HLWMemberInfoInterface.getMemberInfoCount(date, corpId);
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
            return CWTMemberInfoInterface.findMaxDeleteTime(corpId);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findMaxDeleteTime(corpId);
        else
            return HLWMemberInfoInterface.findMaxDeleteTime(corpId);
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
            return CWTMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
        else
            return HLWMemberInfoInterface.findAddressBookByPage(page, rows, conditions);
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
            return CWTMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
        else
            return HLWMemberInfoInterface.findByOperationTimeAndCorpId(corpId, operationTime);
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
            return CWTMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else if (fromChannel == 5L)
            return XXTMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else
            return HLWMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
    }

    public MemberInfoVO saveMemberInfo(MemberInfoVO memberInfoVO, Long fromChannel) {
        if (null == memberInfoVO)
            return null;
        if (fromChannel == 1L || fromChannel == 6L)
            // return memberInfoInterface.save(memberInfoVO);
            return memberInfoInterface.saveNoChangeOpttime(memberInfoVO);
        else if (fromChannel == 4L)
            return null;
        // CWTMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else if (fromChannel == 5L)
            return null;
        // XXTMemberInfoInterface.findMemberInfoHisAllByPage(page, rows, conditions, sortMap);
        else
            // return HLWMemberInfoInterface.save(memberInfoVO);
            return HLWMemberInfoInterface.saveNoChangeOpttime(memberInfoVO);
    }

    public List<MemberInfoVO> findByFromchannel(Map<String, Object> conditions, Map<String, Boolean> sortMap, String fromChannel) {
        List<MemberInfoVO> list = new ArrayList<MemberInfoVO>();
        // 来源渠道:1：V网通,4：村务通,5：校讯通,6：通讯助手,7：互联网
        switch (fromChannel) {
            case "1":
                list = memberInfoInterface.findMemberInfoByCondition(conditions, sortMap);
                break;
            case "4":
                list = CWTMemberInfoInterface.findCWTMemberInfoByCondition(conditions, sortMap);
                break;
            case "5":
                list = XXTMemberInfoInterface.findXXTMemberInfoByCondition(conditions, sortMap);
                break;
            case "6":
                list = memberInfoInterface.findMemberInfoByCondition(conditions, sortMap);
                break;
            case "7":
                list = HLWMemberInfoInterface.findHLWMemberInfoByCondition(conditions, sortMap);
                break;
            default:
                break;
        }
        return list;
    }
}
