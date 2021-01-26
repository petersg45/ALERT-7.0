/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredDistributionJobResponseModel extends AlertSerializableModel {
    private UUID id;
    private ProcessingType processingType;
    private String channelName;

    public FilteredDistributionJobResponseModel(UUID id, ProcessingType processingType, String channelName) {
        this.processingType = processingType;
        this.id = id;
        this.channelName = channelName;
    }

    public UUID getId() {
        return id;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelName() {
        return channelName;
    }
}