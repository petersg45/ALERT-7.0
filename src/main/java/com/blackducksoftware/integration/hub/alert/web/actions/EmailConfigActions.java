/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@Component
public class EmailConfigActions extends ConfigActions<EmailConfigEntity, EmailConfigRestModel> {
    private final EmailChannel emailChannel;

    @Autowired
    public EmailConfigActions(final EmailRepository emailRepository, final ObjectTransformer objectTransformer, final EmailChannel emailChannel) {
        super(EmailConfigEntity.class, EmailConfigRestModel.class, emailRepository, objectTransformer);
        this.emailChannel = emailChannel;
    }

    @Override
    public String validateConfig(final EmailConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getMailSmtpPort()) && !StringUtils.isNumeric(restModel.getMailSmtpPort())) {
            fieldErrors.put("mailSmtpPort", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(restModel.getMailSmtpConnectionTimeout())) {
            fieldErrors.put("mailSmtpConnectionTimeout", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(restModel.getMailSmtpTimeout())) {
            fieldErrors.put("mailSmtpTimeout", "Not an Integer.");
        }

        if (StringUtils.isNotBlank(restModel.getMailSmtpEhlo()) && !isBoolean(restModel.getMailSmtpEhlo())) {
            fieldErrors.put("mailSmtpEhlo", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpAuth()) && !isBoolean(restModel.getMailSmtpAuth())) {
            fieldErrors.put("mailSmtpAuth", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpAllow8bitmime()) && !isBoolean(restModel.getMailSmtpAllow8bitmime())) {
            fieldErrors.put("mailSmtpAllow8bitmime", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpSendPartial()) && !isBoolean(restModel.getMailSmtpSendPartial())) {
            fieldErrors.put("mailSmtpSendPartial", "Not an Boolean.");
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String channelTestConfig(final EmailConfigRestModel restModel) throws IntegrationException {
        return emailChannel.testMessage(objectTransformer.configRestModelToDatabaseEntity(restModel, EmailConfigEntity.class));
    }

    @Override
    public List<String> sensitiveFields() {
        final List<String> sensitiveFields = new ArrayList<>();
        sensitiveFields.add("mailSmtpPassword");
        return sensitiveFields;
    }

}
