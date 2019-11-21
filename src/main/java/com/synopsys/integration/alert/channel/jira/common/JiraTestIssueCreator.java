/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.issuetracker.IssueOperation;
import com.synopsys.integration.alert.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueProperties;
import com.synopsys.integration.alert.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.service.TestIssueCreator;

//TODO TestIssueRequestCreator
public class JiraTestIssueCreator implements TestIssueCreator {
    private static final Logger logger = LoggerFactory.getLogger(JiraTestIssueCreator.class);
    private final FieldAccessor fieldAccessor;
    private final JiraMessageParser jiraMessageParser;

    public JiraTestIssueCreator(FieldAccessor fieldAccessor, JiraMessageParser jiraMessageParser) {
        this.fieldAccessor = fieldAccessor;
        this.jiraMessageParser = jiraMessageParser;
    }

    @Override
    public IssueTrackerRequest createRequest(IssueOperation operation, String messageId) {
        try {
            String topic = fieldAccessor.getString(TestAction.KEY_CUSTOM_TOPIC).orElse("Alert Test Message");
            String customMessage = fieldAccessor.getString(TestAction.KEY_CUSTOM_MESSAGE).orElse("Test Message Content");
            ProviderMessageContent providerMessageContent = createTestNotificationContent(ItemOperation.ADD, messageId, topic, customMessage);
            ComponentItem arbitraryItem = providerMessageContent.getComponentItems().stream()
                                              .findAny()
                                              .orElseThrow(() -> new AlertException("No actionable component items were found. Cannot create test message content."));

            String providerName = providerMessageContent.getProvider().getValue();
            LinkableItem topicItem = providerMessageContent.getTopic();
            LinkableItem subTopicItem = providerMessageContent.getSubTopic().orElse(null);
            Set<ComponentItem> componentItems = providerMessageContent.getComponentItems();

            IssueProperties issueProperties = JiraIssuePropertiesUtil.create(providerName, topicItem, subTopicItem, arbitraryItem, StringUtils.EMPTY);

            switch (operation) {
                case RESOLVE: {
                    IssueContentModel contentModel = jiraMessageParser.createIssueContentModel(providerName, IssueResolutionRequest.OPERATION, topicItem, subTopicItem, componentItems, arbitraryItem);
                    return IssueResolutionRequest.of(issueProperties, contentModel);
                }
                case OPEN:
                case UPDATE:
                default: {
                    IssueContentModel contentModel = jiraMessageParser.createIssueContentModel(providerName, IssueCreationRequest.OPERATION, topicItem, subTopicItem, componentItems, arbitraryItem);
                    return IssueCreationRequest.of(issueProperties, contentModel);
                }
            }

        } catch (AlertException ex) {
            logger.error("Error create test issue content", ex);
        }

        return null;
    }

    private ProviderMessageContent createTestNotificationContent(ItemOperation operation, String messageId, String customTopic, String customMessage) throws AlertException {
        return new ProviderMessageContent.Builder()
                   .applyProvider("Alert")
                   .applyTopic("Test Topic", customTopic)
                   .applySubTopic("Test SubTopic", "Test message sent by Alert")
                   .applyComponentItem(createTestComponentItem(operation, messageId, customMessage))
                   .build();
    }

    private ComponentItem createTestComponentItem(ItemOperation operation, String messageId, String customMessage) throws AlertException {
        return new ComponentItem.Builder()
                   .applyOperation(operation)
                   .applyCategory("Test Category")
                   .applyComponentData("Message ID", messageId)
                   .applyCategoryItem("Details", customMessage)
                   .applyNotificationId(1L)
                   .build();
    }
}
