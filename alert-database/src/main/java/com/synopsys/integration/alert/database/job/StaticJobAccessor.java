package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

// @Component
public class StaticJobAccessor implements JobAccessor {
    private final DistributionJobRepository distributionJobRepository;

    // Temporary until all three tiers of the application have been updated to new Job models
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessor(DistributionJobRepository distributionJobRepository, RegisteredDescriptorRepository registeredDescriptorRepository, ProviderKey blackDuckProviderKey) {
        this.distributionJobRepository = distributionJobRepository;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    public List<ConfigurationJobModel> getAllJobs() {
        return distributionJobRepository.findAll()
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationJobModel> getJobsById(Collection<UUID> jobIds) {
        return distributionJobRepository.findAllById(jobIds)
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageOffset, int pageLimit, Collection<String> descriptorsNamesToInclude) {
        if (!descriptorsNamesToInclude.contains(blackDuckProviderKey.getUniversalKey())) {
            return new AlertPagedModel<>(0, pageOffset, pageLimit, List.of());
        }

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<ConfigurationJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findByChannelDescriptorNameIn(descriptorsNamesToInclude, pageRequest)
                                                                        .map(this::convertToConfigurationJobModel);
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageOffset, pageLimit, pageOfJobsWithDescriptorNames.getContent());
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) {
        return distributionJobRepository.findById(jobId).map(this::convertToConfigurationJobModel);
    }

    @Override
    public Optional<ConfigurationJobModel> getJobByName(String jobName) {
        return distributionJobRepository.findByName(jobName)
                   .map(this::convertToConfigurationJobModel);
    }

    @Override
    public List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency) {
        return distributionJobRepository.findByDistributionFrequency(frequency.name())
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        return null;
    }

    @Override
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
    }

    private ConfigurationJobModel convertToConfigurationJobModel(DistributionJobEntity jobEntity) {
        Set<ConfigurationModel> configurationModels = new LinkedHashSet<>();

        String createdAtDateTime = DateUtils.formatDate(jobEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String updatedAtDateTime = DateUtils.formatDate(jobEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        String providerUniversalKey = blackDuckProviderKey.getUniversalKey();
        Long blackDuckDescriptorId = getDescriptorId(providerUniversalKey);

        ConfigurationModelMutable blackDuckConfigurationModel = new ConfigurationModelMutable(blackDuckDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        JobConfigurationModelFieldPopulationUtils.populateBlackDuckConfigurationModelFields(jobEntity, blackDuckConfigurationModel);
        configurationModels.add(blackDuckConfigurationModel);

        Long channelDescriptorId = getDescriptorId(jobEntity.getChannelDescriptorName());
        ConfigurationModelMutable channelConfigurationModel = new ConfigurationModelMutable(channelDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        channelConfigurationModel.put(JobConfigurationModelFieldPopulationUtils.createConfigFieldModel("channel.common.provider.name", providerUniversalKey));
        JobConfigurationModelFieldPopulationUtils.populateChannelConfigurationModelFields(jobEntity, channelConfigurationModel);
        configurationModels.add(channelConfigurationModel);

        return new ConfigurationJobModel(jobEntity.getJobId(), configurationModels);
    }

    private Long getDescriptorId(String descriptorUniversalKey) {
        return registeredDescriptorRepository.findFirstByName(descriptorUniversalKey)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException("FATAL: A descriptor is missing from the database"));
    }

}
