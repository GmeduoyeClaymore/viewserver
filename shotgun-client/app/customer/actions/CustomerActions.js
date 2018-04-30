import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderDao from 'common/dao/OrderDao';
import CustomerDao from 'customer/dao/CustomerDao';
import PaymentDao from 'common/dao/PaymentDao';
import DeliveryAddressDao from 'customer/dao/DeliveryAddressDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import ProductDao from 'common/dao/ProductDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client));
    const orderDao = registerNakedDao(dispatch, new OrderDao(client));
    registerNakedDao(dispatch, new PaymentDao(client));
    register(dispatch, new DeliveryAddressDao(client));
    register(dispatch, new OrderSummaryDao(client));
    register(dispatch, new OrderSummaryDao(client, undefined, 'singleOrderSummaryDao'));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new ProductDao(client));
    register(dispatch, new ProductCategoryDao(client));
    register(dispatch, new UserRelationshipDao(client));
    registerNakedDao(dispatch, new CustomerDao(client, orderDao), undefined, continueWith);
  };
};

export const checkout = (orderItem, payment, delivery, product, continueWith) => {
  return invokeDaoCommand('customerDao', 'checkout', {orderItem, payment, delivery, product}, continueWith);
};

export const registerCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('loginDao', 'registerAndLoginCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const updateOrderPrice = (orderId, price, continueWith) => {
  return invokeDaoCommand('customerDao', 'updateOrderPrice', {orderId, price}, continueWith);
};

export const updateCustomer = (customer, continueWith) => {
  return invokeDaoCommand('customerDao', 'updateCustomer', {customer}, continueWith);
};

export const loginCustomer = (email, password, continueWith) => {
  return invokeDaoCommand('customerDao', 'loginCustomer', {email, password}, continueWith);
};

export const getPaymentCards = (continueWith) => {
  return invokeDaoCommand('paymentDao', 'getCustomerPaymentCards', continueWith);
};

export const cancelOrder = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'cancelOrder', {orderId}, continueWith);
};

export const rejectDriver = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'rejectDriver', {orderId}, continueWith);
};

export const customerCompleteOrder = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'customerCompleteOrder', {orderId}, continueWith);
};

export const callDriver = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'callDriver', {orderId}, continueWith);
};

export const deletePaymentCard = (cardId, continueWith) => {
  return invokeDaoCommand('paymentDao', 'deletePaymentCard', {cardId}, continueWith);
};

export const addPaymentCard = (paymentCard, continueWith) => {
  return invokeDaoCommand('paymentDao', 'addPaymentCard', {paymentCard}, continueWith);
};

export const rateDriver = (orderId, rating, continueWith) => {
  return invokeDaoCommand('customerDao', 'rateDriver', {orderId, rating}, continueWith);
};
