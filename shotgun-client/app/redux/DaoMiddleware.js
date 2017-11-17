
import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, INVOKE_DAO_COMMAND} from './ActionConstants';


export const DaoMiddleware = ({ getState, dispatch }) => {
    //TODO - find a better way of passing dispatch to the dao objects
    
    const asyncDispatch = (action, promiseOrFactory) => {
        const path = [action.daoName, action.method];
		dispatch({ type, UPDATE_STATE, path, data: {status: 'start', message: undefined} });
		//	if supplied with a function then wrapped in Promise to get built in exception handling
		const promise = typeof promiseOrFactory === 'function' ?
			new Promise((resolve, reject) => promiseOrFactory(action).then(resolve, reject)) :
			promiseOrFactory;
		return promise
			.then(result => {
				dispatch({ type, UPDATE_STATE, path, data: {status: 'success', message: result} });
				return result;
			}, error => {
				dispatch({ type, UPDATE_STATE, path, data: {status: 'fail', message: result} });
				return Promise.reject(error);
			});
	};

    const DAO_SUBSCRIPTIONS = {};
    const DAOS = {};

    const registerDao = ({name, dao}) => {
        if (DAOS[name]){
            throw new Exception(`A DAO with name ${name} has already been registered`);
        }
        DAOS[name] = dao;
        const sub = dao.observable.subject(c => dispatch({type: UPDATE_STATE, path: [name], data: c}));
        DAO_SUBSCRIPTIONS[name] = sub;
        return getState();
    };

    const unRegisterDao = ({name}) => {
        if (!DAOS[name]){
            return;
        }
        DAO_SUBSCRIPTIONS[name].unsubscribe();
        DAOS[name] = undefined;
        DAO_SUBSCRIPTIONS[name] = undefined;
        return getState();
    };

    const updateState = (action) => {
        return getState().setIn(action.path || [], action.data);
    };

    const invokeCommand = (action) => {
        const dao = DAOS[action.daoName];
        if (!dao){
            throw new Exception(`Unable to find DAO named ${action.daoName}`);
        }
        const method = dao[action.method];
        if (!method){
            throw new Exception(`Unable to find method named ${action.method} on ${action.daoName}`);
        }
        asyncDispatch(action.payload, method);
        return getState();
    };

    const _handlers = {
        [REGISTER_DAO_ACTION]: registerDao,
        [UNREGISTER_DAO_ACTION]: unRegisterDao,
        [UPDATE_STATE]: updateState,
        [INVOKE_DAO_COMMAND]: invokeCommand
    };

    return next => action => {
        const handler = _handlers[action.type];
        const result = handler ? handler(action) : next(action);
        return result;
    };
};
