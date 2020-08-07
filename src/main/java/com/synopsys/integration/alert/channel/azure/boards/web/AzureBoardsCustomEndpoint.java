/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.azure.boards.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.ButtonCustomEndpoint;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class AzureBoardsCustomEndpoint extends ButtonCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomEndpoint.class);

    private final AzureBoardsChannelKey azureBoardsChannelKey;
    private final ResponseFactory responseFactory;

    @Autowired
    public AzureBoardsCustomEndpoint(AzureBoardsChannelKey azureBoardsChannelKey, CustomEndpointManager customEndpointManager, ResponseFactory responseFactory) throws Exception {
        super(AzureBoardsDescriptor.KEY_OAUTH, customEndpointManager);
        this.azureBoardsChannelKey = azureBoardsChannelKey;
        this.responseFactory = responseFactory;
    }

    @Override
    public ResponseEntity<String> createResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        return responseFactory.createOkResponse("", "Placeholder Message"); //FIXME this message should fixed once the Oauth is implemented.
    }

}
