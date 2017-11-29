import {unregisterDao, registerDao, updateSubscriptionAction} from 'common/dao';
import Dao from 'common/dao/DaoBase';
import UserDao from 'common/dao/UserDao';
import VehicleTypeDao from 'common/dao/VehicleTypeDao';
import {getAllDaos} from 'common/dao';

export const register = (dispatch, daoContext, options, continueWith) => {
  const dao = new Dao(daoContext);
  dispatch(registerDao(dao));
  dispatch(updateSubscriptionAction(dao.name, options, continueWith));
  return dao;
};

export const unregisterAllDaos = () => {
  return async (dispatch, getState) => {
    const daos = Object.keys(getAllDaos(getState()).asMutable());
    daos.forEach(async dao => await dispatch(unregisterDao(dao)));
    return;
  };
};


export const commonServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client), {userId});
    register(dispatch, new VehicleTypeDao(client), {userId}, continueWith);
  };
};
