/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class NotificationToChannelEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToChannelEventConverter.class);
    private final DescriptorMap descriptorMap;

    @Autowired
    // TODO investigate, DescriptorMap is lazy because of a circular injection
    public NotificationToChannelEventConverter(@Lazy final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    public List<ChannelEvent> convertToEvents(final Map<? extends CommonDistributionConfig, List<AggregateMessageContent>> messageContentMap) {
        final List<ChannelEvent> channelEvents = new ArrayList<>();
        final Set<? extends Map.Entry<? extends CommonDistributionConfig, List<AggregateMessageContent>>> jobMessageContentEntries = messageContentMap.entrySet();
        for (final Map.Entry<? extends CommonDistributionConfig, List<AggregateMessageContent>> entry : jobMessageContentEntries) {
            final CommonDistributionConfig jobConfig = entry.getKey();
            final List<AggregateMessageContent> contentList = entry.getValue();
            for (final AggregateMessageContent content : contentList) {
                channelEvents.add(createChannelEvent(jobConfig, content));
            }
        }
        logger.debug("Created {} events.", channelEvents.size());
        return channelEvents;
    }

    private ChannelEvent createChannelEvent(final CommonDistributionConfig config, final AggregateMessageContent messageContent) {
        return descriptorMap.getChannelDescriptor(config.getDistributionType()).getChannelEventProducer().createChannelEvent(config, messageContent);
    }
}
