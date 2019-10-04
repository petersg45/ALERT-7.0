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
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.PolicyPriorityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Component
public class BomEditMessageBuilder implements BlackDuckMessageBuilder<BomEditNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(BomEditMessageBuilder.class);
    private VulnerabilityUtil vulnerabilityUtil;
    private PolicyPriorityUtil policyPriorityUtil;
    private ComponentBuilderUtil componentBuilderUtil;
    private PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public BomEditMessageBuilder(VulnerabilityUtil vulnerabilityUtil, PolicyPriorityUtil policyPriorityUtil, ComponentBuilderUtil componentBuilderUtil, PolicyCommonBuilder policyCommonBuilder) {
        this.vulnerabilityUtil = vulnerabilityUtil;
        this.policyPriorityUtil = policyPriorityUtil;
        this.componentBuilderUtil = componentBuilderUtil;
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.BOM_EDIT.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, BomEditNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        BomEditNotificationContent bomEditContent = notificationView.getContent();
        Optional<VersionBomComponentView> bomComponent = responseCache.getBomComponentView(bomEditContent.getBomComponent());
        Optional<ProjectVersionWrapper> projectVersionWrapper = bomComponent.flatMap(responseCache::getProjectVersionWrapper);

        if (bomComponent.isPresent() && projectVersionWrapper.isPresent()) {
            try {
                VersionBomComponentView versionBomComponentView = bomComponent.get();
                ProjectVersionWrapper projectVersionData = projectVersionWrapper.get();
                ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                                  .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                                  .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, projectVersionData.getProjectView().getName())
                                                                                  .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, projectVersionData.getProjectVersionView().getVersionName(),
                                                                                      projectVersionData.getProjectVersionView().getHref().orElse(null))
                                                                                  .applyProviderCreationTime(providerCreationDate);

                List<LinkableItem> commonAttributes = Stream.concat(componentBuilderUtil.getLicenseLinkableItems(bomComponent.get()).stream(), componentBuilderUtil.getUsageLinkableItems(bomComponent.get()).stream())
                                                          .collect(Collectors.toList());

                List<ComponentItem> componentItems = new LinkedList<>(addVulnerabilityData(responseCache, componentService, notificationId, versionBomComponentView, projectVersionData, commonAttributes));
                projectVersionWrapper.ifPresent(versionWrapper -> componentItems.addAll(createPolicyItems(responseCache, blackDuckService, notificationId, versionWrapper, versionBomComponentView, commonAttributes)));

                projectVersionMessageBuilder.applyAllComponentItems(componentItems);
                return List.of(projectVersionMessageBuilder.build());
            } catch (AlertException ex) {
                logger.error("Error creating policy violation message.", ex);
            }
        }
        return List.of();
    }

    private Collection<ComponentItem> addVulnerabilityData(BlackDuckResponseCache blackDuckResponseCache, ComponentService componentService, Long notificationId, VersionBomComponentView versionBomComponent,
        ProjectVersionWrapper projectVersionWrapper, List<LinkableItem> commonAttributes) {
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl);
            if (vulnerabilityUtil.doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                List<LinkableItem> componentAttributes = new LinkedList<>();
                componentAttributes.addAll(commonAttributes);

                Optional<ComponentVersionView> componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, versionBomComponent.getComponentVersion());
                if (componentVersionView.isPresent()) {
                    List<LinkableItem> remediationItems = vulnerabilityUtil.getRemediationItems(componentService, componentVersionView.get());
                    componentAttributes.addAll(remediationItems);
                }
                ComponentItem.Builder builder = new ComponentItem.Builder()
                                                    .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_VULNERABILITY)
                                                    .applyOperation(ItemOperation.UPDATE)
                                                    // FIXME get the vulnerability id(s) and create a ComponentItem from each of them
                                                    .applyCategoryItem(new LinkableItem(MessageBuilderConstants.LABEL_VULNERABILITIES, "Present"))
                                                    .applyCollapseOnCategory(true)
                                                    .applyAllComponentAttributes(componentAttributes)
                                                    .applyNotificationId(notificationId);
                componentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
                try {
                    items.add(builder.build());
                } catch (AlertException alertException) {
                    logger.warn("Error building vulnerability BOM edit component for notification {}, operation {}, component {}, component version {}", notificationId, ItemOperation.UPDATE, componentName, componentVersionName);
                    logger.error("Error building vulnerability BOM edit component cause ", alertException);
                }
            }
        } catch (Exception genericException) {
            logger.error("BOM Edit: Error processing vulnerabilities ", genericException);
        }
        return items;
    }

    private Collection<ComponentItem> createPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, Long notificationId, ProjectVersionWrapper projectVersionWrapper,
        VersionBomComponentView versionBomComponent, List<LinkableItem> commonAttributes) {
        if (!PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            return List.of();
        }
        Collection<ComponentItem> items = new LinkedList<>();
        try {
            String componentName = versionBomComponent.getComponentName();
            String componentVersionName = versionBomComponent.getComponentVersionName();
            String projectVersionUrl = projectVersionWrapper.getProjectVersionView().getHref().orElse(null);
            ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl);
            List<VersionBomPolicyRuleView> policyRules = blackDuckService.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (VersionBomPolicyRuleView rule : policyRules) {
                if (!PolicySummaryStatusType.IN_VIOLATION.equals(rule.getPolicyApprovalStatus())) {
                    continue;
                }

                LinkableItem policyNameItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_NAME, rule.getName(), null);
                LinkableItem policySeverityItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_SEVERITY_NAME, rule.getSeverity());
                if (policyCommonBuilder.hasVulnerabilityRule(rule)) {
                    List<VulnerableComponentView> vulnerableComponentViews = vulnerabilityUtil.getVulnerableComponentViews(blackDuckService, projectVersionWrapper, versionBomComponent);
                    List<ComponentItem> vulnerabilityComponentItems =
                        policyCommonBuilder.createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverityItem, componentData, notificationId, blackDuckService,
                            blackDuckResponseCache);
                    items.addAll(vulnerabilityComponentItems);
                } else {
                    ComponentItem.Builder builder = new ComponentItem.Builder()
                                                        .applyCategory(MessageBuilderConstants.CATEGORY_TYPE_POLICY)
                                                        .applyOperation(ItemOperation.UPDATE)
                                                        .applyPriority(policyPriorityUtil.getPriorityFromSeverity(rule.getSeverity()))
                                                        .applyCategoryItem(policyNameItem)
                                                        .applyAllComponentAttributes(commonAttributes)
                                                        .applyNotificationId(notificationId);
                    componentBuilderUtil.applyComponentInformation(builder, blackDuckResponseCache, componentData);
                    items.add(builder.build());
                }
            }
        } catch (Exception e) {
            logger.error("BOM Edit: Error processing policy ", e);
        }

        return items;
    }
}
