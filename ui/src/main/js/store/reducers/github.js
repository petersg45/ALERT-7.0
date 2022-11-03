import {
    SERIALIZE,
    GET_GITHUB_SUCCESS,
    GET_GITHUB_FETCHING,
    GET_GITHUB_ERROR,
    ADD_GITHUB_USER_SUCCESS,
    ADD_GITHUB_USER_REQUEST,
    ADD_GITHUB_USER_FAIL,
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    roleError: null,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: ''
};

const roles = (state = initialState, action) => {
    switch (action.type) {
        case GET_GITHUB_FETCHING:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''
            };
        case GET_GITHUB_SUCCESS:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                data: action.data,
                fetching: false,
                saveStatus: ''
            };
        case GET_GITHUB_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false,
                saveStatus: ''
            };
        case ADD_GITHUB_USER_REQUEST:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            };
        case ADD_GITHUB_USER_FAIL:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {},
                saveStatus: 'ERROR'
            };
        case ADD_GITHUB_USER_SUCCESS:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {},
                saveStatus: 'SAVED'
            };
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default roles;