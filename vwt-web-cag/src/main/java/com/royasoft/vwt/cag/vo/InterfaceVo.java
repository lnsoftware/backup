package com.royasoft.vwt.cag.vo;

public class InterfaceVo {

    private String functionId;

    private String encodeType;

    private String requestUrl;

    public InterfaceVo(String functionId, String encodeType, String requestUrl) {
        super();
        this.functionId = functionId;
        this.encodeType = encodeType;
        this.requestUrl = requestUrl;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

}
