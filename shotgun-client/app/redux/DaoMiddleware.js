
import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, INVOKE_DAO_COMMAND} from 'common/dao/ActionConstants';
import Logger from 'common/Logger';
import {SubscribeWithSensibleErrorHandling} from 'common/rx';
const listMethodNames = (object, downToClass = Object) => {
    // based on code by Muhammad Umer, https://stackoverflow.com/a/31055217/441899
    let props = [];
    for (let obj = object; obj !== null && obj !== downToClass.prototype; obj = Object.getPrototypeOf(obj)){
        props = props.concat(Object.getOwnPropertyNames(obj));
    }
    return props.sort().filter((e, i, arr) => e != arr[ i + 1] && typeof object[e] == 'function');
};

export default DaoMiddleware = ({ getState, dispatch }) => {
    //TODO - find a better way of passing dispatch to the dao objects

    const asyncDispatch = (action, promiseOrFactory) => {
        const path = [action.daoName, 'commands', action.method];
		dispatch({ type: UPDATE_STATE, path, data: {status: 'start', message: undefined} });
		//	if supplied with a function then wrapped in Promise to get built in exception handling
		const promise = typeof promiseOrFactory === 'function' ?
			new Promise((resolve, reject) => promiseOrFactory(action.payload).then(resolve, reject)) :
			promiseOrFactory;
		return promise
			.then(result => {
				dispatch({ type: UPDATE_STATE, path, data: {status: 'success', message: result} });
				return result;
			}, error => {
				dispatch({ type: UPDATE_STATE, path, data: {status: 'fail', message: error.message ? error.message : error} });
			});
	};

    const DAO_SUBSCRIPTIONS = {};
    const DAOS = {};

    const registerDao = ({name, dao}) => {
        if (DAOS[name]){
            throw new Error(`A DAO with name ${name} has already been registered`);
        }
        DAOS[name] = dao;
        const sub = SubscribeWithSensibleErrorHandling(dao.observable, c => dispatch({type: UPDATE_STATE, path: [name], data: c}));
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

    const invokeCommand = (action) => {
        const dao = DAOS[action.daoName];
        Logger.info(`Invoking command ${JSON.stringify(action)}`);
        if (!dao){
            throw new Error(`Unable to find DAO named ${action.daoName}`);
        }
        const method = dao[action.method];
        if (!method){
            throw new Error(`Unable to find method named ${action.method} on ${action.daoName} method names are ${listMethodNames(dao).join(',')}`);
        }
        asyncDispatch(action, method);
        return getState();
    };

    const _handlers = {
        [REGISTER_DAO_ACTION]: registerDao,
        [UNREGISTER_DAO_ACTION]: unRegisterDao,
        [INVOKE_DAO_COMMAND]: invokeCommand
    };

    return next => action => {
        const handler = _handlers[action.type];
        if (handler){
            handler(action);
        }
        return next(action);
    };
};
