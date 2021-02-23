/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common;

import java.net.MalformedURLException;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import com.synopsys.integration.alert.common.exception.AlertException;

public class AlertProperties {
    @Value("${alert.config.home:}")
    private String alertConfigHome;

    @Value("${alert.images.dir:}")
    private String alertImagesDir;

    @Value("${alert.secrets.dir:/run/secrets}")
    private String alertSecretsDir;

    @Value("${alert.email.attachments.dir:./email/attachments}")
    private String alertEmailAttachmentsDir;

    @Value("${alert.trust.cert:}")
    private Boolean alertTrustCertificate;

    @Value("${alert.encryption.password:}")
    private String alertEncryptionPassword;

    @Value("${alert.encryption.global.salt:}")
    private String alertEncryptionGlobalSalt;

    @Value("${alert.logging.level:INFO}")
    private String loggingLevel;

    // SSL properties
    @Value("${server.port:}")
    private String serverPort;

    @Value("${public.alert.webserver.port:}")
    private String publicServerPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.ssl.key-store:}")
    private String keyStoreFile;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePass;

    @Value("${server.ssl.keyStoreType:}")
    private String keyStoreType;

    @Value("${server.ssl.keyAlias:}")
    private String keyAlias;

    @Value("${server.ssl.trust-store:}")
    private String trustStoreFile;

    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePass;

    @Value("${server.ssl.trustStoreType:}")
    private String trustStoreType;

    @Value("${spring.h2.console.enabled:false}")
    private Boolean h2ConsoleEnabled;

    @Value("${server.ssl.enabled:false}")
    private Boolean sslEnabled;

    @Value("${alert.hostname:}")
    private String alertHostName;

    public String getAlertConfigHome() {
        return StringUtils.trimToNull(alertConfigHome);
    }

    public String getAlertImagesDir() {
        return StringUtils.trimToNull(alertImagesDir);
    }

    public String getAlertLogo() throws AlertException {
        String imagesDirectory = getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return imagesDirectory + "/synopsys.png";
        }
        throw new AlertException(String.format("Could not find the Alert logo in the images directory '%s'", imagesDirectory));
    }

    public String getAlertSecretsDir() {
        return StringUtils.trimToNull(alertSecretsDir);
    }

    public String getAlertEmailAttachmentsDir() {
        return alertEmailAttachmentsDir;
    }

    public boolean getH2ConsoleEnabled() {
        return BooleanUtils.isTrue(h2ConsoleEnabled);
    }

    public boolean getSslEnabled() {
        return BooleanUtils.isTrue(sslEnabled);
    }

    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public Optional<String> getAlertEncryptionPassword() {
        return getOptionalString(alertEncryptionPassword);
    }

    public Optional<String> getAlertEncryptionGlobalSalt() {
        return getOptionalString(alertEncryptionGlobalSalt);
    }

    public Optional<String> getLoggingLevel() {
        return getOptionalString(loggingLevel);
    }

    public Optional<String> getServerPort() {
        return getOptionalString(serverPort);
    }

    public Optional<String> getPublicServerPort() {
        return getOptionalString(publicServerPort);
    }

    public Optional<String> getContextPath() {
        return getOptionalString(contextPath);
    }

    public Optional<String> getKeyAlias() {
        return getOptionalString(keyAlias);
    }

    public Optional<String> getKeyStoreFile() {
        return getOptionalString(keyStoreFile);
    }

    public Optional<String> getKeyStorePass() {
        return getOptionalString(keyStorePass);
    }

    public Optional<String> getKeyStoreType() {
        return getOptionalString(keyStoreType);
    }

    public Optional<String> getTrustStoreFile() {
        return getOptionalString(trustStoreFile);
    }

    public Optional<String> getTrustStorePass() {
        return getOptionalString(trustStorePass);
    }

    public Optional<String> getTrustStoreType() {
        return getOptionalString(trustStoreType);
    }

    public Optional<String> getAlertHostName() {
        return getOptionalString(alertHostName);
    }

    private Optional<String> getOptionalString(String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public UriComponentsBuilder getServerUrlBuilder() {
        String scheme = getSslEnabled() ? "https" : "http";
        String hostName = getAlertHostName().orElse("localhost");
        String port = getPublicServerPort().orElse(getServerPort().orElse("8443"));
        String path = getContextPath().orElse("alert");

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        uriComponentsBuilder.scheme(scheme);
        uriComponentsBuilder.host(hostName);
        uriComponentsBuilder.port(port);
        uriComponentsBuilder.path(path);
        return uriComponentsBuilder;
    }

    // TODO investigate using ServletUriComponentsBuilder.fromCurrentContextPath() to construct the URL
    //  for code paths that are triggered from a HttpServletRequest

    public Optional<String> getServerUrl(String... pathSegments) {
        UriComponentsBuilder uriComponentsBuilder = getServerUrlBuilder();
        uriComponentsBuilder.pathSegment(pathSegments);
        try {
            String serverUrl = uriComponentsBuilder.build().toUri().toURL().toString();
            return Optional.of(serverUrl);
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public String getRootURL() {
        UriComponentsBuilder uriComponentsBuilder = getServerUrlBuilder();
        uriComponentsBuilder.path("/");
        return uriComponentsBuilder.toUriString();

    }
}
