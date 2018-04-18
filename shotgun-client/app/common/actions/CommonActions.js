import {getAllDaos, invokeDaoCommand, unregisterDao, registerDao, updateSubscriptionAction, updateOptionsAction} from 'common/dao';
import Dao from 'common/dao/DaoBase';
import UserDao from 'common/dao/UserDao';
import LoginDao from 'common/dao/LoginDao';
import Logger from 'common/Logger';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import {RESET_ALL_COMPONENT_STATE} from 'common/dao/ActionConstants';

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

export const registerNakedDao = (dispatch, dao) => {
  dispatch(registerDao(dao));
  return dao;
};

export const unregisterAllDaos = () => {
  return async (dispatch, getState) => {
    const daos = Object.keys(getAllDaos(getState()).asMutable());
    const daosToRemove = daos.filter(daoName => daoName !== 'loginDao');
    daosToRemove.forEach(async dao => await dispatch(unregisterDao(dao)));
    Logger.info(`Unregistering ${daosToRemove.join(',')}`);
    return;
  };
};

export const unregisterAllDaosAndResetComponentState = () => {
  return async (dispatch) => {
    dispatch(unregisterAllDaos());
    dispatch({type: RESET_ALL_COMPONENT_STATE});
  };
};

export const updateDeliveryAddress = (deliveryAddress, continueWith) => {
  return invokeDaoCommand('deliveryAddressDao', 'addOrUpdateDeliveryAddress', {deliveryAddress}, continueWith);
};

export const updateRange = (range) => {
  return invokeDaoCommand('userDao', 'updateRange', {range});
};

export const updateStatus = ({status, statusMessage}) => {
  return invokeDaoCommand('userDao', 'updateStatus', {status, statusMessage});
};

export const updateRelationship = (options, continueWith) => {
  return invokeDaoCommand('userRelationshipDao', 'updateRelationship', options, continueWith);
};

export const loginUserByUsernameAndPassword = (options, continueWith) => {
  return invokeDaoCommand('loginDao', 'loginUserByUsernameAndPassword', options, continueWith);
};

export const logOut = (continueWith) => {
  return invokeDaoCommand('loginDao', 'logOut', undefined, continueWith);
};

export const callUser = (userId, continueWith) => {
  return invokeDaoCommand('userRelationshipDao', 'callUser', {userId}, continueWith);
};

export const commonServicesRegistrationAction = (client) => {
  return (dispatch) => {
    register(dispatch, new UserDao(client));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new ProductCategoryDao(client));
  };
};

export const loginServicesRegistrationAction = (client) => {
  return (dispatch) => {
    registerNakedDao(dispatch, new LoginDao(client));
  };
};
