/**
 * web
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
package com.synopsys.integration.alert.web.api.provider.project;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.PagingParamValidationUtils;

@Component
public class ProviderProjectCustomFunctionAction extends CustomFunctionAction<ProviderProjectOptions> {
    private static final String MISSING_PROVIDER_ERROR = "Provider name is required to retrieve projects.";

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public ProviderProjectCustomFunctionAction(AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility, ProviderDataAccessor providerDataAccessor) {
        super(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, authorizationManager, descriptorMap, fieldValidationUtility);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public ActionResponse<ProviderProjectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        HttpServletRequest httpRequest = servletContentWrapper.getHttpRequest();
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();

        int pageNumber = extractPagingParam(parameterMap, "pageNumber", 0);
        int pageSize = extractPagingParam(parameterMap, "pageSize", 10);
        Optional<ActionResponse<ProviderProjectOptions>> pageRequestError = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumber, pageSize);
        if (pageRequestError.isPresent()) {
            return pageRequestError.get();
        }

        String providerName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        if (StringUtils.isBlank(providerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR);
        }

        String searchTerm = extractFirstParam(parameterMap, "searchTerm").orElse("");
        return fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                   .map(Long::parseLong)
                   .map(configId -> getBlackDuckProjectsActionResponse(configId, pageNumber, pageSize, searchTerm))
                   .orElse(new ActionResponse<>(HttpStatus.OK, new ProviderProjectOptions(0, pageNumber, pageSize, List.of())));
    }

    private ActionResponse<ProviderProjectOptions> getBlackDuckProjectsActionResponse(Long blackDuckGlobalConfigId, int pageNumber, int pageSize, String searchTerm) {
        AlertPagedModel<ProviderProject> providerProjectsPage = providerDataAccessor.getProjectsByProviderConfigId(blackDuckGlobalConfigId, pageNumber, pageSize, searchTerm);
        List<ProviderProjectSelectOption> options = providerProjectsPage.getModels()
                                                        .stream()
                                                        .map(project -> new ProviderProjectSelectOption(project.getName(), project.getDescription()))
                                                        .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, new ProviderProjectOptions(providerProjectsPage.getTotalPages(), providerProjectsPage.getCurrentPage(), providerProjectsPage.getPageSize(), options));
    }

    private int extractPagingParam(Map<String, String[]> parameterMap, String paramName, int defaultValue) {
        return extractFirstParam(parameterMap, paramName)
                   .filter(NumberUtils::isDigits)
                   .map(NumberUtils::toInt)
                   .orElse(defaultValue);
    }

    private Optional<String> extractFirstParam(Map<String, String[]> parameterMap, String paramName) {
        return Optional.ofNullable(parameterMap.get(paramName))
                   .filter(paramValues -> paramValues.length > 0)
                   .map(paramValues -> paramValues[0]);
    }

}
