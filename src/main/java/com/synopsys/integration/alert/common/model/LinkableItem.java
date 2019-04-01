/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.model;

import java.util.Optional;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.synopsys.integration.util.Stringable;

public class LinkableItem extends Stringable implements Comparable<LinkableItem> {
    private final String name;
    private final String value;
    private final String url;

    public LinkableItem(final String name, final String value) {
        this(name, value, null);
    }

    public LinkableItem(final String name, final String value, final String url) {
        this.name = name;
        this.value = value;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    @Override
    public int compareTo(final LinkableItem otherItem) {
        return CompareToBuilder.reflectionCompare(this, otherItem);
    }
}
