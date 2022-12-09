package com.synopsys.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

class DistributionEventHandlerTest {
    private final ChannelKey channelKey = new ChannelKey("test universal key", "Test Universal Key");

    @Test
    void handleEventSuccessTest() {
        AtomicInteger count = new AtomicInteger(0);
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntrySuccess(Mockito.any(), Mockito.anySet());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannel<DistributionJobDetailsModel> channel = (v, w, x, y, z) -> {
            count.incrementAndGet();
            return null;
        };

        ExecutingJobManager executingJobManager = new ExecutingJobManager();

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor, executingJobManager);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(1, count.get());
    }

    @Test
    void handleEventExceptionTest() {
        AtomicInteger count = new AtomicInteger(0);
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.any(), Mockito.anySet(), Mockito.anyString(), Mockito.any(Throwable.class));

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        AlertException testException = new AlertException("Test exception");
        DistributionChannel<DistributionJobDetailsModel> channel = (v, w, x, y, z) -> {
            count.incrementAndGet();
            throw testException;
        };

        ExecutingJobManager executingJobManager = new ExecutingJobManager();
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor, executingJobManager);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(1, count.get());
    }

    @Test
    void handleEventJobDetailsMissingTest() {
        AtomicInteger count = new AtomicInteger(0);
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.any(), Mockito.anySet(), Mockito.anyString(), Mockito.any(Throwable.class));

        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        DistributionChannel<DistributionJobDetailsModel> channel = (v, w, x, y, z) -> {
            count.incrementAndGet();
            return null;
        };
        ExecutingJobManager executingJobManager = new ExecutingJobManager();

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor, executingJobManager);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(0, count.get());
    }

}
