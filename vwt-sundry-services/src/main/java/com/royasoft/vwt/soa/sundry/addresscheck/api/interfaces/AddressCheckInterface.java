package com.royasoft.vwt.soa.sundry.addresscheck.api.interfaces;

public interface AddressCheckInterface {

    /**
     * 验证通讯录是否正确
     * 
     * @param corpId
     * @param operationTime
     * @param sum
     * @return
     */
    public boolean validateMemberData(String corpId, String memberTime, String enterTime, int memberCount, int enterCount);

}
