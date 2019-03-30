/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: ParamConfig.java
 * @Prject: vwt-web-controller
 * @Package: com.royasoft.vwt.controller.config
 * @Description: TODO
 * @author: xutf
 * @date: 2016年6月2日 下午3:56:06
 * @version: V1.0
 */
package com.royasoft.vwt.controller.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.util.BaseConstant;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;

/**
 * @ClassName: ParamConfig
 * @Description: 加载应用配置
 * @author: xutf
 * @date: 2016年6月2日 下午3:56:06
 */
@Component
public class ParamConfig {

    public static int port = 80;

    /** 文件服务器访问地址 */
    public static String FILE_SERVER_URL;

    public static String NGINX_ADDRESS;
    
    public static String GRAPHIC_SOURCE_URL;

    @Resource
    private ZkUtil zkUtil;

    @PostConstruct
    public void init() throws Exception {
        port = Integer.parseInt(zkUtil.findData(Constants.Param.ZK_PORT));
        FastDFSUtil.init(zkUtil.findData(BaseConstant.fastDFSADDR));
    }

}
