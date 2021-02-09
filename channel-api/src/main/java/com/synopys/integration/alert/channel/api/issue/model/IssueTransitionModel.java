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
package com.synopys.integration.alert.channel.api.issue.model;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTransitionModel<T extends Serializable> extends AlertSerializableModel {
    private final T issueId;
    private final IssueTransitionType transitionType;
    private final List<String> postTransitionComments;

    private final ProjectIssueModel source;

    public IssueTransitionModel(T issueId, IssueTransitionType transitionType, List<String> postTransitionComments, ProjectIssueModel source) {
        this.issueId = issueId;
        this.transitionType = transitionType;
        this.postTransitionComments = postTransitionComments;
        this.source = source;
    }

    public T getIssueId() {
        return issueId;
    }

    public IssueTransitionType getTransitionType() {
        return transitionType;
    }

    public List<String> getPostTransitionComments() {
        return postTransitionComments;
    }

    public ProjectIssueModel getSource() {
        return source;
    }

}
