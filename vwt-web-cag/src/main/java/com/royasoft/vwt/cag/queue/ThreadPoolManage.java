package com.royasoft.vwt.cag.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.royasoft.vwt.cag.server.HttpServer;
import com.royasoft.vwt.cag.service.AnnounceService;
import com.royasoft.vwt.cag.service.BeautyJSService;
import com.royasoft.vwt.cag.service.CollectionService;
import com.royasoft.vwt.cag.service.CorpCustomService;
import com.royasoft.vwt.cag.service.HlwAuthService;
import com.royasoft.vwt.cag.service.ImsAdressBookService;
import com.royasoft.vwt.cag.service.InsideBuyService;
import com.royasoft.vwt.cag.service.InsidePurchService;
import com.royasoft.vwt.cag.service.IntegralSignH5Service;
import com.royasoft.vwt.cag.service.IntegralSignService;
import com.royasoft.vwt.cag.service.InviteSystemService;
import com.royasoft.vwt.cag.service.LoginAuthService;
import com.royasoft.vwt.cag.service.MailConfigService;
import com.royasoft.vwt.cag.service.NoDisturbService;
import com.royasoft.vwt.cag.service.OAaccountService;
import com.royasoft.vwt.cag.service.QueueMonitorService;
import com.royasoft.vwt.cag.service.SendTaskServices;
import com.royasoft.vwt.cag.service.SensitiveWordService;
import com.royasoft.vwt.cag.service.SettingNewService;
import com.royasoft.vwt.cag.service.SettingService;
import com.royasoft.vwt.cag.service.ShanDongOAService;
import com.royasoft.vwt.cag.service.SqliteUpdateService;
import com.royasoft.vwt.cag.service.SquareFeedbackService;
import com.royasoft.vwt.cag.service.TwoLearnService;
import com.royasoft.vwt.cag.service.UrlManageService;
import com.royasoft.vwt.cag.service.VersionPcUpdateService;
import com.royasoft.vwt.cag.service.VersionUpdateService;
import com.royasoft.vwt.cag.service.VersionUpdateVGPService;
import com.royasoft.vwt.cag.service.VoteService;
import com.royasoft.vwt.cag.service.WorkBenchService;
import com.royasoft.vwt.cag.service.WorkTeamService;

