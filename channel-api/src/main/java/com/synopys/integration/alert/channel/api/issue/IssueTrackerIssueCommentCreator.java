/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;

public abstract class IssueTrackerIssueCommentCreator<T extends Serializable> {
    public static final String COMMENTING_DISABLED_MESSAGE = "Commenting on issues is disabled. Skipping.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AlertIssueOriginCreator alertIssueOriginCreator;

    protected IssueTrackerIssueCommentCreator(AlertIssueOriginCreator alertIssueOriginCreator) {
        this.alertIssueOriginCreator = alertIssueOriginCreator;
    }

    public final Optional<IssueTrackerIssueResponseModel> commentOnIssue(IssueCommentModel<T> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return Optional.empty();
        }

        addComments(issueCommentModel);

        AlertIssueOrigin alertIssueOrigin = alertIssueOriginCreator.createIssueOrigin(issueCommentModel.getSource());
        ExistingIssueDetails<T> existingIssueDetails = issueCommentModel.getExistingIssueDetails();

        IssueTrackerIssueResponseModel responseModel = new IssueTrackerIssueResponseModel(
            alertIssueOrigin,
            existingIssueDetails.getIssueKey(),
            existingIssueDetails.getIssueLink(),
            existingIssueDetails.getIssueSummary(),
            IssueOperation.UPDATE
        );
        return Optional.of(responseModel);
    }

    protected abstract boolean isCommentingEnabled();

    protected abstract void addComments(IssueCommentModel<T> issueCommentModel) throws AlertException;

}
