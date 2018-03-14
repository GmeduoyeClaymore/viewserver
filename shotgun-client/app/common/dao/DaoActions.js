import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, INVOKE_DAO_COMMAND, UPDATE_COMMAND_STATUS} from 'common/dao/ActionConstants';
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

export const resetDataAction = (daoName, continueWith) => {
  return async (dispatch) => {
    dispatch(invokeDaoCommand(daoName, 'resetData', {}, continueWith));
  };
};

export const clearCommandStatus = (daoName, commandName) => {
  const path = [daoName, 'commands', commandName];
  return async (dispatch) => {
    dispatch({ type: UPDATE_COMMAND_STATUS(daoName, commandName), path, data: {status: undefined, message: undefined} });
  };
};

export const updateSubscriptionAction = (daoName, options, continueWith) => {
  return async (dispatch, getState) => {
    const existingOptions = getDaoOptions(getState(), daoName);
    if (!existingOptions || !isEqual(existingOptions, options)){
      dispatch(invokeDaoCommand(daoName, 'updateSubscription', options, continueWith));
    } else if (continueWith){
      continueWith();
    }
  };
};

export const resetSubscriptionAction = (daoName, options, continueWith) => {
  return async (dispatch, getState) => {
    const existingOptions = getDaoOptions(getState(), daoName);
    if (!existingOptions || !isEqual(existingOptions, options)){
      dispatch(invokeDaoCommand(daoName, 'resetSubscription', options, continueWith));
    } else if (continueWith){
      continueWith();
    }
  };
};

export const updateOptionsAction = (daoName, options, continueWith) => {
  return async (dispatch, getState) => {
    const existingOptions = getDaoOptions(getState(), daoName);
    if (!existingOptions || !isEqual(existingOptions, options)){
      dispatch(invokeDaoCommand(daoName, 'updateOptions', options, continueWith));
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