public class ThreadPoolManage {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManage.class);

    /** 登录业务处理线程组 **/
    private ExecutorService loginAuthThreadPool = Executors.newCachedThreadPool();
    /** 工作圈业务处理线程组 **/
    private ExecutorService getWorkTeamMsgThreadPool = Executors.newCachedThreadPool();
    /** 任务业务处理线程组 **/
    private ExecutorService sendTaskThreadPool = Executors.newCachedThreadPool();
    /** 设置业务处理线程组 **/
    private ExecutorService settingThreadPool = Executors.newCachedThreadPool();
    /** 公告业务处理线程组 **/
    private ExecutorService announceThreadPool = Executors.newCachedThreadPool();
    /** 版本更新业务处理线程组 **/
    private ExecutorService versionThreadPool = Executors.newCachedThreadPool();
    /** PC版本更新业务处理线程组 **/
    private ExecutorService versionPcThreadPool = Executors.newCachedThreadPool();
    /** 版本更新业务处理线程组 **/
    private ExecutorService versionVGPThreadPool = Executors.newCachedThreadPool();
    /** 通讯录业务处理线程组 **/
    private ExecutorService addressThreadPool = Executors.newCachedThreadPool();
    /** 签到业务处理线程组 **/
    private ExecutorService integralThreadPool = Executors.newCachedThreadPool();
    /** 积分(H5)业务处理线程组 */
    private ExecutorService integralH5ThreadPool = Executors.newCachedThreadPool();
    /** 多角色工作台业务处理线程组 **/
    private ExecutorService workBenchThreadPool = Executors.newCachedThreadPool();
    /** 互联网企业认证业务处理线程组 **/
    private ExecutorService hlwAuthThreadPool = Executors.newCachedThreadPool();

    /** 邮箱设置处理线程组 **/
    private ExecutorService mailBoxThreadPool = Executors.newCachedThreadPool();
    /** 企业或部门logo处理线程组 **/
    private ExecutorService corpCustonThreadPool = Executors.newCachedThreadPool();

    /** 投票处理线程组 **/
    private ExecutorService voteThreadPool = Executors.newCachedThreadPool();

    /** 邀请体系业务处理线程 */
    private ExecutorService inviteSystemThreadPool = Executors.newCachedThreadPool();

    /** 2.1版本设置业务处理线程组 **/
    private ExecutorService settingNewThreadPool = Executors.newCachedThreadPool();

    /** IMS通讯录处理线程组 **/
    private ExecutorService IMSThreadPool = Executors.newCachedThreadPool();
    /** OA账号业务处理线程组 **/
    private ExecutorService OAaccountThreadPool = Executors.newCachedThreadPool();
    /** 二学一做业务处理线程组 */
    private ExecutorService twoLearnThreadPool = Executors.newCachedThreadPool();
    /** 美丽江苏业务处理线程组 */
    private ExecutorService beautyJSThreadPool = Executors.newCachedThreadPool();
    /** 敏感词业务处理线程组 */
    private ExecutorService sensitivewordThreadPool = Executors.newCachedThreadPool();
    /** 同步免打扰状态、单聊、群聊模块处理线程组 */
    private ExecutorService noDisturbThreadPool = Executors.newCachedThreadPool();
    
    /** 山东收藏模块处理线程组 */
    private ExecutorService conllectionThreadPool = Executors.newCachedThreadPool();
    
    /** 服务号反馈处理线程组 */
    private ExecutorService sqfeedbackThreadPool = Executors.newCachedThreadPool();
    
    /** 山东OA线程组 */
    private ExecutorService shandongOaThreadPool = Executors.newCachedThreadPool();
    
    /** 二维码url线程组 */
    private ExecutorService urlmanageThreadPool = Executors.newCachedThreadPool();
    
    /** 内购商品线程组 */
    private ExecutorService insidePurchThreadPool = Executors.newCachedThreadPool();

    public void initThreadPool() {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            settingThreadPool.execute(HttpServer.context.getBean(SettingService.class));
            getWorkTeamMsgThreadPool.execute(HttpServer.context.getBean(WorkTeamService.class));
            sendTaskThreadPool.execute(HttpServer.context.getBean(SendTaskServices.class));

            addressThreadPool.execute(HttpServer.context.getBean(SqliteUpdateService.class));
            loginAuthThreadPool.execute(HttpServer.context.getBean(LoginAuthService.class));
            versionThreadPool.execute(HttpServer.context.getBean(VersionUpdateService.class));
            versionPcThreadPool.execute(HttpServer.context.getBean(VersionPcUpdateService.class));
            versionVGPThreadPool.execute(HttpServer.context.getBean(VersionUpdateVGPService.class));
            integralThreadPool.execute(HttpServer.context.getBean(IntegralSignService.class));
            announceThreadPool.execute(HttpServer.context.getBean(AnnounceService.class));
            workBenchThreadPool.execute(HttpServer.context.getBean(WorkBenchService.class));
            inviteSystemThreadPool.execute(HttpServer.context.getBean(InviteSystemService.class));
            hlwAuthThreadPool.execute(HttpServer.context.getBean(HlwAuthService.class));
            mailBoxThreadPool.execute(HttpServer.context.getBean(MailConfigService.class));
            corpCustonThreadPool.execute(HttpServer.context.getBean(CorpCustomService.class));
            voteThreadPool.execute(HttpServer.context.getBean(VoteService.class));
            settingNewThreadPool.execute(HttpServer.context.getBean(SettingNewService.class));
            IMSThreadPool.execute(HttpServer.context.getBean(ImsAdressBookService.class));
            OAaccountThreadPool.execute(HttpServer.context.getBean(OAaccountService.class));
            twoLearnThreadPool.execute(HttpServer.context.getBean(TwoLearnService.class));
            beautyJSThreadPool.execute(HttpServer.context.getBean(BeautyJSService.class));
            sensitivewordThreadPool.execute(HttpServer.context.getBean(SensitiveWordService.class));
            noDisturbThreadPool.execute(HttpServer.context.getBean(NoDisturbService.class));
            conllectionThreadPool.execute(HttpServer.context.getBean(CollectionService.class));
            sqfeedbackThreadPool.execute(HttpServer.context.getBean(SquareFeedbackService.class));
            shandongOaThreadPool.execute(HttpServer.context.getBean(ShanDongOAService.class));
            urlmanageThreadPool.execute(HttpServer.context.getBean(UrlManageService.class));
            integralH5ThreadPool.execute(HttpServer.context.getBean(IntegralSignH5Service.class));
            insidePurchThreadPool.execute(HttpServer.context.getBean(InsidePurchService.class));
            insidePurchThreadPool.execute(HttpServer.context.getBean(InsideBuyService.class));
        }
        logger.info("cag server 初始化业务处理线程组成...");

        QueueMonitorService monitorService = HttpServer.context.getBean(QueueMonitorService.class);
        monitorService.startMonitor();
    }
}
