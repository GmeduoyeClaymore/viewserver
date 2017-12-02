import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, INVOKE_DAO_COMMAND} from 'common/dao/ActionConstants';
import {getDaoOptions} from './DaoStateUtils';
import {isEqual} from 'lodash';

export const invokeDaoCommand = (daoName, method, options, continueWith) => {
  return {
    type: INVOKE_DAO_COMMAND(daoName, method),
    daoName,
    method,
    payload: options,
    continueWith
  };
};

const getExistingValues = (existingOptions, options) => {
  const existingValues = {};
  Object.keys(options).forEach(op => {existingValues[op] = existingOptions[op];});
  return existingValues;
};

export const updateSubscriptionAction = (daoName, options, continueWith) => {
  return async (dispatch, getState) => {
    const existingOptions = getDaoOptions(getState(), daoName);
    if (!existingOptions || !isEqual(getExistingValues(existingOptions, options), options)){
      dispatch(invokeDaoCommand(daoName, 'updateSubscription', options, continueWith));
    } else if (continueWith){
      continueWith();
    }
  };
};

export const pageAction = (daoName, offset, limit) => {
  return invokeDaoCommand(daoName, 'page', {offset, limit});
};

export const registerDao = (dao) => {
  return {
    type: REGISTER_DAO_ACTION(dao.name),
    name: dao.name,
    dao
  };
};

export const unregisterDao = (daoName) => {
  return {
    type: UNREGISTER_DAO_ACTION(daoName),
    name: daoName
  };
};


