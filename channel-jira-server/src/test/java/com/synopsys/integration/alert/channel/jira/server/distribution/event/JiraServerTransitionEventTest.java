package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

class JiraServerTransitionEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobId = UUID.randomUUID();
        IssueTransitionModel<String> model = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(destination, jobId, model);

        assertNotNull(event.getEventId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(model, event.getTransitionModel());
    }
}