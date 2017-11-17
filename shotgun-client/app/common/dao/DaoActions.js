import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, INVOKE_DAO_COMMAND} from '../ActionConstants';

export const invokeDaoCommand = (daoName, method, options) => {
    return {
        type: INVOKE_DAO_COMMAND,
        daoName,
        method,
        payload: options
    };
};

export const updateSubscriptionAction = (daoName, options) => {
    return invokeDaoCommand(daoName, 'updateSubscription', options);
};

export const pageAction = (daoName, offset, limit) => {
    return invokeDaoCommand(daoName, 'page', {offset, limit});
};

