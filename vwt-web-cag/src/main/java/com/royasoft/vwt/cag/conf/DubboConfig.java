/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: DubboConfig.java
 * @Prject: vwt-base-services
 * @Package: com.royasoft.vwt.soa.base.config
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月3日 上午11:15:58
 * @version: V1.0
 */
package com.royasoft.vwt.cag.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.AnnotationBean;
import com.royasoft.vwt.base.dubbo.DubboUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.base.zookeeper.api.interfaces.ZookeeperInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceAnnexInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceContentInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceHisInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceInfoInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceReceiverInterface;
import com.royasoft.vwt.soa.business.blackLlist.api.interfaces.BlackListInterface;
import com.royasoft.vwt.soa.business.conllection.api.interfaces.CollectionInterface;
import com.royasoft.vwt.soa.business.festival.api.interfaces.FestivalInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.HlwCorpAuthInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.SmsSwitchInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImGroupInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImMessageInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImSquareInterface;
import com.royasoft.vwt.soa.business.imAttention.api.interfaces.ImAttentionInterface;
import com.royasoft.vwt.soa.business.invitationSystem.api.interfaces.QuestionFeedBackInterface;
import com.royasoft.vwt.soa.business.invite.api.interfaces.InviteShareInterface;
import com.royasoft.vwt.soa.business.login.api.interfaces.AlreadyLoginInterface;
import com.royasoft.vwt.soa.business.meeting.api.interfaces.MeetingInterface;
import com.royasoft.vwt.soa.business.sendtask.api.interfaces.WorkTaskInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.interfaces.SensitiveWordInterface;
import com.royasoft.vwt.soa.business.serviceCallBack.api.ServiceCallBackInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.GraphicPushInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.LabelInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareWelMsgInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.TwoDimensionalCodeInterface;
import com.royasoft.vwt.soa.business.squeareFeedback.api.interfaces.SqueareFeedbackInterface;
//wdw import com.royasoft.vwt.cag.util.IntegralUtil;
import com.royasoft.vwt.soa.business.urlmanage.api.interfaces.UrlManageInterface;
import com.royasoft.vwt.soa.business.vote.api.interfaces.VoteInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.SpecialUserInfoInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamFileInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamMessageInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamReplyInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamUserInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicSourceInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.IntegralSpendInterface;
import com.royasoft.vwt.soa.integral.api.interfaces.SignInterface;
import com.royasoft.vwt.soa.newssync.api.interfaces.NewsSyncInterface;
import com.royasoft.vwt.soa.sundry.addresscheck.api.interfaces.AddressCheckInterface;
import com.royasoft.vwt.soa.sundry.clientversion.api.interfaces.ClientVersionInterface;
import com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces.InsidePruchInterface;
import com.royasoft.vwt.soa.sundry.logmanager.api.interfaces.LogManagerInterface;
import com.royasoft.vwt.soa.sundry.memberactive.api.interfaces.MemberActiveInterface;
import com.royasoft.vwt.soa.sundry.sysparam.api.interfaces.SystemParamInterface;
import com.royasoft.vwt.soa.sundry.vprivilege.api.interfaces.VprivilegeInterface;
import com.royasoft.vwt.soa.systemsettings.gatedlaunch.api.interfaces.GatedlaunchInterface;
import com.royasoft.vwt.soa.systemsettings.mailconfig.api.interfaces.MailConfigInterface;
import com.royasoft.vwt.soa.systemsettings.msglist.api.MesListInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.AccountManagerInterface;
import com.royasoft.vwt.soa.systemsettings.versionupdate.api.interfaces.VersionPcUpdateInterface;
import com.royasoft.vwt.soa.systemsettings.versionupdate.api.interfaces.VersionupdateInterface;
import com.royasoft.vwt.soa.twolearn.api.interfaces.TwoLearnInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.clique.api.interfaces.CliqueInfoInterface;
import com.royasoft.vwt.soa.uic.contact.api.interfaces.ContactGroupInterface;
import com.royasoft.vwt.soa.uic.contact.api.interfaces.ContactInterface;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.AffiliationInterface;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartmentsInterface;
import com.royasoft.vwt.soa.uic.infofeedback.api.interfaces.InfoFeedbackInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.CWTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.PersonInfoLeaderSearchInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.XXTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.oaaccount.api.interfaces.OAaccountInfoInterface;
import com.royasoft.vwt.soa.uic.reservefield.api.interfaces.ReserveFieldInterface;

