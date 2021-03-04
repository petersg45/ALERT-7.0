/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueTrackerChannel extends DistributionChannel implements ProviderCallbackEventProducer {
    private final IssueTrackerChannelKey channelKey;
    private final EventManager eventManager;

    public IssueTrackerChannel(IssueTrackerChannelKey channelKey, Gson gson, AuditAccessor auditAccessor, EventManager eventManager) {
        super(gson, auditAccessor);
        this.channelKey = channelKey;
        this.eventManager = eventManager;
    }

    @Override
    public final MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        IssueTrackerContext context = getIssueTrackerContext(event);
        List<IssueTrackerRequest> requests = createRequests(context, event);
        String statusMessage;
        if (requests.isEmpty()) {
            statusMessage = String.format("No requests to send to issue tracker: %s", channelKey.getDisplayName());
        } else {
            IssueTrackerResponse result = sendRequests(context, requests);
            statusMessage = result.getStatusMessage();

            List<ProviderCallbackEvent> callbackEvents = createCallbackEvents(result);
            sendProviderCallbackEvents(callbackEvents);
        }
        return new MessageResult(statusMessage);
    }

    @Override
    public final String getDestinationName() {
        // Required to avoid conflicts when registering new JMS Listeners
        return channelKey.getUniversalKey() + "_old";
    }

    @Override
    public final void sendProviderCallbackEvents(List<ProviderCallbackEvent> callbackEvents) {
        eventManager.sendEvents(callbackEvents);
    }

    /**
     * This method will send requests to an Issue Tracker to create, update, or resolve issues.
     * @param context  The object containing the configuration of the issue tracker server and the configuration of how to map and manage issues.
     * @param requests The list of requests to submit to the issue tracker.  Must be a list because the order requests are added matter.
     * @return A response object containing the aggregate status of sending the requests passed.
     * @throws IntegrationException
     */
    public abstract IssueTrackerResponse sendRequests(IssueTrackerContext context, List<IssueTrackerRequest> requests) throws IntegrationException;

    protected abstract IssueTrackerContext getIssueTrackerContext(DistributionEvent event) throws AlertConfigurationException;

    protected abstract List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException;

    private List<ProviderCallbackEvent> createCallbackEvents(IssueTrackerResponse issueTrackerResponse) {
        List<ProviderCallbackEvent> callbackEvents = new ArrayList<>();
        for (IssueTrackerIssueResponseModel issueResponseModel : issueTrackerResponse.getUpdatedIssues()) {
            AlertIssueOrigin alertIssueOrigin = issueResponseModel.getAlertIssueOrigin();
            ContentKey providerContentKey = alertIssueOrigin.getProviderContentKey();

            Optional<ComponentItem> optionalComponentItem = alertIssueOrigin.getComponentItem();
            if (optionalComponentItem.isPresent()) {
                ComponentItem componentItem = optionalComponentItem.get();
                Optional<ComponentItemCallbackInfo> optionalCallbackInfo = componentItem.getCallbackInfo();
                if (optionalCallbackInfo.isPresent()) {
                    ComponentItemCallbackInfo callbackInfo = optionalCallbackInfo.get();
                    ProviderCallbackEvent issueCallback = new ProviderCallbackEvent(
                        callbackInfo.getProviderKey(),
                        callbackInfo.getCallbackUrl(),
                        issueResponseModel.getIssueKey(),
                        issueResponseModel.getIssueLink(),
                        issueResponseModel.getIssueOperation(),
                        issueResponseModel.getIssueTitle(),
                        providerContentKey.getProviderConfigId(),
                        providerContentKey.getTopicName(),
                        providerContentKey.getSubTopicName()
                    );
                    callbackEvents.add(issueCallback);
                }
            }
        }
        return callbackEvents;
    }

}
