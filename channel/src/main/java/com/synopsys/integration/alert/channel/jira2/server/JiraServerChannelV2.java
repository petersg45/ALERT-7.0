/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.IssueTrackerChannel;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerChannelV2 extends IssueTrackerChannel<JiraServerJobDetailsModel, String> {
    @Autowired
    protected JiraServerChannelV2(JiraServerProcessorFactory processorFactory, IssueTrackerResponsePostProcessor responsePostProcessor) {
        super(processorFactory, responsePostProcessor);
    }
}