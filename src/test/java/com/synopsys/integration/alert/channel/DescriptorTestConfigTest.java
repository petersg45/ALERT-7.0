package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.RestApiType;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class DescriptorTestConfigTest<R extends CommonDistributionConfig, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity> {
    private final EmailGroupDistributionRepository emailGroupDistributionRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;
    private final SlackDistributionRepository slackDistributionRepository;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    protected Gson gson;
    protected ContentConverter contentConverter;
    protected ChannelEventFactory channelEventFactory;
    protected TestProperties properties;

    @Autowired
    public DescriptorTestConfigTest(final EmailGroupDistributionRepository emailGroupDistributionRepository, final HipChatDistributionRepository hipChatDistributionRepository,
        final SlackDistributionRepository slackDistributionRepository, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        this.emailGroupDistributionRepository = emailGroupDistributionRepository;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
        this.slackDistributionRepository = slackDistributionRepository;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        channelEventFactory = new ChannelEventFactory(emailGroupDistributionRepository, hipChatDistributionRepository, slackDistributionRepository,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
        cleanGlobalRepository();
    }

    public abstract void cleanGlobalRepository();

    public abstract void saveGlobalConfiguration();

    public abstract ChannelDescriptor getDescriptor();

    public abstract MockEntityUtil<E> getMockEntityUtil();

    @Test
    public void testCreateChannelEvent() {
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(FrequencyType.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent channelEvent = channelEventFactory.createChannelEvent(1L, getDescriptor().getDestinationName(), notificationContent);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }

    @Test
    public void testSendTestMessage() throws IntegrationException {
        saveGlobalConfiguration();
        final RestApi restApi = getDescriptor().getRestApi(RestApiType.CHANNEL_DISTRIBUTION_CONFIG);
        final RestApi spyDescriptorConfig = Mockito.spy(restApi);
        final Config restModel = getMockEntityUtil().createConfig();
        try {
            spyDescriptorConfig.testConfig(restModel);
        } catch (final IntegrationException e) {
            Assert.fail();
        }

        Mockito.verify(spyDescriptorConfig).testConfig(Mockito.any());
    }

}
