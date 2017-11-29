import {invokeDaoCommand, getDaoOptions} from 'common/dao';
import {register} from 'common/actions/CommonActions';
import OrderItemsDao from 'customer/data/OrderItemsDao';
import CartItemsDao from 'customer/data/CartItemsDao';
import CartSummaryDao from 'customer/data/CartSummaryDao';
import OrderDao from 'customer/data/OrderDao';
import CustomerDao from 'customer/data/CustomerDao';
import PaymentCardsDao from 'customer/data/PaymentCardsDao';
import DeliveryAddressDao from 'customer/data/DeliveryAddressDao';
import ProductCategoryDao from 'customer/data/ProductCategoryDao';
import OrderSummaryDao from 'customer/data/OrderSummaryDao';
import DeliveryDao from 'customer/data/DeliveryDao';
import ProductDao from 'common/dao/ProductDao';
import UserDao from 'common/dao/UserDao';

export const addItemToCartAction = ({quantity, productId}, continueWith) => {
  return async (dispatch, getState) => {
    const existingOptions = getDaoOptions(getState(), 'customerDao');
    dispatch(invokeDaoCommand('cartItemsDao', 'addItemToCart', {quantity, productId, userId: existingOptions.userId }, continueWith));
  };
};

export const updateCartItemQuantityAction = ({productId, quantity}, continueWith) => {
  return async dispatch => {
    dispatch(invokeDaoCommand('cartItemsDao', 'updateItemCartQuantity', {quantity, productId}, continueWith));
  };
};

export const customerServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'customerDao'])){
      register(dispatch, new ProductCategoryDao(client), {userId});
      register(dispatch, new OrderItemsDao(client), {userId});
      const userDao = register(dispatch, new UserDao(client), {userId});
      const orderDao = register(dispatch, new OrderDao(client), {userId});
      const paymentCardsDao = register(dispatch, new PaymentCardsDao(client), {userId});
      const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {userId});
      const deliveryDao = register(dispatch, new DeliveryDao(client), {userId});
      register(dispatch, new CartSummaryDao(client), {userId});
      register(dispatch, new ProductDao(client));
      register(dispatch, new OrderSummaryDao(client), {userId});
      register(dispatch, new CartItemsDao(client, orderDao, deliveryDao), {userId});
      register(dispatch, new CustomerDao(client, userDao, paymentCardsDao, deliveryAddressDao), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

export const purchaseCartItemsAction = (eta, paymentId, deliveryAddressId, deliveryType, continueWith) => {
  return invokeDaoCommand('cartItemsDao', 'purchaseCartItems', {eta, paymentId, deliveryAddressId, deliveryType}, continueWith);
};

export const addOrUpdateCustomer = (customer, paymentCard, deliveryAddress, continueWith) => {
  return invokeDaoCommand('customerDao', 'addOrUpdateCustomer', {customer, paymentCard, deliveryAddress}, continueWith);
};
