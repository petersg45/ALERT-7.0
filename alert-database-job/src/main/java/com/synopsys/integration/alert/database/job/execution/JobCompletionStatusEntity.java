package com.synopsys.integration.alert.database.job.execution;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_completion_status")
public class JobCompletionStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -3107164032971829096L;
    @Id
    @Column(name = "job_config_id")
    private UUID jobConfigId;

    @Column(name = "latest_notification_count")
    private Long latestNotificationCount;

    @Column(name = "average_notification_count")
    private Long averageNotificationCount;
    @Column(name = "success_count")
    private Long successCount;
    @Column(name = "failure_count")
    private Long failureCount;

    @Column(name = "latest_status")
    private String latestStatus;
    @Column(name = "last_run")
    private OffsetDateTime lastRun;

    @Column(name = "duration_nanoseconds")
    private Long durationNanos;

    public JobCompletionStatusEntity() {
        // default constructor for JPA
    }

    public JobCompletionStatusEntity(
        UUID jobConfigId,
        Long latestNotificationCount,
        Long averageNotificationCount,
        Long successCount,
        Long failureCount,
        String latestStatus,
        OffsetDateTime lastRun,
        Long durationNanos
    ) {
        this.jobConfigId = jobConfigId;
        this.latestNotificationCount = latestNotificationCount;
        this.averageNotificationCount = averageNotificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
        this.durationNanos = durationNanos;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getLatestNotificationCount() {
        return latestNotificationCount;
    }

    public Long getAverageNotificationCount() {
        return averageNotificationCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }

    public Long getDurationNanos() {
        return durationNanos;
    }
}
