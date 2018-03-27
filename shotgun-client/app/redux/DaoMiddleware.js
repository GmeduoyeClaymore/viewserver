
import {REGISTER_DAO_ACTION, UNREGISTER_DAO_ACTION, UPDATE_STATE, UPDATE_OPTIONS, UPDATE_COMMAND_STATUS, INVOKE_DAO_COMMAND} from 'common/dao/ActionConstants';
import Logger from 'common/Logger';
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
  const DAO_COUNT_SUBSCRIPTIONS = {};
  const DAOS = {};
  const DAO_SNAPSHOT_COMPLETE_SUBSCRIPTIONS = {};

  const registerDao = ({name, dao}) => {
    if (DAOS[name]){
      //TODO - I think re-registering is fine...
      //Yep it's fine just as long as you unregister the old one first :)
      unRegisterDao({name});
    }
    DAOS[name] = dao;
    
    if (dao.observable){
      let daoEventFunc = c => {
        dispatch({type: UPDATE_STATE(name), path: [name, 'data'], data: c});
      };
      daoEventFunc = daoEventFunc.bind(dao);
      const sub = dao.observable.subscribe(daoEventFunc);
      DAO_SUBSCRIPTIONS[name] = sub;
    }

    if (dao.optionsObservable){
      let daoOptionFunc = c => {
        dispatch({type: UPDATE_OPTIONS(name), path: [name, 'options'], data: c});
      };
      daoOptionFunc = daoOptionFunc.bind(dao);
      const optionsSub = dao.optionsObservable.subscribe(daoOptionFunc);
      DAO_OPTIONS_SUBSCRIPTIONS[name] = optionsSub;
    }

    if (dao.countObservable){
      const daoCountFunc = c => {
        dispatch({type: UPDATE_STATE(name), path: [name, 'size'], data: c});
      };
      const countSub = dao.countObservable.subscribe(daoCountFunc);
      DAO_COUNT_SUBSCRIPTIONS[name] = countSub;
    }

    if (dao.snapshotCompleteObservable){
      const daoSnapshotComplete = c => {
        dispatch({type: UPDATE_STATE(name), path: [name, 'snapshotComplete'], data: c});
      };
      const snapshotCompleteSub = dao.snapshotCompleteObservable.subscribe(daoSnapshotComplete);
      DAO_SNAPSHOT_COMPLETE_SUBSCRIPTIONS[name] = snapshotCompleteSub;
    }


    return getState();
  };

  const unsub = (name, subs) => {
    if (subs[name]){
      subs[name].unsubscribe();
      subs[name] = undefined;
    }
  };

  const unRegisterDao = ({name}) => {
    if (!DAOS[name]){
      return;
    }
    unsub(name, DAO_SUBSCRIPTIONS);
    unsub(name, DAO_OPTIONS_SUBSCRIPTIONS);
    unsub(name, DAO_COUNT_SUBSCRIPTIONS);
    unsub(name, DAO_SNAPSHOT_COMPLETE_SUBSCRIPTIONS);
    DAOS[name] = undefined;

    dispatch({type: UPDATE_STATE(name), path: [name], data: undefined});
    return getState();
  };

  const invokeCommand = (action) => {
    const dao = DAOS[action.daoName];
    Logger.debug(`Invoking command ${action.type}`);
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
