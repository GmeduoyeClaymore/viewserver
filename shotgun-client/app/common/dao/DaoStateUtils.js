import Logger from 'common/Logger';
import {isEqual} from 'lodash';

export const getDaoCommandStatus = (state, commandName, daoName) => {
  return state.getIn(['dao', daoName, 'commands', commandName, 'status']);
};
export const getDaoCommandResult = (state, commandName, daoName) => {
  return state.getIn(['dao', daoName, 'commands', commandName, 'message']);
};
export const getDaoState = (state, path, daoName) => {
  return state.getIn(['dao', daoName, 'data', ...path]);
};
export const getDaoOptions = (state, daoName) => {
  return state.getIn(['dao', daoName, 'options']);
};
export const getDaoSize = (state, daoName) => {
  return state.getIn(['dao', daoName, 'size']);
};

export const getAllDaos = (state) => {
  return state.getIn(['dao']);
};

export const getDao = (state, daoName) => {
  const dao = state.getIn(['dao', daoName]);

  if (dao == undefined){
    Logger.warning(`getDao for dao ${daoName} could not find dao`);
  }
  return dao;
};

export const isLoading = (state, daoName) => {
  return isOperationPending(state, daoName, 'updateSubscription');
};

export const isOperationPending = (state, daoName, operationName) => {
  const status = getDaoCommandStatus(state, operationName, daoName);
  return status === null || status === 'start';
};

export const getOperationError = (state, daoName, operationName) => {
  const status = getDaoCommandStatus(state, operationName, daoName);
  return status === 'fail' ? getDaoCommandResult(state, operationName, daoName) : undefined;
};

export const getLoadingError = (state, daoName) => {
  const status = getDaoCommandStatus(state, 'updateSubscription', daoName);
  return status === 'fail' ? getDaoCommandResult(state, 'updateSubscription', daoName) : undefined;
};

export const getLoadingErrors = (state, daoNames) => {
  return daoNames.map(nm => getLoadingError(state, nm)).filter(err => err).join( '\n');
};

export const getOperationErrors = (state, daoOperations) => {
  return daoOperations.map(nm => {const key = Object.keys(nm)[0]; return getOperationError(state, key, nm[key]);}).filter(err => err).join( '\n');
};

export const isAnyOperationPending = (state, daoOperations) => {
  return daoOperations.some( nm => {const key = Object.keys(nm)[0]; return isOperationPending(state, key, nm[key]);});
};

export const isAnyLoading = (state, daoNames) => {
  return daoNames.some( dao => getDao(state, dao) == undefined) || daoNames.some( nm => isLoading(state, nm));
};

export const getNavigationProps = (props) => {
  return (props && props.location && props.location.state ? props.location.state : {});
};

export const hasAnyOptionChanged = (prev = {}, current = {}, properties = []) => {
  for (let i = 0, len = properties.length; i < len; i++) {
    const propertyName = properties[i];
    const prevValue = prev[propertyName];
    const currentValue = current[propertyName];
    if (!prevValue && !currentValue){
      continue;
    }
    if (!isEqual(prevValue, currentValue)){
      return true;
    }
  }
  return false;
};
