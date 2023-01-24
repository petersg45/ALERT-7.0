package com.synopsys.integration.alert.component.diagnostic.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.execution.AggregatedExecutionResults;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionModel;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobStageModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobDurationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobStageDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobStatusDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class DefaultDiagnosticAccessorTest {
    public static final String TEST_JOB_NAME = "Job Name";
    private NotificationContentRepository notificationContentRepository;
    private AuditEntryRepository auditEntryRepository;
    private RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;
    private ExecutingJobManager executingJobManager;
    private StaticJobAccessor staticJobAccessor;
    private JobCompletionStatusAccessor jobCompletionStatusAccessor;
    private JobExecutionAccessor jobExecutionAccessor;

    @BeforeEach
    public void init() {
        notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        rabbitMQDiagnosticUtility = Mockito.mock(RabbitMQDiagnosticUtility.class);
        staticJobAccessor = Mockito.mock(StaticJobAccessor.class);
        jobCompletionStatusAccessor = Mockito.mock(JobCompletionStatusAccessor.class);
        jobExecutionAccessor = Mockito.mock(JobExecutionAccessor.class);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
    }

    @Test
    void testGetDiagnosticInfo() {
        DefaultDiagnosticAccessor diagnosticAccessor = new DefaultDiagnosticAccessor(
            notificationContentRepository,
            auditEntryRepository,
            rabbitMQDiagnosticUtility,
            staticJobAccessor,
            executingJobManager,
            jobCompletionStatusAccessor
        );
        NotificationDiagnosticModel notificationDiagnosticModel = createNotificationDiagnosticModel();
        AuditDiagnosticModel auditDiagnosticModel = createAuditDiagnosticModel();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = createRabbitMQDiagnosticModel();
        JobDiagnosticModel jobDiagnosticModel = createJobDiagnosticModel();
        JobExecutionsDiagnosticModel jobExecutionsDiagnosticModel = createJobExecutionsDiagnosticModel();
        DiagnosticModel diagnosticModel = diagnosticAccessor.getDiagnosticInfo();

        assertEquals(notificationDiagnosticModel, diagnosticModel.getNotificationDiagnosticModel());
        assertEquals(auditDiagnosticModel, diagnosticModel.getAuditDiagnosticModel());
        assertEquals(rabbitMQDiagnosticModel, diagnosticModel.getRabbitMQDiagnosticModel());
        assertEquals(jobDiagnosticModel, diagnosticModel.getJobDiagnosticModel());
        assertEquals(jobExecutionsDiagnosticModel, diagnosticModel.getExecutingJobsDiagnosticModel());
        assertSystemDiagnostics(diagnosticModel.getSystemDiagnosticModel());
    }

    private NotificationDiagnosticModel createNotificationDiagnosticModel() {
        long numberOfNotifications = 10L;
        long numberOfNotificationsProcessed = 5L;
        long numberOfNotificationsUnprocessed = 5L;
        Mockito.when(notificationContentRepository.count()).thenReturn(numberOfNotifications);
        Mockito.when(notificationContentRepository.countByProcessed(true)).thenReturn(numberOfNotificationsProcessed);
        Mockito.when(notificationContentRepository.countByProcessed(false)).thenReturn(numberOfNotificationsUnprocessed);
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed);
    }

    private AuditDiagnosticModel createAuditDiagnosticModel() {
        long numberOfAuditEntriesSuccessful = 10L;
        long numberOfAuditEntriesFailed = 15L;
        long numberOfAuditEntriesPending = 20L;
        String averageAuditProcessingTime = AuditDiagnosticModel.NO_AUDIT_CONTENT_MESSAGE;
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name())).thenReturn(numberOfAuditEntriesSuccessful);
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name())).thenReturn(numberOfAuditEntriesFailed);
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name())).thenReturn(numberOfAuditEntriesPending);
        Mockito.when(auditEntryRepository.getAverageAuditEntryCompletionTime()).thenReturn(Optional.of(averageAuditProcessingTime));
        return new AuditDiagnosticModel(numberOfAuditEntriesSuccessful, numberOfAuditEntriesFailed, numberOfAuditEntriesPending, averageAuditProcessingTime);
    }

    private RabbitMQDiagnosticModel createRabbitMQDiagnosticModel() {
        AlertQueueInformation queue1 = new AlertQueueInformation("queue1", 50, 1);
        AlertQueueInformation queue2 = new AlertQueueInformation("queue2", 0, 50);
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = new RabbitMQDiagnosticModel(List.of(queue1, queue2));
        Mockito.when(rabbitMQDiagnosticUtility.getRabbitMQDiagnostics()).thenReturn(rabbitMQDiagnosticModel);
        return rabbitMQDiagnosticModel;
    }

    private JobDiagnosticModel createJobDiagnosticModel() {
        UUID jobConfigId = UUID.randomUUID();
        Long notificationCount = 10L;
        Long successCount = 1L;
        Long failureCount = 0L;
        String latestStatus = AuditEntryStatus.SUCCESS.name();
        OffsetDateTime lastRun = DateUtils.createCurrentDateTimestamp();
        JobCompletionStatusDurations durations = new JobCompletionStatusDurations(
            Duration.between(lastRun, lastRun.minusSeconds(30)).toMillis(),
            Duration.between(lastRun, lastRun.minusMinutes(20)).toMillis(),
            Duration.between(lastRun, lastRun.minusMinutes(10)).toMillis(),
            Duration.between(lastRun, lastRun.minusMinutes(5)).toMillis(),
            Duration.between(lastRun, lastRun.minusMinutes(4)).toMillis(),
            Duration.between(lastRun, lastRun.minusMinutes(1)).toMillis()
        );
        JobCompletionStatusModel statusModel = new JobCompletionStatusModel(jobConfigId, notificationCount, successCount, failureCount, latestStatus, lastRun, durations);
        AlertPagedModel<JobCompletionStatusModel> pageModel = new AlertPagedModel<>(1, 0, 10, List.of(statusModel));
        Mockito.when(jobCompletionStatusAccessor.getJobExecutionStatus(Mockito.any(AlertPagedQueryDetails.class))).thenReturn(pageModel);

        DistributionJobModelBuilder jobModelBuilder = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .name(TEST_JOB_NAME)
            .processingType(ProcessingType.DEFAULT)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .blackDuckGlobalConfigId(1L)
            .createdAt(OffsetDateTime.now())
            .channelDescriptorName(ChannelKeys.SLACK.getUniversalKey())
            .notificationTypes(List.of("VULNERABILITY"));

        Mockito.when(staticJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModelBuilder.build()));
        JobDurationDiagnosticModel durationDiagnosticModel = new JobDurationDiagnosticModel(
            DateUtils.formatDurationFromNanos(durations.getJobDurationMillisec()),
            durations.getNotificationProcessingDuration().map(DateUtils::formatDurationFromNanos).orElse(null),
            durations.getChannelProcessingDuration().map(DateUtils::formatDurationFromNanos).orElse(null),
            durations.getIssueCreationDuration().map(DateUtils::formatDurationFromNanos).orElse(null),
            durations.getIssueCommentingDuration().map(DateUtils::formatDurationFromNanos).orElse(null),
            durations.getIssueTransitionDuration().map(DateUtils::formatDurationFromNanos).orElse(null)
        );
        JobStatusDiagnosticModel statusDiagnosticModel = new JobStatusDiagnosticModel(
            jobConfigId,
            TEST_JOB_NAME,
            notificationCount,
            successCount,
            failureCount,
            latestStatus,
            DateUtils.formatDateAsJsonString(lastRun),
            durationDiagnosticModel
        );
        return new JobDiagnosticModel(List.of(statusDiagnosticModel));
    }

    private JobExecutionsDiagnosticModel createJobExecutionsDiagnosticModel() {
        long totalJobsInSystem = 1L;
        long pendingCount = 1L;
        long successCount = 0L;
        long failureCount = 0L;
        int processedNotificationCount = 10;
        int totalNotificationCount = 100;

        AggregatedExecutionResults results = new AggregatedExecutionResults(totalJobsInSystem, pendingCount, successCount, failureCount);

        String channelName = ChannelKeys.SLACK.getUniversalKey();
        OffsetDateTime startTime = DateUtils.createCurrentDateTimestamp();
        String start = DateUtils.formatDateAsJsonString(startTime);
        String end = "";

        OffsetDateTime firstStageStart = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime firstStageEnd = DateUtils.createCurrentDateTimestamp();
        JobStageDiagnosticModel firstStage = new JobStageDiagnosticModel(
            JobStage.NOTIFICATION_PROCESSING,
            DateUtils.formatDateAsJsonString(firstStageStart),
            DateUtils.formatDateAsJsonString(firstStageEnd)
        );

        OffsetDateTime secondStageStart = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime secondStageEnd = DateUtils.createCurrentDateTimestamp();
        JobStageDiagnosticModel secondStage = new JobStageDiagnosticModel(
            JobStage.CHANNEL_PROCESSING,
            DateUtils.formatDateAsJsonString(secondStageStart),
            DateUtils.formatDateAsJsonString(secondStageEnd)
        );
        List<JobStageDiagnosticModel> stages = List.of(firstStage, secondStage);

        JobExecutionDiagnosticModel model = new JobExecutionDiagnosticModel(
            TEST_JOB_NAME,
            channelName,
            start,
            end,
            AuditEntryStatus.PENDING,
            processedNotificationCount,
            totalNotificationCount,
            stages
        );
        List<JobExecutionDiagnosticModel> jobExecutionDiagnosticModels = List.of(model);
        DistributionJobModelBuilder jobModelBuilder = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .name(TEST_JOB_NAME)
            .processingType(ProcessingType.DEFAULT)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .blackDuckGlobalConfigId(1L)
            .createdAt(OffsetDateTime.now())
            .channelDescriptorName(ChannelKeys.SLACK.getUniversalKey())
            .notificationTypes(List.of("VULNERABILITY"));

        DistributionJobModel distributionJobModel = jobModelBuilder.build();
        Mockito.when(staticJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        JobExecutionModel jobExecutionModel = new JobExecutionModel(
            UUID.randomUUID(),
            distributionJobModel.getJobId(),
            startTime,
            null,
            AuditEntryStatus.PENDING,
            processedNotificationCount,
            totalNotificationCount
        );
        JobStageModel firstJobStageModel = new JobStageModel(jobExecutionModel.getExecutionId(), firstStage.getStage().getStageId(), firstStageStart, firstStageEnd);
        JobStageModel secondJobStageModel = new JobStageModel(
            jobExecutionModel.getExecutionId(),
            secondStage.getStage().getStageId(),
            secondStageStart,
            secondStageEnd
        );
        Mockito.when(executingJobManager.aggregateExecutingJobData()).thenReturn(results);
        List<JobStageModel> jobStages = List.of(firstJobStageModel, secondJobStageModel);
        Mockito.when(executingJobManager.getStages(Mockito.any(UUID.class))).thenReturn(jobStages);
        AlertPagedModel<JobExecutionModel> executingJobs = new AlertPagedModel<>(1, 0, 1, List.of(jobExecutionModel));
        Mockito.when(executingJobManager.getExecutingJobs(Mockito.anyInt(), Mockito.anyInt())).thenReturn(executingJobs);
        return new JobExecutionsDiagnosticModel(totalJobsInSystem, pendingCount, successCount, failureCount, jobExecutionDiagnosticModels);
    }

    private void assertSystemDiagnostics(SystemDiagnosticModel systemDiagnosticModel) {
        // System diagnostics entirely depend on the system running them
        assertTrue(systemDiagnosticModel.getAvailableProcessors() > 0);
        assertTrue(systemDiagnosticModel.getMaxMemory() > 0);
        assertTrue(systemDiagnosticModel.getTotalMemory() > 0);
        assertTrue(systemDiagnosticModel.getFreeMemory() > 0);
        assertTrue(systemDiagnosticModel.getUsedMemory() > 0);
    }

    private String calculateFormattedDuration(Long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        return String.format("%sH:%sm:%ss.%s", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
    }
}
