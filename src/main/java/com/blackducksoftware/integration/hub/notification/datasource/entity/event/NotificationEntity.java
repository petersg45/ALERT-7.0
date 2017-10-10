package com.blackducksoftware.integration.hub.notification.datasource.entity.event;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "notification_events", schema = "notification")
public class NotificationEntity implements Serializable {
    private static final long serialVersionUID = -1194014350183607831L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "event_key")
    private String eventKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_version")
    private String projectVersion;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "component_version")
    private String componentVersion;

    @Column(name = "policy_rule_name")
    private String policyRuleName;

    @Column(name = "vulnerabilty_list")
    private String vulnerabilityList;

    public NotificationEntity() {
    }

    public NotificationEntity(final String eventKey, final Date createdAt, final String notificationType, final String projectName, final String projectVersion, final String componentName, final String componentVersion,
            final String policyRuleName, final String vulnerabilityList) {
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.policyRuleName = policyRuleName;
        this.vulnerabilityList = vulnerabilityList;
    }

    public Long getId() {
        return id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getPolicyRuleName() {
        return policyRuleName;
    }

    public String getVulnerabilityList() {
        return vulnerabilityList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
        result = prime * result + ((componentVersion == null) ? 0 : componentVersion.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((eventKey == null) ? 0 : eventKey.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
        result = prime * result + ((policyRuleName == null) ? 0 : policyRuleName.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((projectVersion == null) ? 0 : projectVersion.hashCode());
        result = prime * result + ((vulnerabilityList == null) ? 0 : vulnerabilityList.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NotificationEntity other = (NotificationEntity) obj;
        if (componentName == null) {
            if (other.componentName != null) {
                return false;
            }
        } else if (!componentName.equals(other.componentName)) {
            return false;
        }
        if (componentVersion == null) {
            if (other.componentVersion != null) {
                return false;
            }
        } else if (!componentVersion.equals(other.componentVersion)) {
            return false;
        }
        if (createdAt == null) {
            if (other.createdAt != null) {
                return false;
            }
        } else if (!createdAt.equals(other.createdAt)) {
            return false;
        }
        if (eventKey == null) {
            if (other.eventKey != null) {
                return false;
            }
        } else if (!eventKey.equals(other.eventKey)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (notificationType == null) {
            if (other.notificationType != null) {
                return false;
            }
        } else if (!notificationType.equals(other.notificationType)) {
            return false;
        }
        if (policyRuleName == null) {
            if (other.policyRuleName != null) {
                return false;
            }
        } else if (!policyRuleName.equals(other.policyRuleName)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        if (projectVersion == null) {
            if (other.projectVersion != null) {
                return false;
            }
        } else if (!projectVersion.equals(other.projectVersion)) {
            return false;
        }
        if (vulnerabilityList == null) {
            if (other.vulnerabilityList != null) {
                return false;
            }
        } else if (!vulnerabilityList.equals(other.vulnerabilityList)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotificationEntity [id=" + id + ", eventKey=" + eventKey + ", createdAt=" + createdAt + ", notificationType=" + notificationType + ", projectName=" + projectName + ", projectVersion=" + projectVersion + ", componentName="
                + componentName + ", componentVersion=" + componentVersion + ", policyRuleName=" + policyRuleName + ", vulnerabilityList=" + vulnerabilityList + "]";
    }
}
