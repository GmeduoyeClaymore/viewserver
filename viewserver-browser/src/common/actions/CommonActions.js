import {unregisterDao, registerDao, updateSubscriptionAction, invokeDaoCommand} from 'common/dao';
import Dao from 'common/dao/DaoBase';
import LoginDao from 'common/dao/LoginDao';
import {getAllDaos} from 'common/dao';

export const register = (dispatch, daoContext, options, continueWith) => {
  const dao = new Dao(daoContext);
  dispatch(registerDao(dao));
  dispatch(updateSubscriptionAction(dao.name, options, continueWith));
  return dao;
};


export const login = (options, continueWith) => {
  return async (dispatch, getState) => {
    const dao = new LoginDao();
    dispatch(registerDao(dao));
    dispatch(invokeDaoCommand('loginDao', 'login', options, continueWith));
    return dao;
  };
};

export const unregisterAllDaos = () => {
  return async (dispatch, getState) => {
    const daos = Object.keys(getAllDaos(getState()).asMutable());
    daos.forEach(async dao => await dispatch(unregisterDao(dao)));
    return;
  };
};

