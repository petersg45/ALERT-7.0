package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

class AzureBoardsTransitionEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID parentEventId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueTransitionModel<Integer> model = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        AzureBoardsTransitionEvent event = new AzureBoardsTransitionEvent(destination, parentEventId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(parentEventId, event.getParentEventId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobExecutionId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertEquals(model, event.getTransitionModel());
    }
}
