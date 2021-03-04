/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;

@Component
public class ProviderCallbackIssueTrackerResponsePostProcessor implements IssueTrackerResponsePostProcessor {
    private final EventManager eventManager;

    @Autowired
    public ProviderCallbackIssueTrackerResponsePostProcessor(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void postProcess(IssueTrackerResponse response) {
        List<ProviderCallbackEvent> callbackEvents = createCallbackEvents(response);
        eventManager.sendEvents(callbackEvents);
    }

    private List<ProviderCallbackEvent> createCallbackEvents(IssueTrackerResponse issueTrackerResponse) {
        return issueTrackerResponse.getUpdatedIssues()
                   .stream()
                   .map(this::createProviderCallbackEvent)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private Optional<ProviderCallbackEvent> createProviderCallbackEvent(IssueTrackerIssueResponseModel issueResponseModel) {
        AlertIssueOrigin alertIssueOrigin = issueResponseModel.getAlertIssueOrigin();
        ContentKey providerContentKey = alertIssueOrigin.getProviderContentKey();

        Optional<ComponentItem> optionalComponentItem = alertIssueOrigin.getComponentItem();
        if (optionalComponentItem.isPresent()) {
            ComponentItem componentItem = optionalComponentItem.get();
            return componentItem.getCallbackInfo()
                       .map(callbackInfo ->
                                new ProviderCallbackEvent(
                                    callbackInfo.getProviderKey(),
                                    callbackInfo.getCallbackUrl(),
                                    issueResponseModel.getIssueKey(),
                                    issueResponseModel.getIssueLink(),
                                    issueResponseModel.getIssueOperation(),
                                    issueResponseModel.getIssueTitle(),
                                    providerContentKey.getProviderConfigId(),
                                    providerContentKey.getTopicName(),
                                    providerContentKey.getSubTopicName()
                                )
                       );
        }
        return Optional.empty();
    }

}