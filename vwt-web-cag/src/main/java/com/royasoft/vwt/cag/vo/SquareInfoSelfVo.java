package com.royasoft.vwt.cag.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.royasoft.vwt.soa.business.square.api.vo.SquareMenuVo;

/**
 * 用户可见应用VO
 *
 * @Author:MB
 * @Since:2016年4月13日
 */
public class SquareInfoSelfVo implements Serializable {

    private static final long serialVersionUID = -3572918641864627521L;

    private String id;
    /** 应用名称 */
    private String name;
    /** 应用图片 */
    private String logo;
    /** 应用类型。apk:1;HTML5:2;服务号:3 */
    private Integer type;
    /** 描述 */
    private String description;

    private String ftpUrl;

    private Date createTime;

    private String version;

    private String versionCode;

    private Long size;

    private String packageName;

    private int isSystemApp;

    private String publicImage1;
    private String publicImage2;
    private String publicImage3;
    private String publicImage4;
    /** 是否关注或收藏 未关注为0.已关注为1 */

    private int isAttend;

    private int isCancelAttention;

    private int preset;

    private Integer telNum;

    private Integer userId;

    private Integer channelNum;

    private Long sort;

    private String paramList;

    private int paramFlag;

    private String startParameter;

    private Integer isFreeLogin;// 是否免登陆。0否，1方案一(动态令?

    private String token;// 令牌值

    private String tokenUrl;// 获取令牌接口

    private String securityKey;// 安全身份密钥

    /** 菜单列表 */
    private List<SquareMenuVo> squareMenuVos;
    
    private String personalize;//-1是屏蔽，0是默认，1~6 是刷新，复制链接，反馈，发送给同事，分享至圈子，浏览器打开
    
    private Integer reconfirm ;// 二次确认 0否，1是

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SquareMenuVo> getSquareMenuVos() {
        return squareMenuVos;
    }

    public void setSquareMenuVos(List<SquareMenuVo> squareMenuVos) {
        this.squareMenuVos = squareMenuVos;
    }

    public int getPreset() {
        return preset;
    }

    public void setPreset(int preset) {
        this.preset = preset;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPublicImage1() {
        return publicImage1;
    }

    public void setPublicImage1(String publicImage1) {
        this.publicImage1 = publicImage1;
    }

    public String getPublicImage2() {
        return publicImage2;
    }

    public void setPublicImage2(String publicImage2) {
        this.publicImage2 = publicImage2;
    }

    public String getPublicImage3() {
        return publicImage3;
    }

    public void setPublicImage3(String publicImage3) {
        this.publicImage3 = publicImage3;
    }

    public String getPublicImage4() {
        return publicImage4;
    }

    public void setPublicImage4(String publicImage4) {
        this.publicImage4 = publicImage4;
    }

    public int getIsAttend() {
        return isAttend;
    }

    public void setIsAttend(int isAttend) {
        this.isAttend = isAttend;
    }

    public int getIsCancelAttention() {
        return isCancelAttention;
    }

    public void setIsCancelAttention(int isCancelAttention) {
        this.isCancelAttention = isCancelAttention;
    }

    public int getIsSystemApp() {
        return isSystemApp;
    }

    public void setIsSystemApp(int isSystemApp) {
        this.isSystemApp = isSystemApp;
    }

    public Integer getTelNum() {
        return telNum;
    }

    public void setTelNum(Integer telNum) {
        this.telNum = telNum;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(Integer channelNum) {
        this.channelNum = channelNum;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public String getParamList() {
        return paramList;
    }

    public void setParamList(String paramList) {
        this.paramList = paramList;
    }

    public int getParamFlag() {
        return paramFlag;
    }

    public void setParamFlag(int paramFlag) {
        this.paramFlag = paramFlag;
    }

    public String getStartParameter() {
        return startParameter;
    }

    public void setStartParameter(String startParameter) {
        this.startParameter = startParameter;
    }

    public Integer getIsFreeLogin() {
        return isFreeLogin;
    }

    public void setIsFreeLogin(Integer isFreeLogin) {
        this.isFreeLogin = isFreeLogin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

	public String getPersonalize() {
		return personalize;
	}

	public void setPersonalize(String personalize) {
		this.personalize = personalize;
	}

    public Integer getReconfirm() {
        return reconfirm;
    }

    public void setReconfirm(Integer reconfirm) {
        this.reconfirm = reconfirm;
    }
}
