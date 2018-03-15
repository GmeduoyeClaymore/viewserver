import {getAllDaos, invokeDaoCommand, unregisterDao, registerDao, updateSubscriptionAction, updateOptionsAction} from 'common/dao';
import Dao from 'common/dao/DaoBase';
import UserDao from 'common/dao/UserDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';

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

export const updateRange = (range) => {
  return invokeDaoCommand('userDao', 'updateRange', {range});
};

export const updateStatus = ({status, statusMessage}) => {
  return invokeDaoCommand('userDao', 'updateStatus', {status, statusMessage});
};

export const updateRelationship = (options, continueWith) => {
  return invokeDaoCommand('userRelationshipDao', 'updateRelationship', options, continueWith);
};

export const callUser = (userId, continueWith) => {
  return invokeDaoCommand('customerDao', 'callUser', {userId}, continueWith);
};

export const commonServicesRegistrationAction = (client) => {
  return (dispatch) => {
    register(dispatch, new UserDao(client));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new ProductCategoryDao(client));
  };
};
