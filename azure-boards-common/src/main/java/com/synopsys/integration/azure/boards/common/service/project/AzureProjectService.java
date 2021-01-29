/*
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.service.project;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/core/projects?view=azure-devops-rest-5.1">Projects</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/core/projects/get%20project%20properties?view=azure-devops-rest-5.1">Project Properties</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/fields/create?view=azure-devops-rest-5.1">Project Fields</a>
 */
public class AzureProjectService {
    private final AzureHttpService azureHttpService;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureProjectService(AzureHttpService azureHttpService, AzureApiVersionAppender azureApiVersionAppender) {
        this.azureHttpService = azureHttpService;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public AzureArrayResponseModel<TeamProjectReferenceResponseModel> getProjects(String organizationName) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/projects", organizationName);
        Type responseType = new TypeToken<AzureArrayResponseModel<TeamProjectReferenceResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public TeamProjectResponseModel getProject(String organizationName, String projectId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/projects/%s", organizationName, projectId);
        Type responseType = new TypeToken<TeamProjectResponseModel>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<ProjectPropertyResponseModel> getProjectProperties(String organizationName, String projectId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/projects/%s/properties", organizationName, projectId);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_1(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<ProjectPropertyResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public ProjectWorkItemFieldModel createProjectField(String organizationName, String projectNameOrId, ProjectWorkItemFieldModel requestModel) throws IOException, HttpServiceException {
        String requestSpec = createOrganizationProjectFieldsSpec(organizationName, projectNameOrId);
        return azureHttpService.post(requestSpec, requestModel, ProjectWorkItemFieldModel.class);
    }

    public ProjectWorkItemFieldModel getField(String organizationName, String fieldNameOrRef) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/wit/fields/%s", organizationName, fieldNameOrRef);
        Type responseType = new TypeToken<ProjectWorkItemFieldModel>() {}.getType();

        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<ProjectWorkItemFieldModel> getProjectFields(String organizationName, String projectNameOrId) throws HttpServiceException {
        String requestSpec = createOrganizationProjectFieldsSpec(organizationName, projectNameOrId);
        Type responseType = new TypeToken<AzureArrayResponseModel<ProjectWorkItemFieldModel>>() {}.getType();

        return azureHttpService.get(requestSpec, responseType);
    }

    private String createOrganizationProjectFieldsSpec(String organizationName, String projectNameOrId) {
        return String.format("/%s/%s/_apis/wit/fields", organizationName, projectNameOrId);
    }

}
