package com.synopsys.integration.alert.database.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class JobSubTaskStatusEntityTest {
    @Test
    void defaultConstructorTest() {
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity();
        assertNull(entity.getId());
        assertNull(entity.getJobId());
        assertNull(entity.getJobExecutionId());
        assertNull(entity.getNotificationCorrelationId());
        assertNull(entity.getRemainingEvents());
    }

    @Test
    void constructorTest() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Long remainingTaskCount = 5L;
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity(id, jobId, jobExecutionId, remainingTaskCount, correlationId);
        assertEquals(id, entity.getId());
        assertEquals(jobId, entity.getJobId());
        assertEquals(correlationId, entity.getNotificationCorrelationId());
        assertEquals(remainingTaskCount, entity.getRemainingEvents());
    }
}
