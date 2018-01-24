import {unregisterDao, registerDao, updateSubscriptionAction, updateOptionsAction} from 'common/dao';
import Dao from 'common/dao/DaoBase';
import UserDao from 'common/dao/UserDao';
import CustomerDao from 'customer/dao/CustomerDao';
import DriverDao from 'driver/dao/DriverDao';
import {getAllDaos, invokeDaoCommand} from 'common/dao';

export const register = (dispatch, daoContext, options, continueWith) => {
  const dao = new Dao(daoContext);
  dispatch(registerDao(dao));

  if (daoContext.subscribeOnCreate == undefined || daoContext.subscribeOnCreate == true) {
    dispatch(updateSubscriptionAction(dao.name, options, continueWith));
  } else {
    dispatch(updateOptionsAction(dao.name, options, continueWith));
  }
  return dao;
};

export const registerNakedDao = (dispatch, dao, options, continueWith) => {
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

export const getCurrentPosition = () => {
  return invokeDaoCommand('userDao', 'getCurrentPosition');
};

export const updateDeliveryAddress = (deliveryAddress, continueWith) => {
  return invokeDaoCommand('deliveryAddressDao', 'addOrUpdateDeliveryAddress', {deliveryAddress}, continueWith);
};

export const commonServicesRegistrationAction = (client, userId, continueWith) => {
  return (dispatch) => {
    register(dispatch, new UserDao(client), {userId});
    registerNakedDao(dispatch, new CustomerDao(client), {userId}, continueWith);
    registerNakedDao(dispatch, new DriverDao(client), {userId}, continueWith);
  };
};
