import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderDao from 'common/dao/OrderDao';
import CustomerDao from 'customer/dao/CustomerDao';
import PaymentDao from 'common/dao/PaymentDao';
import DeliveryAddressDao from 'customer/dao/DeliveryAddressDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import VehicleTypeDao from 'common/dao/VehicleTypeDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import ProductDao from 'common/dao/ProductDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client), {userId});
    const orderDao = registerNakedDao(dispatch, new OrderDao(client), {userId});
    registerNakedDao(dispatch, new PaymentDao(client), {userId});
    register(dispatch, new DeliveryAddressDao(client), {userId});
    register(dispatch, new OrderSummaryDao(client), {userId});
    register(dispatch, new VehicleTypeDao(client), {userId});
    register(dispatch, new ContentTypeDao(client), {userId});
    register(dispatch, new ProductDao(client), {userId});
    register(dispatch, new ProductCategoryDao(client), {userId});
    registerNakedDao(dispatch, new CustomerDao(client, orderDao), {userId}, continueWith);
  };
};

export const checkout = (orderItem, payment, delivery, product, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {orderItem, payment, delivery, product}, continueWith);
};

export const registerCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('customerDao', 'registerCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const updateCustomer = (customer, continueWith) => {
  return invokeDaoCommand('customerDao', 'updateCustomer', {customer}, continueWith);
};

export const loginCustomer = (email, password, continueWith) => {
  return invokeDaoCommand('customerDao', 'loginCustomer', {email, password}, continueWith);
};

export const getPaymentCards = (customerToken, continueWith) => {
  return invokeDaoCommand('paymentDao', 'getCustomerPaymentCards', customerToken, continueWith);
};

export const cancelOrder = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'cancelOrder', {orderId}, continueWith);
};

export const rejectDriver = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'rejectDriver', {orderId}, continueWith);
};

export const callDriver = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'callDriver', {orderId}, continueWith);
};

export const deletePaymentCard = (customerToken, cardId, continueWith) => {
  return invokeDaoCommand('paymentDao', 'deletePaymentCard', {customerToken, cardId}, continueWith);
};

export const addPaymentCard = (customerToken, paymentCard, continueWith) => {
  return invokeDaoCommand('paymentDao', 'addPaymentCard', {customerToken, paymentCard}, continueWith);
};

export const rateDriver = (deliveryId, rating, continueWith) => {
  return invokeDaoCommand('customerDao', 'rateDriver', {deliveryId, rating}, continueWith);
};
