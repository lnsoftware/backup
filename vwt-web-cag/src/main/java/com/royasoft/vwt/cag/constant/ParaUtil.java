/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.constant;

/**
 * 
 * @author ZHOUKQ
 *
 */
public class ParaUtil {
    public static final String SUCC_CODE = "200";
    public static final String ERROY_CODE_201 = "201";
    public static final String ERROY_CODE = "300";
    public static final String ERROY_CODE_301 = "301";
    public static final String ERROY_CODE_302 = "302";
    public static final String ERROY_CODE_303 = "303";
    public static final String ERROY_CODE_304 = "304";
    public static final String EXIT_CODE = "400";
    public static final String GROUP_ERROR_CODE = "500";
    public static final String ERROY_CODE_501 = "501";
    public static final String ERROY_CODE_502 = "502";
    public static final String ERROY_CODE_503 = "503";
    public static final String ERROY_CODE_504 = "504";
    public static final String ERROY_CODE_505 = "505";
    public static final String ERROY_CODE_506 = "506";
    public static final String ERROY_CODE_10001 = "10001";

    public static final String JSONFORMAT_ERROR = "JSON格式异常";
    public static final String WORKTEAM_USERNAME_ERROY = "userName参数为空";
    public static final String WORKTEAM_CIRCLEID_ERROY = "CIRCLEID参数为空";
    public static final String WORKTEAM_GETCIRCLEID_ERROY = "获取CIRCLEID参数失败";
    public static final String WORKTEAM_PKCORPID_ERROY = "查询企业Id异常";
    public static final String WORKTEAM_MSGLIST_ERROR = "获取工作圈列表消息异常";
    public static final String WORKTEAM_MSGLIST_SUCCESS = "获取工作圈列表消息成功";

    public static final String WORKTEAM_MYMSGLIST_ERROR = "获取工作圈个人说说异常";
    public static final String WORKTEAM_MYMSGLIST_SUCC = "获取工作圈个人说说成功";
    public static final String WORKTEAM_SENDPHONEISNULL = "发送者手机号为空";
    public static final String WORKTEAM_SENDCONTENTISNULL = "发送内容为空";
    public static final String WORKTEAM_MENBERINFOISNULL = "没找到对应的用户";
    public static final String WORKTEAM_MSGSAVE_ERROR = "说说数据保存失败";
    public static final String WORKTEAM_MSGSAVE_SUCC = "说说数据保存成功";
    public static final String WORKTEAM_MSGFILESAVE_ERROR = "说说附件数据保存失败";

    public static final String WORKTEAM_MSGDEL_SUCC = "删除工作圈消息成功";
    public static final String WORKTEAM_MSGDEL_ERROR = "删除工作圈消息失败";
    public static final String WORKTEAM_MSGDEL_USERIDISNULL = "消息ID为空";
    public static final String PARM_VALIDATION_ERROR = "参数校验失败";
    public static final String WORKTEAM_MSGISNULL = "指定Id的说说不存在";
    public static final String WORKTEAM_REPLYISNULL = "指定Id的回复不存在";
    public static final String WORKTEAM_MSGISNOTREPLY = "用户不是指定id回复的人";
    public static final String WORKTEAM_MSGISNOTSENDER = "用户不是说说发表人";
    public static final String TPYECHANGE_ERROR = "类型转换异常";
    public static final String WORKTEAM_REPLY_ERROR = "评论发表失败";
    public static final String WORKTEAM_REPLY_SUCC = "评论成功！";
    public static final String WORKTEAM_PRISE_ERROR = "赞失败";
    public static final String WORKTEAM_PRISE_SUCC = "赞成功！";
    public static final String WORKTEAM_CANCLEPRISE_ERROR = "取消赞失败";
    public static final String WORKTEAM_CANCLEREPLY_ERROR = "取消回复失败";
    public static final String WORKTEAM_CANCLEPRISE_SUCC = "取消赞成功！";
    public static final String WORKTEAM_CANCLEREPLY_SUCC = "取消回复成功！";

    public static final String WORKTEAM_MSGINFO_SUCC = "工作圈消息详情查询成功！";
    public static final String WORKTEAM_MSGINFO_ERROR = "工作圈消息详情查询失败！";

    public static final String GETCIRCLE_SUCC="获取圈子信息成功";
    public static final String GETCIRCLE_ERROR="获取圈子信息异常";
    
