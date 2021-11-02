/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class DatabaseModelWrapper<T extends AlertSerializableModel> extends AlertSerializableModel {
    private final Long descriptorId;
    private final Long configurationId;
    private final String createdAt;
    private final String lastUpdated;
    private final T model;

    public DatabaseModelWrapper(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, T model) {
        descriptorId = registeredDescriptorId;
        configurationId = descriptorConfigId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

}