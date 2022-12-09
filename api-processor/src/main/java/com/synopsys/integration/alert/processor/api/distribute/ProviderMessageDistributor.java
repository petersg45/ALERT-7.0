/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;

@Component
public class ProviderMessageDistributor {
    private static final String EVENT_CLASS_NAME = DistributionEvent.class.getSimpleName();
    private static final String DESTINATION_WRAPPER_CLASS_NAME = ChannelKey.class.getSimpleName();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());
    private final EventManager eventManager;

    @Autowired
    public ProviderMessageDistributor(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void distribute(ProcessedNotificationDetails processedNotificationDetails, ProcessedProviderMessageHolder processedMessageHolder) {
        String channelName = processedNotificationDetails.getChannelName();
        ChannelKey destinationKey = ChannelKeys.getChannelKey(channelName);
        if (null == destinationKey) {
            logger.warn("Unable to send {}. No {} with the name {} exists", EVENT_CLASS_NAME, DESTINATION_WRAPPER_CLASS_NAME, channelName);
            return;
        }

        for (ProcessedProviderMessageHolder singleMessageHolder : processedMessageHolder.expand()) {
            distributeIndividually(processedNotificationDetails.getJobId(), processedNotificationDetails.getJobName(), destinationKey, singleMessageHolder);
        }
    }

    public void distributeIndividually(UUID jobExecutionId, String jobName, ChannelKey destinationKey, ProcessedProviderMessageHolder processedMessageHolder) {
        Set<Long> notificationIds = processedMessageHolder.extractAllNotificationIds();

        DistributionEvent event = new DistributionEvent(destinationKey, jobExecutionId, jobName, notificationIds, processedMessageHolder.toProviderMessageHolder());
        logger.info("Sending {}. Event ID: {}. Job Execution ID: {}. Destination: {}", EVENT_CLASS_NAME, event.getEventId(), jobExecutionId, destinationKey);
        if (logger.isDebugEnabled()) {
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug("Creating event: {}. Job Execution ID: {}. For notifications: {}", event.getEventId(), jobExecutionId, joinedIds);
        }
        eventManager.sendEvent(event);
    }

}
