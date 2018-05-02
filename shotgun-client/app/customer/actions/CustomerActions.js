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
  return invokeDaoCommand('paymentDao', 'getPaymentCards', continueWith);
};


export const callPartner = (orderId, continueWith) => {
  return invokeDaoCommand('customerDao', 'callPartner', {orderId}, continueWith);
};

export const deletePaymentCard = (cardId, continueWith) => {
  return invokeDaoCommand('paymentDao', 'deletePaymentCard', {cardId}, continueWith);
};

export const addPaymentCard = (paymentCard, continueWith) => {
  return invokeDaoCommand('paymentDao', 'addPaymentCard', {paymentCard}, continueWith);
};

export const ratePartner = (orderId, rating, continueWith) => {
  return invokeDaoCommand('customerDao', 'ratePartner', {orderId, rating}, continueWith);
};

export const checkout = (order, payment, continueWith) => {
  return invokeDaoCommand('orderDao', 'createOrder', {order, payment}, continueWith);
};

export const addPaymentStage = (orderId, continueWith) => {
  return invokeDaoCommand('orderDao', 'addPaymentStage', {orderId}, continueWith);
};

export const cancelOrder = (orderId, continueWith) => {
  return invokeDaoCommand('orderDao', 'cancelOrder', {orderId}, continueWith);
};

export const rejectPartner = (orderId, partnerId, continueWith) => {
  return invokeDaoCommand('orderDao', 'rejectPartner', {orderId, partnerId}, continueWith);
};

export const acceptPartner = (orderId, partnerId, continueWith) => {
  return invokeDaoCommand('orderDao', 'acceptPartner', {orderId, partnerId}, continueWith);
};
