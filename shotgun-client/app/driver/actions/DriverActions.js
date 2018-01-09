import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import UserDao from 'common/dao/UserDao';
import DriverDao from 'driver/dao/DriverDao';
import VehicleDao from 'driver/dao/VehicleDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import OrderRequestDao from 'driver/dao/OrderRequestDao';

export const driverServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'driverDao'])){
      register(dispatch, new UserDao(client), {userId});
      register(dispatch, new VehicleDao(client), {userId});
      register(dispatch, new OrderRequestDao(client));
      register(dispatch, new OrderSummaryDao(client), {userId});
      registerNakedDao(dispatch, new DriverDao(client), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};
  
export const registerDriver = (driver, vehicle, continueWith) => {
  return invokeDaoCommand('driverDao', 'registerDriver', {driver, vehicle}, continueWith);
};

export const acceptOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('driverDao', 'acceptOrderRequest', {orderId}, continueWith);
};

export const startOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('driverDao', 'startOrderRequest', {orderId}, continueWith);
};

export const cancelOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('driverDao', 'cancelOrderRequest', {orderId}, continueWith);
};
