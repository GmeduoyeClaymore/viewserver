import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, INVOKE_DAO_COMMAND} from 'common/dao/ActionConstants';

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

export const registerDao = (dao) => {
    return {
        type: REGISTER_DAO_ACTION,
        name: dao.name,
        dao
    };
};


