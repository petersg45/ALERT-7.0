/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.service.query;

import java.io.IOException;

import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/wiql?view=azure-devops-rest-5.1">Documentation</a>
 */
public class AzureWorkItemQueryService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_TEAM_WIQL = new AzureSpecTemplate("/{organization}/{project}/{team}/_apis/wit/wiql");
    private final AzureHttpService azureHttpService;

    public AzureWorkItemQueryService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public WorkItemQueryResultResponseModel queryForWorkItems(String organizationName, String projectIdOrName, String teamIdOrName, WorkItemQuery query) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_TEAM_WIQL
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .defineReplacement("{team}", teamIdOrName)
                                 .populateSpec();
        requestSpec = String.format("%s?%s=%s", requestSpec, AzureHttpService.AZURE_API_VERSION_QUERY_PARAM_NAME, "5.0");
        WorkItemQueryRequestModel requestModel = new WorkItemQueryRequestModel(query.rawQuery());
        try {
            return azureHttpService.post(requestSpec, requestModel, WorkItemQueryResultResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

}