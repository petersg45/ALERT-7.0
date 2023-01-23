package com.synopsys.integration.alert.database.api;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionModel;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobStageModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.execution.JobExecutionEntity;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStageEntity;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStageRepository;

@Component
public class DefaultJobExecutionAccessor implements JobExecutionAccessor {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionStageRepository jobExecutionStageRepository;

    @Autowired
    public DefaultJobExecutionAccessor(JobExecutionRepository jobExecutionRepository, JobExecutionStageRepository jobExecutionStageRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobExecutionStageRepository = jobExecutionStageRepository;
    }

    @Override
    @Transactional
    public JobExecutionModel startJob(UUID jobConfigId, int totalNotificationCount) {
        JobExecutionEntity entity = new JobExecutionEntity(
            UUID.randomUUID(),
            jobConfigId,
            DateUtils.createCurrentDateTimestamp(),
            null,
            AuditEntryStatus.PENDING.name(),
            0,
            totalNotificationCount
        );
        entity = jobExecutionRepository.save(entity);
        return convertToExecutionModel(entity);
    }

    @Override
    @Transactional
    public void endJobWithSuccess(UUID executionId, Instant endTime) {
        endJobWIthStatus(executionId, endTime, AuditEntryStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void endJobWithFailure(UUID executionId, Instant endTime) {
        endJobWIthStatus(executionId, endTime, AuditEntryStatus.FAILURE);
    }

    private void endJobWIthStatus(UUID executionId, Instant endTime, AuditEntryStatus status) {
        Optional<JobExecutionEntity> searchedEntity = jobExecutionRepository.findById(executionId);
        if (searchedEntity.isPresent()) {
            OffsetDateTime end = DateUtils.fromInstantUTC(endTime);
            JobExecutionEntity savedEntity = searchedEntity.get();
            JobExecutionEntity updatedEntity = new JobExecutionEntity(
                savedEntity.getExecutionId(),
                savedEntity.getJobConfigId(),
                savedEntity.getStart(),
                end,
                status.name(),
                savedEntity.getProcessedNotificationCount(),
                savedEntity.getTotalNotificationCount()
            );
            jobExecutionRepository.save(updatedEntity);
        }
    }

    @Override
    @Transactional
    public void incrementNotificationCount(UUID jobExecutionId, int notificationCount) {
        Optional<JobExecutionEntity> searchedEntity = jobExecutionRepository.findById(jobExecutionId);
        if (searchedEntity.isPresent()) {
            JobExecutionEntity entity = searchedEntity.get();
            int incrementedValue = entity.getProcessedNotificationCount() + notificationCount;
            jobExecutionRepository.save(new JobExecutionEntity(
                entity.getExecutionId(),
                entity.getJobConfigId(),
                entity.getStart(),
                entity.getEnd(),
                entity.getStatus(),
                incrementedValue,
                entity.getTotalNotificationCount()
            ));
        }
    }

    @Override
    @Transactional
    public Optional<JobExecutionModel> getJobExecution(UUID jobExecutionId) {
        return jobExecutionRepository.findById(jobExecutionId).map(this::convertToExecutionModel);
    }

    @Override
    @Transactional
    public AlertPagedModel<JobExecutionModel> getExecutingJobs(AlertPagedQueryDetails pagedQueryDetails) {
        PageRequest pageRequest = PageRequest.of(pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit());
        Page<JobExecutionEntity> page = jobExecutionRepository.findAllByStatusIn(Set.of(AuditEntryStatus.PENDING.name()), pageRequest);
        List<JobExecutionModel> data = page
            .stream()
            .map(this::convertToExecutionModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(page.getTotalPages(), page.getNumber(), page.getNumberOfElements(), data);
    }

    @Override
    @Transactional
    public AlertPagedModel<JobExecutionModel> getCompletedJobs(AlertPagedQueryDetails pagedQueryDetails) {
        PageRequest pageRequest = PageRequest.of(pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit());
        Page<JobExecutionEntity> page = jobExecutionRepository.findAllByStatusIn(Set.of(AuditEntryStatus.FAILURE.name(), AuditEntryStatus.SUCCESS.name()), pageRequest);
        List<JobExecutionModel> data = page
            .stream()
            .map(this::convertToExecutionModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(page.getTotalPages(), page.getNumber(), page.getNumberOfElements(), data);
    }

    @Override
    @Transactional
    public Long countJobsByStatus(AuditEntryStatus status) {
        return jobExecutionRepository.countAllByStatusIn(Set.of(status.name()));
    }

    @Override
    @Transactional
    public Optional<JobStageModel> getJobStage(UUID jobExecutionId, String stageName) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public AlertPagedModel<JobStageModel> getJobStages(UUID jobExecutionId, AlertPagedQueryDetails pagedQueryDetails) {
        PageRequest pageRequest = PageRequest.of(pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit());
        Page<JobExecutionStageEntity> page = jobExecutionStageRepository.findAllByExecutionId(jobExecutionId, pageRequest);
        List<JobStageModel> data = page
            .stream()
            .map(this::convertToStageModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(page.getTotalPages(), page.getNumber(), page.getNumberOfElements(), data);
    }

    @Override
    @Transactional
    public void startStage(UUID executionId, String name, Instant start) {
        Optional<JobExecutionStageEntity> stageData = jobExecutionStageRepository.findByExecutionIdAndStage(executionId, name);
        if (stageData.isPresent()) {
            JobExecutionStageEntity stage = stageData.get();
            JobExecutionStageEntity updatedStage = new JobExecutionStageEntity(
                stage.getId(),
                stage.getExecutionId(),
                stage.getStage(),
                DateUtils.fromInstantUTC(start),
                stage.getEnd()
            );
            jobExecutionStageRepository.save(updatedStage);
        } else {
            jobExecutionStageRepository.save(new JobExecutionStageEntity(UUID.randomUUID(), executionId, name, DateUtils.fromInstantUTC(start), null));
        }
    }

    @Override
    @Transactional
    public void endStage(UUID executionId, String stageName, Instant end) {
        Optional<JobExecutionStageEntity> stageData = jobExecutionStageRepository.findByExecutionIdAndStage(executionId, stageName);
        if (stageData.isPresent()) {
            JobExecutionStageEntity stage = stageData.get();
            JobExecutionStageEntity updatedStage = new JobExecutionStageEntity(
                stage.getId(),
                stage.getExecutionId(),
                stage.getStage(),
                stage.getStart(),
                DateUtils.fromInstantUTC(end)
            );
            jobExecutionStageRepository.save(updatedStage);
        }
    }

    @Override
    @Transactional
    public void purgeJob(UUID executionId) {
        jobExecutionRepository.deleteById(executionId);
    }

    private JobExecutionModel convertToExecutionModel(JobExecutionEntity entity) {
        return new JobExecutionModel(
            entity.getExecutionId(),
            entity.getJobConfigId(),
            entity.getStart(),
            entity.getEnd(),
            AuditEntryStatus.valueOf(entity.getStatus()),
            entity.getProcessedNotificationCount(),
            entity.getTotalNotificationCount()
        );
    }

    private JobStageModel convertToStageModel(JobExecutionStageEntity entity) {
        return new JobStageModel(entity.getId(), entity.getExecutionId(), entity.getStage(), entity.getStart(), entity.getEnd());
    }
}
