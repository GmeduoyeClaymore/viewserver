
import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, UPDATE_COMMAND_STATUS, INVOKE_DAO_COMMAND} from 'common/dao/ActionConstants';
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
		dispatch({ type: UPDATE_COMMAND_STATUS(action.daoName, action.method), path, data: {status: 'start', message: undefined} });
		//	if supplied with a function then wrapped in Promise to get built in exception handling
		const promise = typeof promiseOrFactory === 'function' ?
			new Promise((resolve, reject) => promiseOrFactory(action.payload).then(resolve, reject)) :
			promiseOrFactory;
		return promise
			.then(result => {
                dispatch({ type: UPDATE_COMMAND_STATUS(action.daoName, action.method), path, data: {status: 'success', message: result} });
                if (action.continueWith && typeof action.continueWith === 'function'){
                    try {
                        action.continueWith();
                    } catch (error){
                        dispatch({ type: UPDATE_COMMAND_STATUS(action.daoName, action.method), path, data: {status: 'fail', message: action.daoName + '/' + action.method + ' ' +  (error.message ? error.message : error)} });
                    }
                }
				return result;
			}, error => {
				dispatch({ type: UPDATE_COMMAND_STATUS(action.daoName, action.method), path, data: {status: 'fail', message: action.daoName + '/' + action.method + ' ' +  (error.message ? error.message : error)} });
			});
	};

    const DAO_SUBSCRIPTIONS = {};
    const DAO_OPTIONS_SUBSCRIPTIONS = {};
    const DAOS = {};

    const registerDao = ({name, dao}) => {
        if (DAOS[name]){
            throw new Error(`A DAO with name ${name} has already been registered`);
        }
        DAOS[name] = dao;
        let daoEventFunc = c => {
            const _this = this;
            dispatch({type: UPDATE_STATE(_this.name), path: [_this.name, 'data'], data: c});
        };
        daoEventFunc = daoEventFunc.bind(dao);
        const sub = SubscribeWithSensibleErrorHandling(dao.observable, daoEventFunc);
        let daoOptionFunc = c => {
            const _this = this;
            dispatch({type: UPDATE_STATE(_this.name), path: [_this.name, 'options'], data: c});
        };
        daoOptionFunc = daoOptionFunc.bind(dao);
        const optionsSub = SubscribeWithSensibleErrorHandling(dao.optionsObservable, daoOptionFunc);
        DAO_SUBSCRIPTIONS[name] = sub;
        DAO_OPTIONS_SUBSCRIPTIONS[name] = optionsSub;
        return getState();
    };

    const unRegisterDao = ({name}) => {
        if (!DAOS[name]){
            return;
        }
        DAO_SUBSCRIPTIONS[name].unsubscribe();
        DAOS[name] = undefined;
        DAO_SUBSCRIPTIONS[name] = undefined;
        DAO_OPTIONS_SUBSCRIPTIONS[name] = undefined;
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
        [REGISTER_DAO_ACTION()]: registerDao,
        [UNREGISTER_DAO_ACTION()]: unRegisterDao,
        [INVOKE_DAO_COMMAND()]: invokeCommand
    };

    return next => action => {
        const handlerName = Object.keys(_handlers).find(h => action.type.startsWith(h));
        if (handlerName){
            const handler = _handlers[handlerName];
            handler(action);
        }
        return next(action);
    };
};
