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

export const isLoading = (state, daoName) => {
    const status = getDaoCommandStatus(state, 'updateSubscription', daoName);
    return status === null || status === 'start';
};

export const getLoadingError = (state, daoName) => {
    const status = getDaoCommandStatus(state, 'updateSubscription', daoName);
    return status === 'fail' ? getDaoCommandResult(state, 'updateSubscription', daoName) : undefined;
};

export const getLoadingErrors = (state, daoNames) => {
    return daoNames.map(nm => getLoadingError(state, nm)).filter(err => err).join( '\n');
};

export const isAnyLoading = (state, daoNames) => {
    return daoNames.some( nm => isLoading(state, nm));
};
