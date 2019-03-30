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
package com.royasoft.vwt.controller.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.royasoft.vwt.base.dubbo.DubboUtil;
import com.royasoft.vwt.soa.base.database.api.interfaces.DatabaseInterface;
import com.royasoft.vwt.soa.base.dictionary.api.interfaces.DictionaryInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.ImRedisInterface;
import com.royasoft.vwt.soa.base.redis.api.interfaces.RedisInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendProvinceSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.SendSmsInterface;
import com.royasoft.vwt.soa.base.sms.api.interfaces.VerifyCodeInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceAnnexInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceContentInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceHisInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceInfoInterface;
import com.royasoft.vwt.soa.business.announce.api.interfaces.AnnounceReceiverInterface;
import com.royasoft.vwt.soa.business.blackLlist.api.interfaces.BlackListInterface;
import com.royasoft.vwt.soa.business.export.api.ExportInterface;
import com.royasoft.vwt.soa.business.festival.api.interfaces.FestivalInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.HlwCorpAuthInterface;
import com.royasoft.vwt.soa.business.hlwAuth.api.interfaces.SmsSwitchInterface;
import com.royasoft.vwt.soa.business.im.api.interfaces.ImGroupInterface;
import com.royasoft.vwt.soa.business.industry.api.interfaces.IndustryManagerInterface;
import com.royasoft.vwt.soa.business.invitationSystem.api.interfaces.QuestionFeedBackInterface;
import com.royasoft.vwt.soa.business.materialRole.api.interfaces.DepartMentInterfaces;
import com.royasoft.vwt.soa.business.materialRole.api.interfaces.MemberInfoInterfaces;
import com.royasoft.vwt.soa.business.materialRole.api.interfaces.RoleInterface;
import com.royasoft.vwt.soa.business.sensitivewords.api.interfaces.SensitiveWordInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.DeptSquareInterface;
import com.royasoft.vwt.soa.business.square.api.interfaces.SquareInterface;
import com.royasoft.vwt.soa.business.squeareFeedback.api.interfaces.SqueareFeedbackInterface;
import com.royasoft.vwt.soa.business.urlmanage.api.interfaces.UrlManageInterface;
import com.royasoft.vwt.soa.business.vote.api.interfaces.VoteInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.CircleInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamMessageInterface;
import com.royasoft.vwt.soa.business.workteam.api.interfaces.WorkTeamReplyInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicPushInfoInterface;
import com.royasoft.vwt.soa.graphicpush.api.interfaces.GraphicSourceInterface;
import com.royasoft.vwt.soa.sundry.insidePurch.api.interfaces.InsidePruchInterface;
import com.royasoft.vwt.soa.sundry.unregisteRemind.api.interfaces.UnregisteRemindInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.AccountManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.MenuManagerInterface;
import com.royasoft.vwt.soa.systemsettings.platform.api.interfaces.RolePowerManagerInterface;
import com.royasoft.vwt.soa.twolearn.api.interfaces.TwoLearnInterface;
import com.royasoft.vwt.soa.uic.clientuser.api.interfaces.ClientUserInterface;
import com.royasoft.vwt.soa.uic.corp.api.interfaces.CorpInterface;
import com.royasoft.vwt.soa.uic.corpcustom.api.interfaces.CorpCustomInterface;
import com.royasoft.vwt.soa.uic.customer.api.interfaces.CustomerInterface;
import com.royasoft.vwt.soa.uic.depart.api.interfaces.DepartMentInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.CWTMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.HLWMemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.MemberInfoInterface;
import com.royasoft.vwt.soa.uic.member.api.interfaces.XXTMemberInfoInterface;

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

	@Bean
	public ApplicationConfig applicationConfig() {
		ApplicationConfig applicationConfig = new ApplicationConfig("dubbo_interface_controller");
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
	public DatabaseInterface databaseInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(DatabaseInterface.class, VERSION);
	}

	@Bean
	public RedisInterface redisInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(RedisInterface.class, VERSION);
	}

	@Bean
	public SendSmsInterface sendSmsInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(SendSmsInterface.class, VERSION);
	}

	@Bean
	public ImRedisInterface imRedisInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(ImRedisInterface.class, VERSION);
	}

	@Bean
	public VerifyCodeInterface verifyCodeInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(VerifyCodeInterface.class, VERSION);
	}

	@Bean
	public DictionaryInterface dictionaryInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(DictionaryInterface.class, VERSION);
	}

	@Bean
	public VoteInterface voteInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(VoteInterface.class, VERSION);
	}

	@Bean
	public UnregisteRemindInterface unregisteRemindInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(UnregisteRemindInterface.class, VERSION);
	}

	@Bean
	public DepartMentInterface departMentInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(DepartMentInterface.class, VERSION);
	}

	@Bean
	public ClientUserInterface clientUserInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(ClientUserInterface.class, VERSION);
	}

	@Bean
	public HlwCorpAuthInterface HlwCorpAuthInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(HlwCorpAuthInterface.class, VERSION);
	}

	@Bean
	public CorpInterface corpInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(CorpInterface.class, VERSION);
	}

	@Bean
	public MemberInfoInterface memberInfoInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(MemberInfoInterface.class, VERSION);
	}

	@Bean
	public CWTMemberInfoInterface CWTMemberInfoInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(CWTMemberInfoInterface.class, VERSION);
	}

	@Bean
	public XXTMemberInfoInterface XXTMemberInfoInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(XXTMemberInfoInterface.class, VERSION);
	}

	@Bean
	public HLWMemberInfoInterface HLWMemberInfoInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(HLWMemberInfoInterface.class, VERSION);
	}

	@Bean
	public IndustryManagerInterface industryManagerInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(IndustryManagerInterface.class, VERSION);
	}

	@Bean
	public SendProvinceSmsInterface sendProvinceSmsInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(SendProvinceSmsInterface.class, VERSION);
	}

	@Bean
	public CustomerInterface customerInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(CustomerInterface.class, VERSION);
	}

	@Bean
	public AccountManagerInterface accountManagerInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(AccountManagerInterface.class, VERSION);
	}

	@Bean
	public MenuManagerInterface menuManagerInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(MenuManagerInterface.class, VERSION);
	}

	@Bean
	public RolePowerManagerInterface rolePowerManagerInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(RolePowerManagerInterface.class, VERSION);
	}

	@Bean
	public QuestionFeedBackInterface questionFeedBackService(DubboUtil dubboUtil) {
		return dubboUtil.getReference(QuestionFeedBackInterface.class, VERSION);
	}

	@Bean
	public SmsSwitchInterface smsSwitchInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(SmsSwitchInterface.class, VERSION);
	}

	@Bean
	public AnnounceContentInterface announceContentInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(AnnounceContentInterface.class, VERSION);
	}

	@Bean
	public AnnounceAnnexInterface announceAnnexInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(AnnounceAnnexInterface.class, VERSION);
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
	public AnnounceHisInterface announceHisInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(AnnounceHisInterface.class, VERSION);
	}

	@Bean
	public SquareInterface squareInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(SquareInterface.class, VERSION);
	}

	@Bean
	public BlackListInterface blackListInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(BlackListInterface.class, VERSION);
	}

	@Bean
	public WorkTeamInterface workTeamInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(WorkTeamInterface.class, VERSION);
	}

	@Bean
	public WorkTeamReplyInterface workTeamReplyInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(WorkTeamReplyInterface.class, VERSION);
	}

	@Bean
	public WorkTeamMessageInterface workTeamMessageInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(WorkTeamMessageInterface.class, VERSION);
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
	public ImGroupInterface imGroupInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(ImGroupInterface.class, VERSION);
	}

	@Bean
	public CorpCustomInterface corpCustomInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(CorpCustomInterface.class, VERSION);
	}

	@Bean
	public GraphicSourceInterface graphicSourceInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(GraphicSourceInterface.class, VERSION);
	}

	@Bean
	public GraphicPushInfoInterface graphicPushInfoInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(GraphicPushInfoInterface.class, VERSION);
	}

	@Bean
	public FestivalInterface festivalInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(FestivalInterface.class, VERSION);
	}

	@Bean
	public SqueareFeedbackInterface SqueareFeedbackInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(SqueareFeedbackInterface.class, VERSION);
	}

	@Bean
	public ExportInterface exportInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(ExportInterface.class, VERSION);
	}

	@Bean
	public UrlManageInterface urlManageInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(UrlManageInterface.class, VERSION);
	}

	@Bean
	public DeptSquareInterface deptSquareInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(DeptSquareInterface.class, VERSION);
	}

	@Bean
	public RoleInterface roleInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(RoleInterface.class, VERSION);
	}

	@Bean
	public MemberInfoInterfaces memberInfoInterfaces(DubboUtil dubboUtil) {
		return dubboUtil.getReference(MemberInfoInterfaces.class, VERSION);
	}

	@Bean
	public DepartMentInterfaces departMentInterfaces(DubboUtil dubboUtil) {
		return dubboUtil.getReference(DepartMentInterfaces.class, VERSION);
	}

	@Bean
	public InsidePruchInterface insidePruchInterface(DubboUtil dubboUtil) {
		return dubboUtil.getReference(InsidePruchInterface.class, VERSION);
	}
}