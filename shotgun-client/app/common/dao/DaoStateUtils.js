export const getDaoCommandStatus = (state, commandName, daoName) => {
    return state.getIn(['dao', daoName, 'commands', commandName, 'status']);
};
export const getDaoCommandResult = (state, commandName, daoName) => {
    return state.getIn(['dao', daoName, 'commands', commandName, 'message']);
};
export const getDaoState = (state, path, daoName) => {
    return state.getIn(['dao', daoName, ...path]);
};

export const isPaging = (state, daoName) => {
    const status = getDaoCommandStatus(state, 'updateSubscription', daoName);
    return status === null || status === 'start';
};
