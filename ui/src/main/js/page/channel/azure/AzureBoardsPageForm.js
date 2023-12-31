import React, { useEffect, useState } from 'react';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED, AZURE_BOARDS_INFO, AZURE_BOARDS_URLS } from 'page/channel/azure/AzureBoardsModel';
import PageHeader from 'common/component/navigation/PageHeader';
import PasswordInput from 'common/component/input/PasswordInput';
import OAuthEndpointButtonField from 'common/component/input/field/OAuthEndpointButtonField';
import TextInput from 'common/component/input/TextInput';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';

import ButtonField from 'common/component/input/field/ButtonField';


const AzureBoardsForm = ({ csrfToken, errorHandler, readonly, displayTest }) => {
    const { id } = useParams();
    const history = useHistory();
    const location = useLocation();

    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const [buttonErrorMessage, setButtonErrorMessage] = useState('');
    const [buttonSuccess, setButtonSuccess] = useState(false);
    const [buttonMessage, setButtonMessage] = useState('');

    const authenticateAzureForm = async () => {
        setButtonErrorMessage('');
        setButtonSuccess(false);
        // const testObj = {
        //     "totalPages": 0,
        //     "currentPage": 0,
        //     "pageSize": 10,
        //     "models": [],
        //     "name": "12",
        //     "name": "3",
        //     "id": "4",
        //     "secret": "5"
        // }
        const response = await ConfigurationRequestBuilder.createNewConfigurationRequest('/api/configuration/azure-boards/oauth/authenticate', csrfToken, formData);
        const data = await response.json();
        const hasErrors = HttpErrorUtilities.isError(response.status) && data.error && data.error.trim() !== '';

        setButtonSuccess(!hasErrors);
        setErrors(HttpErrorUtilities.createErrorObject(data));
        if (hasErrors) {
            setButtonErrorMessage(HttpErrorUtilities.createFieldError(data.error));
        } else {
            setButtonMessage(data.message);
        }
    };

    const azureBoardsRequestUrl = `${ConfigurationRequestBuilder.AZURE_BOARDS_API_URL}`;

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(azureBoardsRequestUrl, csrfToken, id);
        const data = await response.json();
        if (data) {
            if (location.pathname.includes('/copy')) {
                delete data.id;
                delete data.isAppIdSet;
                delete data.isClientSecretSet;
            }
            setFormData(data);
        }
    };

    useEffect(() => {
        // When editing or copying azure board, both id and models field will be present
        if (formData?.models?.length >= 1 && id) {
            const selectedAzureData = formData.models.find(azureModel => azureModel.id === id);
            setFormData(selectedAzureData);
        }
    }, [formData, id]);

    function postData() {
        return ConfigurationRequestBuilder.createNewConfigurationRequest(azureBoardsRequestUrl, csrfToken, formData);
    }

    function updateData() {
        return ConfigurationRequestBuilder.createUpdateRequest(azureBoardsRequestUrl, csrfToken, id, formData);
    }

    function deleteData() {
        return ConfigurationRequestBuilder.createDeleteRequest(azureBoardsRequestUrl, csrfToken, formData.id);
    }

    function validateData() {
        return ConfigurationRequestBuilder.createValidateRequest(azureBoardsRequestUrl, csrfToken, formData);
    }

    function testData() {
        return ConfigurationRequestBuilder.createTestRequest(azureBoardsRequestUrl, csrfToken, formData);
    }

    return (
        <div>
            <PageHeader
                title={AZURE_BOARDS_INFO.label}
                description="Configure the Azure Boards instance that Alert will send issue updates to."
                lastUpdated={formData.lastUpdated}
            />
            <ConcreteConfigurationForm
                formDataId={formData.id}
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                deleteRequest={deleteData}
                updateRequest={updateData}
                createRequest={postData}
                displayCancel
                displayDelete={false}
                validateRequest={validateData}
                testRequest={testData}
                errorHandler={errorHandler}
                afterSuccessfulSave={() => history.push(AZURE_BOARDS_URLS.mainUrl)}
            >
                <TextInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.name}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.name}
                    label="Name"
                    description="The name of the Azure Board for your identification purposes."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.name || undefined}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.name}
                    errorValue={errors.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.name]}
                />
                <TextInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.organization}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.organization}
                    label="Organization Name"
                    description="The name of the Azure DevOps organization."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.organizationName || undefined}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.organization}
                    errorValue={errors.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.organization]}
                />
                <PasswordInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.appId}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.appId}
                    label="App ID"
                    description="The App ID created for Alert when registering your Azure DevOps Client Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.appId || undefined}
                    isSet={formData.isAppIdSet}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.appId}
                    errorValue={errors.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.appId]}
                />
                <PasswordInput
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.clientSecret}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.clientSecret}
                    label="Client Secret"
                    description="The Client secret created for Alert when registering your Azure DevOps Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.clientSecret || undefined}
                    isSet={formData.isClientSecretSet}
                    errorName={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.clientSecret}
                    errorValue={errors.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.clientSecret]}
                />
                <OAuthEndpointButtonField
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    buttonLabel="Authenticate"
                    label="Microsoft OAuth"
                    description="This will redirect you to Microsoft's OAuth login. To clear the Oauth request cache, please delete and reconfigure the Azure fields.  Please note you will remain logged in; for security reasons you may want to logout of your Microsoft account after authenticating the application."
                    endpoint="/api/function"
                    csrfToken={csrfToken}
                    currentConfig={formData}
                    fieldKey={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    requiredRelatedFields={[
                        AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.organization,
                        AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.appId,
                        AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.clientSecret
                    ]}
                    readOnly={readonly || !displayTest}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth)}
                    errorValue={errors.fieldErrors[AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth]}
                />
                <ButtonField
                    id={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    name={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    label="Authenticate TEST"
                    buttonLabel="Authenticate TEST"
                    description="Installs a required plugin on the Jira server."
                    onSendClick={authenticateAzureForm}
                    fieldKey={AZURE_BOARDS_GLOBAL_FIELD_KEYS_UPDATED.configureOAuth}
                    fieldError={buttonErrorMessage}
                    readOnly={readonly || !displayTest}
                    success={buttonSuccess}
                    statusMessage={buttonMessage}
                />
            </ConcreteConfigurationForm>
        </div>
    );
};

AzureBoardsForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

AzureBoardsForm.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default AzureBoardsForm;