    public static final String REGE_ERROY_MSG_302 = "用户ID为空!";
    public static final String SENDTASK_USERNAME_ISNULL = "任务发起人姓名为空";
    public static final String SENDTASK_TYPE_ISNULL = "任务类型为空";
    public static final String SENDTASK_CONTENT_ISNULL = "任务内容为空";
    public static final String SENDTASK_NUMBER_MORELIMIT = "任务接收人数量超出限制";
    public static final String SENDTASK_NUMBER_ERROR = "获取任务人数限制数量出错";
    public static final String SENDTASK_FILELENGTH_MORE5M = "附件大小超过5M";
    public static final String SENDTASK_SAVEFILE_ERROR = "附件保存异常";
    public static final String CREATE_TASK_MSG = "创建任务成功!";
    public static final String CREATE_EDITTASK_MSG = "编辑任务成功!";
    public static final String CREATE_TASK_EROY_MSG = "创建任务失败!";
    public static final String GET_TASKLIST_ERROR = "获取任务列表类失败！";
    public static final String GET_TASKLIST_SUCC = "获取任务列表类成功！";
    public static final String TASKIDISNULL = "任务ID为空";
    public static final String TASK_FROMUSERID = "任务发起人为空";
    public static final String GET_TASKBYID_ERROR = "根据任务ID查询任务信息失败";
    public static final String END_TALK_ERR_MSG2 = "请求者不是任务发起人";
    public static final String ALREADY_ARCHIVED_TASK_NOEDIT = "已归档的任务不能编辑";
    public static final String TASK_ISNOTEXIST_MSG = "不存在此任务!";
    public static final String CANCLETASK_ERROR = "任务取消失败或不存在任务";
    public static final String CANCLETASK_SUCC = "任务取消成功";

    public static final String END_TASK_ERROR = "结束任务失败";
    public static final String END_TASK_SUCC = "结束任务成功";
    public static final String RECEIPT_TASKIDISNULL = "回执ID为空";
    public static final String NOEXEXIT_TASK = "不存在指定任务的回执";

    public static final String RECEIPT_TASK_ERROR = "发送回执失败";
    public static final String RECEIPT_TASK_SUCC = "发送回执成功";

    public static final String RECEIPT_READ_TASK_ERROR = "发送阅读回执失败";
    public static final String RECEIPT_READ_TASK_SUCC = "发送阅读回执成功";
    public static final String RECEIPT_TASK_STATIUS_IS0 = "指定回执状态为O";

    public static final String GET_RECEIPT_TASKLIST_ERROR = "查询回执列表数据时出错";
    public static final String GET_RECEIPT_TASKLIST_SUCC = "查询任务回执列表成功";

    public static final String CREATE_TALK_SUCC_MSG = "会话成功!";
    public static final String CREATE_TALK_EROY_MSG = "会话失败!";
    public static final String CREATE_TALK_SUS_MSG = "获取会话列表成功";
    public static final String CREATE_IMG_SUS_MSG = "S";
    public static final String CREATE_IMG_EROY_MSG = "N";
    public static final String CREATE_IMG_EXIT_MSG = "任务已经存在!";
    public static final String NULL_PHONENUMBER_MSG = "手机号码为空";
    public static final String END_TALK_ERR_MSG1 = "不存在此任务!";
    public static final String END_TALK_ERR_MSG3 = "任务回执未完成,无法结束!";
    public static final String END_TALK_MSG = "任务结束!";
    public static final String READ_TALK_SUCC_MSG = "已阅读!";
    
    
    public static final String COLLECTION_SUCC="处理收藏信息成功";
    public static final String COLLECTION_ERROR="处理收藏信息异常";
    public static final String COLLECTION_ERROY = "参数异常";
    public static final String SDTOKEN_ERROY = "获取token异常";
    public static final String SDTOKEN_ERROR="处理token异常";
    public static final String SDTOKEN_SUCC="处理token成功";
    public static final String COLLECTION_MEM_ERROY = "人员和收藏不匹配";
    
    public static final Integer IMTASK_TYPE_CREATE=1;
    public static final Integer IMTASK_TYPE_UPDATE=2;
    public static final Integer IMTASK_TYPE_CANCLE=3;
    
    public static String IMPORTANT_SERVICE_NO="";
    
    public static String titleListJson="";
    
    public static String pictureListJson="";

}
