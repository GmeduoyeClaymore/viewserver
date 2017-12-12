import {invokeDaoCommand, getDaoOptions} from 'common/dao';
import {register} from 'common/actions/CommonActions';
import OrderItemsDao from 'customer/data/OrderItemsDao';
import OrderDao from 'customer/data/OrderDao';
import CustomerDao from 'customer/data/CustomerDao';
import PaymentCardsDao from 'customer/data/PaymentCardsDao';
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
      //const paymentCardsDao = register(dispatch, new PaymentCardsDao(client), {userId});
      const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {userId});
      const deliveryDao = register(dispatch, new DeliveryDao(client), {userId});
      //register(dispatch, new OrderSummaryDao(client), {userId});
      register(dispatch, new CustomerDao(client, userDao, undefined, deliveryAddressDao, deliveryDao, orderDao, orderItemsDao), {userId}, continueWith);
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
    //  const paymentCardsDao = register(dispatch, new PaymentCardsDao(client), {userId});
      const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {userId});
      register(dispatch, new CustomerDao(client, userDao, undefined, deliveryAddressDao), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

export const checkout = (order, payment, delivery, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {order, payment, delivery}, continueWith);
};

export const addOrUpdateCustomer = (customer, deliveryAddress, continueWith) => {
  return invokeDaoCommand('customerDao', 'addOrUpdateCustomer', {customer, deliveryAddress}, continueWith);
};
