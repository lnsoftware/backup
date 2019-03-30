/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: LogbackLoadListener.java
 * @Prject: vwt-base
 * @Package: com.royasoft.vwt.soa.base
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 * @version: V1.0
 */
package com.royasoft.vwt.controller.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.royasoft.vwt.controller.service.AddressService;
import com.royasoft.vwt.controller.service.AnnounceService;
import com.royasoft.vwt.controller.service.BaiduUploadService;
import com.royasoft.vwt.controller.service.BaseExportService;
import com.royasoft.vwt.controller.service.CircleService;
import com.royasoft.vwt.controller.service.DeptManagerService;
import com.royasoft.vwt.controller.service.FestivalService;
import com.royasoft.vwt.controller.service.GraphicPushService;
import com.royasoft.vwt.controller.service.GraphicSourceService;
import com.royasoft.vwt.controller.service.ImGroupService;
import com.royasoft.vwt.controller.service.InsideBuyService;
import com.royasoft.vwt.controller.service.InsidePurchService;
import com.royasoft.vwt.controller.service.InternetAuthService;
import com.royasoft.vwt.controller.service.KeyWordsService;
import com.royasoft.vwt.controller.service.LoginAuthService;
import com.royasoft.vwt.controller.service.RedisManagerService;
import com.royasoft.vwt.controller.service.SquareFeedbackService;
import com.royasoft.vwt.controller.service.TwoLearnService;
import com.royasoft.vwt.controller.service.UrlManageService;
import com.royasoft.vwt.controller.service.UserFeedbackService;
import com.royasoft.vwt.controller.service.VoteService;
import com.royasoft.vwt.controller.service.WorkTeamService;

/**
 * @ClassName: LogbackLoadListener
 * @Description: 配置logback
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 */
public class ThreadPoolListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolListener.class);

    /** 登录业务处理线程组 **/
    private final ExecutorService loginAuthThreadPool = Executors.newCachedThreadPool();

    /** 图文推送处理线程组 **/
    private final ExecutorService graphicPushThreadPool = Executors.newCachedThreadPool();

    /** 用户反馈业务处理线程组 */
    private final ExecutorService userFeedbackThreadPool = Executors.newCachedThreadPool();

    /** 投票业务处理线程 */
    private final ExecutorService voteThreadPool = Executors.newCachedThreadPool();

    /** 互联网认证 */
    private final ExecutorService InternetAuthThreadPool = Executors.newCachedThreadPool();

    /** 通讯录相关处理线程 */
    private final ExecutorService addressThreadPool = Executors.newCachedThreadPool();

    /** 公告处理线程 */
    private final ExecutorService announceThreadPool = Executors.newCachedThreadPool();

    /** redis处理线程 */
    private final ExecutorService redisThreadPool = Executors.newCachedThreadPool();

    /** 素材中心处理线程 **/
    private final ExecutorService graphicSourceThreadPool = Executors.newCachedThreadPool();

    /** 工作圈黑名单处理线程 */
    private final ExecutorService workteamThreadPool = Executors.newCachedThreadPool();

    /** 两学一做处理线程 */
    private final ExecutorService twoLearnThreadPool = Executors.newCachedThreadPool();

    /** 圈子管理处理线程 */
    private final ExecutorService circleThreadPool = Executors.newCachedThreadPool();

    /** 关键词处理线程 */
    private final ExecutorService keyWordsThreadPool = Executors.newCachedThreadPool();

    /** 群聊 处理线程 */
    private final ExecutorService imGroupThreadPool = Executors.newCachedThreadPool();

    /** 百度富文本编辑器上传图片 处理线程 */
    private final ExecutorService baiduUploadThreadPool = Executors.newCachedThreadPool();
    
    /** 节日欢迎图 处理线程 */
    private final ExecutorService festivalPool = Executors.newCachedThreadPool();

    /** 服务号反馈处理线程组 */
    private ExecutorService sqfeedbackThreadPool = Executors.newCachedThreadPool();
    
    /** 导出Excel */
    private final ExecutorService exportThreadPool = Executors.newCachedThreadPool();
    
    /** 部门管理员队列处理线程 */
    private final ExecutorService deptManagerThreadPool = Executors.newCachedThreadPool();
    
    /** 二维码url */
    private final ExecutorService urlmanageThreadPool = Executors.newCachedThreadPool();
    
    /** 内购商品 */
    private final ExecutorService insidePurchThreadPool = Executors.newCachedThreadPool();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
                loginAuthThreadPool.execute(event.getApplicationContext().getBean(LoginAuthService.class));
                userFeedbackThreadPool.execute(event.getApplicationContext().getBean(UserFeedbackService.class));
                graphicPushThreadPool.execute(event.getApplicationContext().getBean(GraphicPushService.class));
                voteThreadPool.execute(event.getApplicationContext().getBean(VoteService.class));
                InternetAuthThreadPool.execute(event.getApplicationContext().getBean(InternetAuthService.class));
                addressThreadPool.execute(event.getApplicationContext().getBean(AddressService.class));
                announceThreadPool.execute(event.getApplicationContext().getBean(AnnounceService.class));
                redisThreadPool.execute(event.getApplicationContext().getBean(RedisManagerService.class));
                graphicSourceThreadPool.execute(event.getApplicationContext().getBean(GraphicSourceService.class));
                workteamThreadPool.execute(event.getApplicationContext().getBean(WorkTeamService.class));
                twoLearnThreadPool.execute(event.getApplicationContext().getBean(TwoLearnService.class));
                circleThreadPool.execute(event.getApplicationContext().getBean(CircleService.class));
                keyWordsThreadPool.execute(event.getApplicationContext().getBean(KeyWordsService.class));
                imGroupThreadPool.execute(event.getApplicationContext().getBean(ImGroupService.class));
                baiduUploadThreadPool.execute(event.getApplicationContext().getBean(BaiduUploadService.class));
                festivalPool.execute(event.getApplicationContext().getBean(FestivalService.class));
                sqfeedbackThreadPool.execute(event.getApplicationContext().getBean(SquareFeedbackService.class));
                exportThreadPool.execute(event.getApplicationContext().getBean(BaseExportService.class));
                urlmanageThreadPool.execute(event.getApplicationContext().getBean(UrlManageService.class));
                deptManagerThreadPool.execute(event.getApplicationContext().getBean(DeptManagerService.class));
                insidePurchThreadPool.execute(event.getApplicationContext().getBean(InsidePurchService.class));
				insidePurchThreadPool.execute(event.getApplicationContext().getBean(InsideBuyService.class));
            }
        } catch (Exception e) {
            logger.error("业务线程池启动出错：", e);
            System.exit(1);
        }
    }
}
