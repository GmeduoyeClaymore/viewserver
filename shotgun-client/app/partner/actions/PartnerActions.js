import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import UserDao from 'common/dao/UserDao';
import PartnerDao from 'partner/dao/PartnerDao';
import PartnerOrderResponseDao from 'partner/dao/PartnerOrderResponseDao';
import VehicleDao from 'partner/dao/VehicleDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import OrderRequestDao from 'partner/dao/OrderRequestDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import PaymentDao from 'common/dao/PaymentDao';

export const partnerServicesRegistrationAction = (client, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client));
    register(dispatch, new VehicleDao(client));
    register(dispatch, new OrderRequestDao(client));
    register(dispatch, new OrderSummaryDao(client));
    register(dispatch, new PartnerOrderResponseDao(client));
    register(dispatch, new OrderSummaryDao(client, undefined, 'singleOrderSummaryDao'));
    register(dispatch, new UserRelationshipDao(client));
    registerNakedDao(dispatch, new PaymentDao(client));
    registerNakedDao(dispatch, new PartnerDao(client), continueWith);
  };
};

export const registerAndLoginPartner = (partner, vehicle, address, bankAccount, continueWith) => {
  return invokeDaoCommand('loginDao', 'registerAndLoginPartner', {partner, vehicle, address, bankAccount}, continueWith);
};

export const updatePartner = (partner, continueWith) => {
  return invokeDaoCommand('partnerDao', 'updatePartner', {partner}, continueWith);
};

export const getVehicleDetails = (registrationNumber, continueWith) => {
  return invokeDaoCommand('partnerDao', 'getVehicleDetails', {registrationNumber}, continueWith);
};

export const updateVehicle = (vehicle, continueWith) => {
  return invokeDaoCommand('vehicleDao', 'addOrUpdateVehicle', {vehicle}, continueWith);
};

export const respondToOrder = (orderId, orderContentTypeId, requiredDate, amount, continueWith) => {
  return invokeDaoCommand('orderDao', 'respondToOrder', {orderId, orderContentTypeId, requiredDate, amount}, continueWith);
};

export const acceptOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('partnerDao', 'acceptOrderRequest', {orderId}, continueWith);
};

export const startOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('partnerDao', 'startOrderRequest', {orderId}, continueWith);
};

export const cancelOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('partnerDao', 'cancelOrderRequest', {orderId}, continueWith);
};

export const completeOrderRequest = (orderId, continueWith) => {
  return invokeDaoCommand('partnerDao', 'completeOrderRequest', {orderId}, continueWith);
};

export const rateCustomer = (orderId, rating, continueWith) => {
  return invokeDaoCommand('partnerDao', 'rateCustomer', {orderId, rating}, continueWith);
};

export const callCustomer = (orderId, continueWith) => {
  return invokeDaoCommand('partnerDao', 'callCustomer', {orderId}, continueWith);
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

export const setBankAccount = (paymentBankAccount, address, continueWith) => {
  return invokeDaoCommand('paymentDao', 'setBankAccount', {paymentBankAccount, address}, continueWith);
};
