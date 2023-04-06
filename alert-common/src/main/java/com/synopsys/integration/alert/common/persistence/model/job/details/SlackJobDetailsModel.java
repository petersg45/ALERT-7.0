/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class SlackJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;
    private final String channelUsername;

    // TODO: Make channelUsername @Nullable since it is an optional field. This would additional validation in SlackChannelMessageSender if it wasn't trimmed to null - JM
    public SlackJobDetailsModel(UUID jobId, String webhook, String channelUsername) {
        super(ChannelKeys.SLACK, jobId);
        this.webhook = webhook;
        this.channelUsername = StringUtils.trimToNull(channelUsername);
    }

    public String getWebhook() {
        return webhook;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

}
