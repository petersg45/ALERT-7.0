package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

class JiraServerTransitionEventHandlerTest {

    private Gson gson = new Gson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
    }

    @Test
    void handleUnknownJobTest() {
        UUID jobId = UUID.randomUUID();

        JiraServerPropertiesFactory propertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory messageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_SERVER,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager
        );
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor = jobId1 -> Optional.empty();

        JiraServerTransitionEventHandler handler = new JiraServerTransitionEventHandler(gson, propertiesFactory, messageSenderFactory, jobDetailsAccessor);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(existingIssueDetails, IssueOperation.RESOLVE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(IssueTrackerTransitionIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER), jobId, model);
        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    @Test
    void handleTransitionTest() throws IntegrationException {
        UUID jobId = UUID.randomUUID();

        JiraServerPropertiesFactory propertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory jiraServiceFactory = Mockito.mock(JiraServerServiceFactory.class);
        IssueService issueService = Mockito.mock(IssueService.class);
        IssueSearchService issueSearchService = Mockito.mock(IssueSearchService.class);
        FieldService fieldService = Mockito.mock(FieldService.class);
        ProjectService projectService = Mockito.mock(ProjectService.class);

        Mockito.doAnswer(invocation -> {
            issueCounter.incrementAndGet();
            return null;
        }).when(issueService).transitionIssue(Mockito.any(IssueRequestModel.class));
        Mockito.when(issueService.getStatus(Mockito.anyString())).thenReturn(createStatusDetailsComponent());
        Mockito.when(issueService.getTransitions(Mockito.anyString())).thenReturn(new TransitionsResponseModel("", List.of(createTransition())));

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(jiraServiceFactory);
        Mockito.when(propertiesFactory.createJiraPropertiesWithJobId(jobId)).thenReturn(jiraProperties);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory messageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_SERVER,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager
        );
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor = jobId1 -> Optional.of(createJobDetails(jobId));

        JiraServerTransitionEventHandler handler = new JiraServerTransitionEventHandler(gson, propertiesFactory, messageSenderFactory, jobDetailsAccessor);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(existingIssueDetails, IssueOperation.RESOLVE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(IssueTrackerTransitionIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER), jobId, model);
        handler.handle(event);
        assertEquals(1, issueCounter.get());
    }

    private JiraServerJobDetailsModel createJobDetails(UUID jobId) {
        return new JiraServerJobDetailsModel(
            jobId,
            true,
            "user",
            "jiraProject",
            "Task",
            "Resolve",
            "Reopen",
            List.of(),
            ""
        );
    }

    private StatusDetailsComponent createStatusDetailsComponent() {
        StatusCategory statusCategory = new StatusCategory();

        return new StatusDetailsComponent("self", "description", "iconUrl", "name", "id", statusCategory);
    }

    private TransitionComponent createTransition() {
        return new TransitionComponent("id", "Resolve", null, false, false, false, Map.of(), null);
    }
}