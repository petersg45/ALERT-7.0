/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class DistributionJobDetailsModelJsonAdapter implements JsonSerializer<DistributionJobDetailsModel>, JsonDeserializer<DistributionJobDetailsModel> {
    @Override
    public JsonElement serialize(DistributionJobDetailsModel distributionJobDetailsModel, Type type, JsonSerializationContext context) {
        return context.serialize(distributionJobDetailsModel);
    }

    @Override
    public DistributionJobDetailsModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MutableChannelKey mutableChannelKey = context.deserialize(jsonObject.getAsJsonObject("channelKey"), MutableChannelKey.class);
        Class<? extends DistributionJobDetailsModel> concreteClass = DistributionJobDetailsModel.getConcreteClass(mutableChannelKey.asChannelKey());
        if (null != concreteClass) {
            return context.deserialize(jsonObject, concreteClass);
        }
        throw new JsonParseException("Could not find a suitable class for deserialization");
    }

    private static final class MutableChannelKey {
        public String universalKey;
        public String displayName;

        public ChannelKey asChannelKey() {
            return new ChannelKey(universalKey, displayName);
        }

    }

}