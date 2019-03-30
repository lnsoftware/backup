package com.royasoft.vwt.controller.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 问题反馈DTo
 * 
 * @author liujm
 *
 */
public class QuestionFeedBackDTO implements Serializable {

    private static final long serialVersionUID = 826174055593816237L;

    /** ID号，uuid */
    private String FBID;

    /** 是否登录，0未登录，1已登录 */
    private String isLogin;

    /** 模块大类；11：登录；12：消息：13：通讯录；14：工作；15：我的； */
    private String moduleCode;

    /**
     * 模块大类下的功能分类：第一大类 登录下 1101：账号与安全；1102：手势密码； 第二大类 消息下 1201：聊天；1202：群聊； 第三大类 通讯录下 1301：通讯录更新；1302：企业认证；1303：多身份用户； 第四大类 工作下 1401：公告；
     * 1402：任务；1403：邮箱；1404：电话会议；
     */
    private String functionCode;

    /** 模块大类名称 */
    private String moduleName;

    /** 功能分类名称 */
    private String functionName;

    /** 具体问题场景ID，用逗号隔开（如果选择问题场景则填写） */
    private String problemsceneID;

    /** 问题反馈编码 */
    private String FBCode;

    /** 用户ID号，已登录填写 */
    private String userId;

    /** 用户手机号码，未登录选填，已登录必填 */
    private String telNum;

    /** 用户姓名 */
    private String userName;

    /** 反馈问题 */
    private String FBquestion;

    /** 提问时间 */
    private String questionDate;

    /** 上传图片1 */
    private String img1;

    /** 上传图片2 */
    private String img2;

    /** 上传图片3 */
    private String img3;

    /** 上传图片4 */
    private String img4;

    /** 上传图片5 */
    private String img5;

    /** 问题场景名称 */
    private List<Map<String, String>> problemSceneList;

    /** 反馈人id */
    private String FBUserid;

    /** 反馈内容 */
    private String FBContent;

    /** 反馈时间 */
    private Date FBDate;

    /** 反馈状态；0未反馈；1已反馈； */
    private String FBFlag;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private String createId;

    /** 创建时间 */
    private Date createTime;

    /** 修改人 */
    private String updateId;

    /** 修改时间 */
    private Date updateTime;

    /** 0:未删除 1:删除 */
    private Integer delFlag;
    
    /**客户经理名称*/
    private String customerName;
    
    /**客户经理电话号码*/
    private String customerTelnum;
    
    /**客户经理所属地市*/
    private String customerRegion;
    
    /**客户经理所属区县*/
    private String customerArea;
    
    /** 0:意见反馈 1:需求收集 */
    private String opinionType;
    
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerTelnum() {
        return customerTelnum;
    }

    public void setCustomerTelnum(String customerTelnum) {
        this.customerTelnum = customerTelnum;
    }

    public String getCustomerRegion() {
        return customerRegion;
    }

    public void setCustomerRegion(String customerRegion) {
        this.customerRegion = customerRegion;
    }

    public String getCustomerArea() {
        return customerArea;
    }

    public void setCustomerArea(String customerArea) {
        this.customerArea = customerArea;
    }

    public String getFBID() {
        return FBID;
    }

    public void setFBID(String fBID) {
        FBID = fBID;
    }

    public String getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(String isLogin) {
        this.isLogin = isLogin;
    }

    public String getFBFlag() {
        return FBFlag;
    }

    public void setFBFlag(String fBFlag) {
        FBFlag = fBFlag;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getFBCode() {
        return FBCode;
    }

    public void setFBCode(String fBCode) {
        FBCode = fBCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getFBquestion() {
        return FBquestion;
    }

    public void setFBquestion(String fBquestion) {
        FBquestion = fBquestion;
    }

    public String getQuestionDate() {
        return questionDate;
    }

    public void setQuestionDate(String questionDate) {
        this.questionDate = questionDate;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }

    public String getImg3() {
        return img3;
    }

    public void setImg3(String img3) {
        this.img3 = img3;
    }

    public String getImg4() {
        return img4;
    }

    public void setImg4(String img4) {
        this.img4 = img4;
    }

    public String getImg5() {
        return img5;
    }

    public void setImg5(String img5) {
        this.img5 = img5;
    }

    public String getFBUserid() {
        return FBUserid;
    }

    public void setFBUserid(String fBUserid) {
        FBUserid = fBUserid;
    }

    public String getFBContent() {
        return FBContent;
    }

    public void setFBContent(String fBContent) {
        FBContent = fBContent;
    }

    public Date getFBDate() {
        return FBDate;
    }

    public void setFBDate(Date fBDate) {
        FBDate = fBDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Map<String, String>> getProblemSceneList() {
        return problemSceneList;
    }

    public void setProblemSceneList(List<Map<String, String>> problemSceneList) {
        this.problemSceneList = problemSceneList;
    }

    public String getProblemsceneID() {
        return problemsceneID;
    }

    public void setProblemsceneID(String problemsceneID) {
        this.problemsceneID = problemsceneID;
    }

	public String getOpinionType() {
		return opinionType;
	}

	public void setOpinionType(String opinionType) {
		this.opinionType = opinionType;
	}

    
    
}
