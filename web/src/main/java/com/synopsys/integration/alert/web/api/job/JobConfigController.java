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
package com.synopsys.integration.alert.web.api.job;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseJobResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.TestController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.JobIdsValidationRequestModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@RestController
@RequestMapping(JobConfigController.JOB_CONFIGURATION_PATH)
public class JobConfigController implements BaseJobResourceController, ReadPageController<JobPagedModel>, TestController<JobFieldModel>, ValidateController<JobFieldModel> {
    public static final String JOB_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/job";
    private final JobConfigActions jobConfigActions;

    @Autowired
    public JobConfigController(JobConfigActions jobConfigActions) {
        this.jobConfigActions = jobConfigActions;
    }

    @PostMapping("/validateJobsById")
    public List<JobFieldStatuses> getValidationResultsForJobs(@RequestBody JobIdsValidationRequestModel validationModel) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.validateJobsById(validationModel));
    }

    // This will check if the specified descriptor has a global config associated with it
    @PostMapping("/descriptorCheck")
    public String descriptorCheck(@RequestBody String descriptorName) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.checkGlobalConfigExists(descriptorName));
    }

    @Override
    public JobPagedModel getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.getPage(pageNumber, pageSize, searchTerm));
    }

    @Override
    public JobFieldModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.getOne(id));
    }

    @Override
    public JobFieldModel create(JobFieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.create(resource));
    }

    @Override
    public void update(UUID id, JobFieldModel resource) {
        ResponseFactory.createContentResponseFromAction(jobConfigActions.update(id, resource));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(jobConfigActions.delete(id));
    }

    @Override
    public ValidationResponseModel validate(JobFieldModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.validate(requestBody));
    }

    @Override
    public ValidationResponseModel test(JobFieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(jobConfigActions.test(resource));
    }

}