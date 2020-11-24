package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class DistributionJobRequestModel {
    private final boolean enabled;
    private final String name;
    private final FrequencyType distributionFrequency;
    private final ProcessingType processingType;
    private final String channelDescriptorName;

    // Black Duck fields will be common as long as it is the only provider
    private final Long blackDuckGlobalConfigId;
    private final boolean filterByProject;
    @Nullable
    private final String projectNamePattern;
    private final List<String> notificationTypes;
    private final List<String> projectFilterProjectNames;
    private final List<String> policyFilterPolicyNames;
    private final List<String> vulnerabilityFilterSeverityNames;

    private final DistributionJobDetailsModel distributionJobDetails;

    public DistributionJobRequestModel(
        boolean enabled,
        String name,
        FrequencyType distributionFrequency,
        ProcessingType processingType,
        String channelDescriptorName,
        Long blackDuckGlobalConfigId,
        boolean filterByProject,
        @Nullable String projectNamePattern,
        List<String> notificationTypes,
        List<String> projectFilterProjectNames,
        List<String> policyFilterPolicyNames,
        List<String> vulnerabilityFilterSeverityNames,
        DistributionJobDetailsModel distributionJobDetails
    ) {
        this.enabled = enabled;
        this.name = name;
        this.distributionFrequency = distributionFrequency;
        this.processingType = processingType;
        this.channelDescriptorName = channelDescriptorName;
        this.blackDuckGlobalConfigId = blackDuckGlobalConfigId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.notificationTypes = notificationTypes;
        this.projectFilterProjectNames = projectFilterProjectNames;
        this.policyFilterPolicyNames = policyFilterPolicyNames;
        this.vulnerabilityFilterSeverityNames = vulnerabilityFilterSeverityNames;
        this.distributionJobDetails = distributionJobDetails;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public FrequencyType getDistributionFrequency() {
        return distributionFrequency;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelDescriptorName() {
        return channelDescriptorName;
    }

    public Long getBlackDuckGlobalConfigId() {
        return blackDuckGlobalConfigId;
    }

    public boolean isFilterByProject() {
        return filterByProject;
    }

    public Optional<String> getProjectNamePattern() {
        return Optional.ofNullable(projectNamePattern);
    }

    public List<String> getNotificationTypes() {
        return notificationTypes;
    }

    public List<String> getProjectFilterProjectNames() {
        return projectFilterProjectNames;
    }

    public List<String> getPolicyFilterPolicyNames() {
        return policyFilterPolicyNames;
    }

    public List<String> getVulnerabilityFilterSeverityNames() {
        return vulnerabilityFilterSeverityNames;
    }

    public DistributionJobDetailsModel getDistributionJobDetails() {
        return distributionJobDetails;
    }
}
