/************************************************
 *  Copyright © 2002-2016 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 
 * 常量定义
 *
 * @Author:muqs
 */
public class BaseConstants {

    /** redis缓存 失效时间 单位 秒 */
    public static int redisExpire =2 * 60 * 60;
    /** 缓存命名空间,key为:ROYASOFT:VWT:OMC:SESSIONID:sessionId */
    public static final String nameSpace = "ROYASOFT:VWT:OMC:SESSIONID:";

    /**
     * FastDFS 访问地址
     */
    public static String FASTDFS_URL;
    public static String NGINX_ADDRESS;
    public static String GRAPHIC_SOURCE_URL;

    /** zk上配置的redis集群地址 **/
    public static final String ZK_REDIS_HOME = "/royasoft/vwt/redis";
    /** zk上配置的jdbc信息 **/
    public static final String ZK_JDBC_HOME = "/royasoft/vwt/jdbc_user";

    /**
     * 保存上传文件夹名称
     */
    public static final String SAVE_FILE_DIR = "uploadfile";
    public static final String FILE_PATH = Thread.currentThread().getContextClassLoader().getResource("/").getPath();

    public static final String getUUID() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString();
        // uid = uid.replaceAll("-", "");
        return uid;
    }

    /**
     * 获取项目路径
     * 
     * @return
     */
    public static final String getContextRealPath() {
        int end = FILE_PATH.length() - "WEB-INF/classes/".length();
        String path = FILE_PATH.substring(1, end);
        path = path + SAVE_FILE_DIR;
        return path.replaceAll("%20", " ");
    }

    // 字典表相关常量
    public static final long DICT_ID_REGION = 51;
    public static final long DICT_ID_INDUSTRY = 52;// 行业
    /**
     * 地市
     */
    public static final String DICT_IDDESC_CITY = "地市";
    /**
     * 区域
     */
    public static final String DICT_IDDESC_AREA = "区域";
    /**
     * 省
     */
    public static final String DICT_IDDESC_PROVISION = "省";

    // 角色相关常量
    /**
     * 角色 - 系统管理员
     */
    public static final int ROLENAME_ADMIN_SYSTEM = 1;
    /**
     * 角色 - 平台管理员
     */
    public static final int ROLENAME_ADMIN_PLATFORM = 2;
    /**
     * 角色 - 企业管理员
     */
    public static final int ROLENAME_ADMIN_CORP = 3;
    /**
     * 角色 - 省公司管理员
     */
    public static final int ROLENAME_ADMIN_PROVINCE = 4;
    /**
     * 角色 - 地市公司管理员
     */
    public static final int ROLENAME_ADMIN_CITY = 5;
    /**
     * 角色 - 区县管理员
     */
    public static final int ROLENAME_ADMIN_AREA = 6;
    /**
     * 角色 -客户经理
     */
    public static final int ROLENAME_ADMIN_CUSTOMER = 7;
    
    /**
     * 角色 - 部门管理员
     */
    public static final int ROLENAME_ADMIN_DEPT = 8;

    // 操作相关常量
    public static final String ACTION_SUCCESS = "操作成功";
    public static final String ACTION_FAIL = "操作失败";
    public static final String ACTION_NO_EXIST = "数据同步中,请稍后重试";
    // session 用户名
    public static final String SESSION_USERNAME = "username";

    public static final String CURRENT_SYS_USER = "CURRENT_SYS_USER";

    // tree menu_type
    public static enum TREE_MENU_TYPE {
        group, module, leaf
    }

    /** 允许上传的图片类型 */
    public static final String INMAGETYPES = ".PNG.JPG.JPEG.png.jpg.jpeg";
    public static final String UPLOADIMGERROR = "请选择指定文件类型（.PNG|.JPG|.JPEG|.png|.jpg|.jpeg）";

    public static final String SYSTEM_CORPID = "0";// 系统对应企业Id
    // 应用,素材相关常量
    /**
     * 应用,素材 -系统对应企业Id
     */
    public static final String SQUARE_SYSTEM_CORPID = "0";// 系统对应企业Id

    /**
     * 应用,素材 -素材标题为主标题
     */
    public static final String GRAPHIC_IS_MAIN = "1";// 素材标题为主标题

    /**
     * 应用,素材 -素材标题为副标题
     */
    public static final String GRAPHIC_NOT_MAIN = "0";// 素材标题为副标题

    /**
     * 应用,素材 -素材链接类型为url
     */
    public static final String GRAPHIC_SOURCE_TYPE_URL = "1";// 素材链接类型为url

    /**
     * 应用,素材 -素材链接类型为自定义
     */
    public static final String GRAPHIC_SOURCE_TYPE_CUSTOM = "0";// 素材链接类型为自定义

    // 分页查询常量
    public static final int PAGEINDEX = 1;// 分页初始页数
    public static final int PAGESIZE = 10;// 分页初始行数
    // 角色对应字典表中的省级市区县
    /**
     * 角色对应字典表中的省级市区县,对应省级角色
     */
    public static final String DICT_AREA_PROVINCE = "4";
    /**
     * 角色对应字典表中的省级市区县,对应市级角色
     */
    public static final String DICT_AREA_CITY = "5";
    /**
     * 角色对应字典表中的省级市区县,对应区县角色
     */
    public static final String DICT_AREA_AREA = "6";

    /**
     * 一人多职，部门中存在该人员
     */
    public static final String PART_EXSIT_MEMBER = "该部门已存在此号码";

    /**
     * 一人多职，添加人员
     */
    public static final String EXSIT_MEMBER = "此号码已有其他职务";

    /**
     * jvm缓存角色
     * 
     */
    public static Map<String, Object> roleCache = new HashMap<String, Object>();

    /**
     * jvm缓存区域
     */
    public static Map<String, String> areaDictionalCache = new HashMap<String, String>();

    /**
     * dfs图片文件下载分割路径前段长度
     */
    public static final int DFS_BEFORE = 7;
    
    /** 四种允许上传的图片类型  */
    public static final String FOURINMAGETYPES = ".PNG.JPG.JPEG.png.jpg.jpeg.bmp.BMP";
    public static final String FOURUPLOADIMGERROR = "请选择指定文件类型（.PNG|.JPG|.JPEG|.png|.jpg|.jpeg|.bmp|.BMP）";

    /**
     * 工分商品排序字段redis缓存
     * 
     */
    public static final String VWT_JF_INTEGRALCOMMODITY_SORT = "VWT_JF_INTEGRALCOMMODITY_SORT";
}
