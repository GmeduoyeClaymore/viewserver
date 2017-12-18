import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderItemsDao from 'customer/data/OrderItemsDao';
import OrderDao from 'customer/data/OrderDao';
import CustomerDao from 'customer/data/CustomerDao';
import PaymentDao from 'customer/data/PaymentDao';
import DeliveryAddressDao from 'customer/data/DeliveryAddressDao';
import OrderSummaryDao from 'customer/data/OrderSummaryDao';
import DeliveryDao from 'customer/data/DeliveryDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'customerDao'])){
      const userDao = register(dispatch, new UserDao(client), {userId});
      const orderDao = register(dispatch, new OrderDao(client), {userId});
      const orderItemsDao = register(dispatch, new OrderItemsDao(client), {userId});
      const paymentDao = registerNakedDao(dispatch, new PaymentDao(client), {userId});
      const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {userId});
      const deliveryDao = register(dispatch, new DeliveryDao(client), {userId});
      register(dispatch, new OrderSummaryDao(client), {userId});
      register(dispatch, new CustomerDao(client, userDao, paymentDao, deliveryAddressDao, deliveryDao, orderDao, orderItemsDao), {userId}, continueWith);
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
      const userDao = register(dispatch, new UserDao(client), {userId});
      const paymentsDao = registerNakedDao(dispatch, new PaymentDao(client), {userId});
      const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {userId});
      register(dispatch, new CustomerDao(client, userDao, paymentsDao, deliveryAddressDao), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

export const checkout = (orderItem, payment, delivery, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {orderItem, payment, delivery}, continueWith);
};

export const addCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('customerDao', 'addCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const getPaymentCards = (customerToken, continueWith) => {
  return invokeDaoCommand('paymentDao', 'getCustomerPaymentCards', customerToken, continueWith);
};
