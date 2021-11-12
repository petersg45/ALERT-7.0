import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import { SETTINGS_INFO } from 'page/settings/SettingsModel';
import SettingsEncryptionConfiguration from 'page/settings/standalone/SettingsEncryptionConfiguration.js';

const SettingsConfigurationStandalone = ({
    csrfToken, errorHandler, readOnly, displaySave
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SETTINGS_INFO.key));

    return (
        <CommonGlobalConfiguration
            label={`${SETTINGS_INFO.label} BETA (WIP)`}
            description="This page allows you to configure the global settings. (WIP: Everything on this page is currently in development)"
            lastUpdated={formData.lastUpdated}
        >
            <Tabs defaultActiveKey={1} id="settings-tabs">
                <Tab eventKey={1} title="SettingsEncryption">
                    <SettingsEncryptionConfiguration
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                        readonly={readOnly}
                        displaySave={displaySave}
                    />
                </Tab>
            </Tabs>
        </CommonGlobalConfiguration>
    );
};

SettingsConfigurationStandalone.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readOnly: PropTypes.bool,
    displaySave: PropTypes.bool
};

SettingsConfigurationStandalone.defaultProps = {
    readOnly: false,
    displaySave: true
};

export default SettingsConfigurationStandalone;
