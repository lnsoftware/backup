package com.royasoft.vwt.cag.vo;
import java.io.Serializable;

/**
 * 服务号推送实体类
 * 
 * @author jxue
 * 
 */
public class ServicePush implements Serializable {

    private static final long serialVersionUID = -5771271944512105032L;

    /** 类型为:text(文本)、news(图文) */
    private String msgType;

    /** 创建时间 */
    private Long createTime;

    /** 图文数量 */
    private int articleCount;

    /** 内容 */
    private Object content;

    /** 服务号id */
    private String serviceId;

    /** 服务号名称 */
    private String serviceName;

    /** 服务号类型 */
    private String serviceType;

    /** 是否系统应用，1为系统应用，2为企 */
    private int isSystemApp;
    
    /** 消息id */
    private String msgUUID;

    /** 主标题 */
    private String mainTitle;

    /** logo地址*/
    private String logoAddr;
    
    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getMsgUUID() {
        return msgUUID;
    }

    public void setMsgUUID(String msgUUID) {
        this.msgUUID = msgUUID;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public String getLogoAddr() {
        return logoAddr;
    }

    public void setLogoAddr(String logoAddr) {
        this.logoAddr = logoAddr;
    }

    public int getIsSystemApp() {
        return isSystemApp;
    }

    public void setIsSystemApp(int isSystemApp) {
        this.isSystemApp = isSystemApp;
    }
}
