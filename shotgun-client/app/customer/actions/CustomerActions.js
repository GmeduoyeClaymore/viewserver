import {invokeDaoCommand} from 'common/dao';
import {register, registerNakedDao} from 'common/actions/CommonActions';
import OrderDao from 'common/dao/OrderDao';
import CustomerDao from 'customer/dao/CustomerDao';
import PaymentDao from 'common/dao/PaymentDao';
import DeliveryAddressDao from 'customer/dao/DeliveryAddressDao';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import NotificationsDao from 'common/dao/NotificationsDao';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import ProductDao from 'common/dao/ProductDao';
import UserDao from 'common/dao/UserDao';

export const customerServicesRegistrationAction = (client, continueWith) => {
  return async (dispatch) => {
    register(dispatch, new UserDao(client));
    register(dispatch, new UserDao(client, 'singleUserDao'));
    const orderDao = registerNakedDao(dispatch, new OrderDao(client));
    registerNakedDao(dispatch, new PaymentDao(client));
    register(dispatch, new DeliveryAddressDao(client));
    register(dispatch, new OrderSummaryDao(client));
    register(dispatch, new OrderSummaryDao(client, undefined, 'singleOrderSummaryDao'));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new ProductDao(client));
    register(dispatch, new NotificationsDao(client));
    register(dispatch, new ProductCategoryDao(client));
    register(dispatch, new UserRelationshipDao(client));
    registerNakedDao(dispatch, new CustomerDao(client, orderDao), undefined, continueWith);
  };
};

export const registerCustomer = (customer, deliveryAddress, paymentCard, continueWith) => {
  return invokeDaoCommand('loginDao', 'registerAndLoginCustomer', {customer, deliveryAddress, paymentCard}, continueWith);
};

export const updateCustomer = (customer, continueWith) => {
  return invokeDaoCommand('customerDao', 'updateCustomer', {customer}, continueWith);
};

export const loginCustomer = (email, password, continueWith) => {
  return invokeDaoCommand('customerDao', 'loginCustomer', {email, password}, continueWith);
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

export const addPaymentStage = ({orderId, orderContentTypeId, amount, name, description, paymentStageType}, continueWith) => {
  return invokeDaoCommand('orderDao', 'addPaymentStage', {orderId, orderContentTypeId, amount, name, description, paymentStageType}, continueWith);
};

export const removePaymentStage = ({orderId, paymentStageId, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'removePaymentStage', {orderId, paymentStageId, orderContentTypeId}, continueWith);
};

export const payForPaymentStage = ({orderId, paymentStageId, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'payForPaymentStage', {orderId, paymentStageId, orderContentTypeId}, continueWith);
};

export const cancelOrder = ({orderId, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'cancelOrder', {orderId, orderContentTypeId}, continueWith);
};

export const rejectResponse = ({orderId, partnerId, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'rejectResponse', {orderId, partnerId, orderContentTypeId}, continueWith);
};

export const acceptResponse = ({orderId, partnerId, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'acceptResponse', {orderId, partnerId, orderContentTypeId}, continueWith);
};

export const updateOrderAmount = ({orderId, amount, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'updateOrderAmount', {orderId, amount, orderContentTypeId}, continueWith);
};

export const updateOrderVisibility = ({orderId, justForFriends, orderContentTypeId}, continueWith) => {
  return invokeDaoCommand('orderDao', 'updateOrderVisibility', {orderId, justForFriends, orderContentTypeId}, continueWith);
};

export const offHireItem = (orderId, orderContentTypeId, continueWith) => {
  return invokeDaoCommand('orderDao', 'offHireItem', {orderId, orderContentTypeId}, continueWith);
};

export const customerCompleteAndPay = (orderId, orderContentTypeId, continueWith) => {
  return invokeDaoCommand('orderDao', 'customerCompleteAndPay', {orderId, orderContentTypeId}, continueWith);
};

export const calculatePriceEstimate = (order, orderContentTypeId, continueWith) => {
  return invokeDaoCommand('orderDao', 'calculatePriceEstimate', {order, orderContentTypeId}, continueWith);
};

export const cancelResponseCustomer = (orderId, orderContentTypeId, partnerId, continueWith) => {
  return invokeDaoCommand('orderDao', 'cancelResponseCustomer', {orderId, orderContentTypeId, partnerId}, continueWith);
};
