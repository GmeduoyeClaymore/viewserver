import Logger from 'common/Logger';

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

export const getOperationsErrors = (state, daoOperationPairs) => {
  return Object.entries(daoOperationPairs).map( nm => getOperationError(state, nm[0], nm[1])).filter(err => err).join( '\n');
};

export const isAnyOperationPending = (state, daoOperationPairs) => {
  return Object.entries(daoOperationPairs).some( nm => isOperationPending(state, nm[0], nm[1]));
};

export const isAnyLoading = (state, daoNames) => {
  return daoNames.some( dao => getDao(state, dao) == undefined) || daoNames.some( nm => isLoading(state, nm));
};

export const getNavigationProps = (props) => {
  return (props && props.location && props.location.state ? props.location.state : {});
};
