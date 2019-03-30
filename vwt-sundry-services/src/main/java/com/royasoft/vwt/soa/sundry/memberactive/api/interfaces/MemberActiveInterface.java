package com.royasoft.vwt.soa.sundry.memberactive.api.interfaces;

public interface MemberActiveInterface {

    /**
     * 保存激活信息
     * 
     * @param memberId
     * @param corpId
     * @return
     * @Description:
     */
    public boolean saveMemberActive(String memberId, String corpId);

}
