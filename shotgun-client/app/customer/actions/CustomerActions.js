import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderDao from 'common/dao/OrderDao';
import CustomerDao from 'customer/data/CustomerDao';
import PaymentDao from 'customer/data/PaymentDao';
import DeliveryAddressDao from 'customer/data/DeliveryAddressDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'customerDao'])){
      register(dispatch, new UserDao(client), {userId});
      const orderDao = registerNakedDao(dispatch, new OrderDao(client), {userId});
      registerNakedDao(dispatch, new PaymentDao(client), {userId});
      register(dispatch, new DeliveryAddressDao(client), {userId});
      register(dispatch, new OrderSummaryDao(client), {userId});
      registerNakedDao(dispatch, new CustomerDao(client, orderDao), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

//Load the minimal set of services we need in order to register a customer.
export const loadCustomerRegistrationServices = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'userDao'])){
      register(dispatch, new UserDao(client), {userId});
      registerNakedDao(dispatch, new CustomerDao(client), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

export const checkout = (orderItem, payment, delivery, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {orderItem, payment, delivery}, continueWith);
};

export const registerCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('customerDao', 'registerCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const getPaymentCards = (customerToken, continueWith) => {
  return invokeDaoCommand('paymentDao', 'getCustomerPaymentCards', customerToken, continueWith);
};
