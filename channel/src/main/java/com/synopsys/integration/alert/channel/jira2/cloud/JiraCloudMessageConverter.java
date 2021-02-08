/*
 * channel
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
package com.synopsys.integration.alert.channel.jira2.cloud;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopys.integration.alert.channel.api.convert.ChannelMessageConverter;
import com.synopys.integration.alert.channel.api.issue.model.IssueTrackerMessageHolder;

@Component
public class JiraCloudMessageConverter implements ChannelMessageConverter<JiraCloudJobDetailsModel, IssueTrackerMessageHolder<String>> {
    @Override
    public List<IssueTrackerMessageHolder<String>> convertToChannelMessages(JiraCloudJobDetailsModel distributionDetails, ProviderMessageHolder messages) {
        // FIXME implement
        return List.of();
    }

}
