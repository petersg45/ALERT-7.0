/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.contract.NotificationContentData;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public abstract class NotificationDetailExtractor<T extends NotificationContentComponent, U extends NotificationContentData<T>> {
    private final NotificationType notificationType;
    private final Class<U> notificationViewClass;
    private final Gson gson;

    public NotificationDetailExtractor(NotificationType notificationType, Class<U> notificationViewClass, Gson gson) {
        this.notificationType = notificationType;
        this.notificationViewClass = notificationViewClass;
        this.gson = gson;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public final List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel) {
        U notificationView = gson.fromJson(alertNotificationModel.getContent(), notificationViewClass);
        T notificationContent = notificationView.getContent();
        return extractDetailedContent(alertNotificationModel, notificationContent);
    }

    protected abstract List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, T notificationContent);

}
