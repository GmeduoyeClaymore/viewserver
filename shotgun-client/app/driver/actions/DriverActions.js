import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import UserDao from 'common/dao/UserDao';
import DriverDao from 'driver/dao/DriverDao';
import VehicleDao from 'driver/dao/VehicleDao';
import VehicleTypeDao from 'common/dao/VehicleTypeDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import OrderRequestDao from 'driver/dao/OrderRequestDao';

export const driverServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client), {userId});
    register(dispatch, new VehicleDao(client), {userId});
    register(dispatch, new VehicleTypeDao(client), {userId});
    register(dispatch, new OrderRequestDao(client));
    register(dispatch, new OrderSummaryDao(client), {userId});
    registerNakedDao(dispatch, new DriverDao(client), {userId}, continueWith);
  };
};
  
export const registerDriver = (driver, vehicle, address, bankAccount, continueWith) => {
  return invokeDaoCommand('driverDao', 'registerDriver', {driver, vehicle, address, bankAccount}, continueWith);
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

export const completeOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('driverDao', 'completeOrderRequest', {orderId}, continueWith);
};

export const rateCustomer = (deliveryId, rating, continueWith) => {
  return invokeDaoCommand('driverDao', 'rateCustomer', {deliveryId, rating}, continueWith);
};
