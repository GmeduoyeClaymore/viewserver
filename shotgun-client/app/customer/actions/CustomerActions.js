import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderDao from 'common/dao/OrderDao';
import CustomerDao from 'customer/dao/CustomerDao';
import PaymentDao from 'customer/dao/PaymentDao';
import DeliveryAddressDao from 'customer/dao/DeliveryAddressDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import VehicleTypeDao from 'common/dao/VehicleTypeDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client), {userId});
    const orderDao = registerNakedDao(dispatch, new OrderDao(client), {userId});
    registerNakedDao(dispatch, new PaymentDao(client), {userId});
    register(dispatch, new DeliveryAddressDao(client), {userId});
    register(dispatch, new OrderSummaryDao(client), {userId});
    register(dispatch, new VehicleTypeDao(client), {userId});
    registerNakedDao(dispatch, new CustomerDao(client, orderDao), {userId}, continueWith);
  };
};

export const checkout = (orderItem, payment, delivery, totalPrice, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {orderItem, payment, delivery, totalPrice}, continueWith);
};

export const registerCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('customerDao', 'registerCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const getPaymentCards = (customerToken, continueWith) => {
  return invokeDaoCommand('paymentDao', 'getCustomerPaymentCards', customerToken, continueWith);
};

export const rateDriver = (deliveryId, rating, continueWith) => {
  return invokeDaoCommand('customerDao', 'rateDriver', {deliveryId, rating}, continueWith);
};
