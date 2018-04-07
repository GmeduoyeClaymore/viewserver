import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import UserDao from 'common/dao/UserDao';
import DriverDao from 'driver/dao/DriverDao';
import VehicleDao from 'driver/dao/VehicleDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import OrderRequestDao from 'driver/dao/OrderRequestDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import PaymentDao from 'common/dao/PaymentDao';

export const driverServicesRegistrationAction = (client, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client));
    register(dispatch, new VehicleDao(client));
    register(dispatch, new OrderRequestDao(client));
    register(dispatch, new OrderSummaryDao(client));
    register(dispatch, new OrderSummaryDao(client, undefined, 'singleOrderSummaryDao'));
    register(dispatch, new UserRelationshipDao(client));
    registerNakedDao(dispatch, new PaymentDao(client));
    registerNakedDao(dispatch, new DriverDao(client), continueWith);
  };
};

export const registerAndLoginDriver = (driver, vehicle, address, bankAccount, continueWith) => {
  return invokeDaoCommand('loginDao', 'registerAndLoginDriver', {driver, vehicle, address, bankAccount}, continueWith);
};

export const updateDriver = (driver, continueWith) => {
  return invokeDaoCommand('driverDao', 'updateDriver', {driver}, continueWith);
};

export const updateVehicle = (vehicle, continueWith) => {
  return invokeDaoCommand('vehicleDao', 'addOrUpdateVehicle', {vehicle}, continueWith);
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

export const rateCustomer = (orderId, rating, continueWith) => {
  return invokeDaoCommand('driverDao', 'rateCustomer', {orderId, rating}, continueWith);
};

export const callCustomer = (orderId, continueWith) => {
  return invokeDaoCommand('driverDao', 'callCustomer', {orderId}, continueWith);
};

export const watchPosition = (continueWith) => {
  return invokeDaoCommand('userDao', 'watchPosition', {}, continueWith);
};

export const stopWatchingPosition = (continueWith) => {
  return invokeDaoCommand('userDao', 'stopWatchingPosition', {}, continueWith);
};

export const getBankAccount = (continueWith) => {
  return invokeDaoCommand('paymentDao', 'getBankAccount', {}, continueWith);
};

export const setBankAccount = (paymentBankAccount, continueWith) => {
  return invokeDaoCommand('paymentDao', 'setBankAccount', {paymentBankAccount}, continueWith);
};