/**
 * @ClassName: DubboConfig
 * @Description: 配置dubbo
 * @author: xutf
 * @date: 2016年5月3日 上午11:15:58
 */

@Configuration
public class DubboConfig {

    private static final Logger logger = LoggerFactory.getLogger(DubboConfig.class);

    private static final String VERSION = "2.0.1";

 /*
  * by wdw
    @Bean
    public IntegralUtil integralUtil(DubboUtil dubboUtil) {
        return dubboUtil.getReference(IntegralUtil.class, VERSION);
    }
*/    
    @Bean
    public AnnotationBean annotationBean() {
        AnnotationBean annotationBean = new AnnotationBean();
        annotationBean.setPackage("com.royasoft.vwt");
        return annotationBean;
    }

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig("dubbo_interface_cag_comsumer");
        logger.info("{}启动中...", applicationConfig.getName());
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig(String zkUrl) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol("zookeeper");
        registryConfig.setClient("curator");
        registryConfig.setAddress(zkUrl);
        registryConfig.setCheck(true);
        registryConfig.setTimeout(60000);
        logger.info("zookeeperd地址：{}", registryConfig.getAddress());
        return registryConfig;
    }

    @Bean
    public DubboUtil dubboUtil(ApplicationConfig applicationConfig, RegistryConfig registryConfig) {
        return new DubboUtil(applicationConfig, registryConfig);
    }

    @Bean
    public WorkTeamFileInterface workTeamFileInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTeamFileInterface.class, VERSION);
    }

    @Bean
    public WorkTeamUserInterface workTeamUserInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTeamUserInterface.class, VERSION);
    }

    @Bean
    public WorkTeamMessageInterface workTeamMessageInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTeamMessageInterface.class, VERSION);
    }

    @Bean
    public WorkTeamReplyInterface workTeamReplyInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTeamReplyInterface.class, VERSION);
    }

    @Bean
    public WorkTeamInterface workTeamInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTeamInterface.class, VERSION);
    }

    @Bean
    public WorkTaskInterface workTaskInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(WorkTaskInterface.class, VERSION);
    }

    @Bean
    public DatabaseInterface databaseInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(DatabaseInterface.class, VERSION);
    }

    @Bean
    public ClientUserInterface clientUserInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ClientUserInterface.class, VERSION);
    }

    @Bean
    public GraphicPushInterface graphicPushInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(GraphicPushInterface.class, VERSION);
    }

    @Bean
    public RedisInterface redisInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(RedisInterface.class, VERSION);
    }

    @Bean
    public VprivilegeInterface vprivilegeInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(VprivilegeInterface.class, VERSION);
    }

    @Bean
    public ClientVersionInterface clientVersionInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ClientVersionInterface.class, VERSION);
    }

    @Bean
    public MemberInfoInterface memberInfoService(DubboUtil dubboUtil) {
        return dubboUtil.getReference(MemberInfoInterface.class, VERSION);
    }

    @Bean
    public CorpInterface corpService(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CorpInterface.class, VERSION);
    }

    @Bean
    public CliqueInfoInterface cliqueInfoService(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CliqueInfoInterface.class, VERSION);
    }

    @Bean
    public DepartMentInterface departMentInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(DepartMentInterface.class, VERSION);
    }

    @Bean
    public ReserveFieldInterface reserveFieldInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ReserveFieldInterface.class, VERSION);
    }

    @Bean
    public AnnounceAnnexInterface announceAnnexInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AnnounceAnnexInterface.class, VERSION);
    }

    @Bean
    public AnnounceContentInterface announceContentInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AnnounceContentInterface.class, VERSION);
    }

    @Bean
    public AnnounceHisInterface announceHisInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AnnounceHisInterface.class, VERSION);
    }

    @Bean
    public AnnounceInfoInterface announceInfoInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AnnounceInfoInterface.class, VERSION);
    }

    @Bean
    public AnnounceReceiverInterface announceReceiverInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AnnounceReceiverInterface.class, VERSION);
    }

    @Bean
    public SendSmsInterface sendSmsInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SendSmsInterface.class, VERSION);
    }

    @Bean
    public VerifyCodeInterface verifyCodeInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(VerifyCodeInterface.class, VERSION);
    }

    @Bean
    public ImRedisInterface imRedisInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ImRedisInterface.class, VERSION);
    }

    @Bean
    public SignInterface signInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SignInterface.class, VERSION);
    }

    @Bean
    public IntegralInterface integralInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(IntegralInterface.class, VERSION);
    }

    @Bean
    public MemberActiveInterface memberActiveInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(MemberActiveInterface.class, VERSION);
    }

    @Bean
    public DictionaryInterface dictionaryInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(DictionaryInterface.class, VERSION);
    }

    @Bean
    public AddressCheckInterface addressCheckInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AddressCheckInterface.class, VERSION);
    }

    @Bean
    public GatedlaunchInterface gatedlaunchInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(GatedlaunchInterface.class, VERSION);
    }

    @Bean
    public VersionupdateInterface versionupdateInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(VersionupdateInterface.class, VERSION);
    }
    
    @Bean
    public VersionPcUpdateInterface versionPcUpdateInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(VersionPcUpdateInterface.class, VERSION);
    }

    @Bean
    public SystemParamInterface systemParamInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SystemParamInterface.class, VERSION);
    }

    @Bean
    public InfoFeedbackInterface infoFeedbackInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(InfoFeedbackInterface.class, VERSION);
    }

    @Bean
    public LogManagerInterface logManagerInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(LogManagerInterface.class, VERSION);
    }

    @Bean
    public LabelInterface labelInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(LabelInterface.class, VERSION);
    }

    @Bean
    public ServiceCallBackInterface serviceCallBackInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ServiceCallBackInterface.class, VERSION);
    }

    @Bean
    public SquareInterface squareInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SquareInterface.class, VERSION);
    }

    @Bean
    public ZookeeperInterface zookeeperInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ZookeeperInterface.class, VERSION);
    }

    @Bean
    public InviteShareInterface inviteShareInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(InviteShareInterface.class, VERSION);
    }

    @Bean
    public XXTMemberInfoInterface xxtMemberInfoInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(XXTMemberInfoInterface.class, VERSION);
    }

    @Bean
    public CWTMemberInfoInterface cwtMemberInfoInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CWTMemberInfoInterface.class, VERSION);
    }

    @Bean
    public HLWMemberInfoInterface hlwMemberInfoInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(HLWMemberInfoInterface.class, VERSION);
    }

    @Bean
    public MeetingInterface meetingInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(MeetingInterface.class, VERSION);
    }

    @Bean
    public CustomerInterface customerInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CustomerInterface.class, VERSION);
    }

    @Bean
    public ContactInterface contactInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ContactInterface.class, VERSION);
    }

    @Bean
    public ContactGroupInterface contactGroupInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ContactGroupInterface.class, VERSION);
    }
    
    @Bean
    public QuestionFeedBackInterface questionFeedBackInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(QuestionFeedBackInterface.class, VERSION);
    }

    @Bean
    public HlwCorpAuthInterface hlwCorpAuthInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(HlwCorpAuthInterface.class, VERSION);
    }

    @Bean
    public MailConfigInterface mailconfigInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(MailConfigInterface.class, VERSION);
    }

    @Bean
    public VoteInterface voteInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(VoteInterface.class, VERSION);
    }

    @Bean
    public AffiliationInterface affiliationInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AffiliationInterface.class, VERSION);
    }

    @Bean
    public DepartmentsInterface departmentsInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(DepartmentsInterface.class, VERSION);
    }

    @Bean
    public PersonInfoLeaderSearchInterface personInfoLeaderSearchInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(PersonInfoLeaderSearchInterface.class, VERSION);
    }

    @Bean
    public AccountManagerInterface accountManagerInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AccountManagerInterface.class, VERSION);
    }

    @Bean
    public SmsSwitchInterface smsSwitchInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SmsSwitchInterface.class, VERSION);
    }

    @Bean
    public SendProvinceSmsInterface sendProvinceSmsInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SendProvinceSmsInterface.class, VERSION);
    }

    @Bean
    public CorpCustomInterface corpCustomInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CorpCustomInterface.class, VERSION);
    }

    @Bean
    public BlackListInterface blackListInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(BlackListInterface.class, VERSION);
    }

    @Bean
    public IntegralSpendInterface integralSpendInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(IntegralSpendInterface.class, VERSION);
    }

    @Bean
    public OAaccountInfoInterface oAaccountInfoInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(OAaccountInfoInterface.class, VERSION);
    }

    @Bean
    public TwoLearnInterface twoLearnInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(TwoLearnInterface.class, VERSION);
    }

    @Bean
    public CircleInterface circleInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CircleInterface.class, VERSION);
    }

    @Bean
    public SensitiveWordInterface sensitiveWordInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SensitiveWordInterface.class, VERSION);
    }

    @Bean
    public MesListInterface mesListInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(MesListInterface.class, VERSION);
    }

    @Bean
    public ImGroupInterface imGroupInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ImGroupInterface.class, VERSION);
    }

    @Bean
    public ImAttentionInterface imAttentionInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ImAttentionInterface.class, VERSION);
    }

    @Bean
    public ImMessageInterface imMessageInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ImMessageInterface.class, VERSION);
    }
    
    @Bean
    public ImSquareInterface imSquareInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(ImSquareInterface.class, VERSION);
    }
    
    @Bean
    public NewsSyncInterface newsSyncInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(NewsSyncInterface.class, VERSION);
    }
    
    @Bean
    public GraphicSourceInterface graphicSourceInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(GraphicSourceInterface.class, VERSION);
    }
    
    @Bean
    public FestivalInterface festivalInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(FestivalInterface.class, VERSION);
    }
    
    @Bean
    public SquareWelMsgInterface squareWelMsgInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SquareWelMsgInterface.class, VERSION);
    }

    @Bean
    public TwoDimensionalCodeInterface twoDimensionalCodeInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(TwoDimensionalCodeInterface.class, VERSION);
    }
    
    @Bean
    public AlreadyLoginInterface alreadyLoginInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(AlreadyLoginInterface.class, VERSION);
    }
    
    @Bean
    public CollectionInterface collectionInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(CollectionInterface.class, VERSION);
    }
    @Bean
    public SqueareFeedbackInterface SqueareFeedbackInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(SqueareFeedbackInterface.class, VERSION);
    }
    @Bean
    public UrlManageInterface urlManageInterface(DubboUtil dubboUtil) {
        return dubboUtil.getReference(UrlManageInterface.class, VERSION);
    }
    @Bean
    public SpecialUserInfoInterface specialUserInfoInterface(DubboUtil dubboUtil) {
    	return dubboUtil.getReference(SpecialUserInfoInterface.class, VERSION);
    }
    @Bean
    public InsidePruchInterface insidePruchInterface(DubboUtil dubboUtil) {
    	return dubboUtil.getReference(InsidePruchInterface.class, VERSION);
    }
}
