/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@JsonAdapter(DistributionJobDetailsModelJsonAdapter.class)
public abstract class DistributionJobDetailsModel extends AlertSerializableModel {
    public static final Class<AzureBoardsJobDetailsModel> AZURE = AzureBoardsJobDetailsModel.class;
    public static final Class<EmailJobDetailsModel> EMAIL = EmailJobDetailsModel.class;
    public static final Class<JiraCloudJobDetailsModel> JIRA_CLOUD = JiraCloudJobDetailsModel.class;
    public static final Class<JiraServerJobDetailsModel> JIRA_SERVER = JiraServerJobDetailsModel.class;
    public static final Class<MSTeamsJobDetailsModel> MS_TEAMS = MSTeamsJobDetailsModel.class;
    public static final Class<SlackJobDetailsModel> SLACK = SlackJobDetailsModel.class;

    private static final Map<ChannelKey, Class<? extends DistributionJobDetailsModel>> detailsModels = new HashMap<>();

    static {
        detailsModels.put(ChannelKey.AZURE_BOARDS, AZURE);
        detailsModels.put(ChannelKey.EMAIL, EMAIL);
        detailsModels.put(ChannelKey.JIRA_CLOUD, JIRA_CLOUD);
        detailsModels.put(ChannelKey.JIRA_SERVER, JIRA_SERVER);
        detailsModels.put(ChannelKey.MS_TEAMS, MS_TEAMS);
        detailsModels.put(ChannelKey.SLACK, SLACK);
    }

    public static Class<? extends DistributionJobDetailsModel> getConcreteClass(ChannelKey channelKey) {
        return detailsModels.get(channelKey);
    }

    private final ChannelKey channelKey;

    public DistributionJobDetailsModel(ChannelKey channelKey) {
        this.channelKey = channelKey;
    }

    public boolean isA(ChannelKey channelKey) {
        return this.channelKey.equals(channelKey);
    }

    public <T extends DistributionJobDetailsModel> T getAs(Class<T> clazz) {
        return clazz.cast(this);
    }

}