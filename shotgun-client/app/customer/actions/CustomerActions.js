import {invokeDaoCommand, getDaoOptions, registerDao, updateSubscriptionAction} from 'common/dao';
import OrderItemsDao from 'customer/data/OrderItemsDao';
import CartItemsDao from 'customer/data/CartItemsDao';
import CartSummaryDao from 'customer/data/CartSummaryDao';
import OrderDao from 'customer/data/OrderDao';
import CustomerDao from 'customer/data/CustomerDao';
import PaymentCardsDao from 'customer/data/PaymentCardsDao';
import DeliveryAddressDao from 'customer/data/DeliveryAddressDao';
import ProductCategoryDao from 'customer/data/ProductCategoryDao';
import DeliveryDao from 'customer/data/DeliveryDao';
import ProductDao from 'customer/data/ProductDao';
import Dao from 'customer/data/DaoBase';

const  register = (dispatch, daoContext, options, continueWith) => {
    const dao = new Dao(daoContext);
    dispatch(registerDao(dao));
    dispatch(updateSubscriptionAction(dao.name, options, continueWith));
    return dao;
};

export const addItemToCartAction = ({quantity, productId}, continueWith) => {
    return async (dispatch, getState) => {
        const existingOptions = getDaoOptions(getState(), 'customerDao');
        dispatch(invokeDaoCommand('cartItemsDao', 'addItemToCart', {quantity, productId, customerId: existingOptions.customerId }, continueWith));
    };
};

export const customerServicesRegistrationAction = (client, customerId, continueWith) => {
    return async (dispatch, getState) => {
        const state = getState();
        if (!state.getIn(['dao', 'customerDao'])){
            register(dispatch, new ProductCategoryDao(client), {customerId});
            register(dispatch, new OrderItemsDao(client), {customerId});
            const orderDao = register(dispatch, new OrderDao(client), {customerId});
            const paymentCardsDao = register(dispatch, new PaymentCardsDao(client), {customerId});
            const deliveryAddressDao = register(dispatch, new DeliveryAddressDao(client), {customerId});
            register(dispatch, new CustomerDao(client, paymentCardsDao, deliveryAddressDao), {customerId}, continueWith);
            const deliveryDao = register(dispatch, new DeliveryDao(client), {customerId});
            register(dispatch, new CartSummaryDao(client), {customerId});
            register(dispatch, new ProductDao(client));
            register(dispatch, new CartItemsDao(client, orderDao, deliveryDao), {customerId});
        }
        continueWith();
    };
};

export const purchaseCartItemsAction = (eta, paymentId, deliveryAddressId) => {
    return invokeDaoCommand('cartItemsDao', 'purchaseCartItems', {eta, paymentId, deliveryAddressId}, continueWith);
};

export const addOrUpdateCustomer = (customer, paymentCard, deliveryAddress) => {
    return invokeDaoCommand('customerDao', 'addOrUpdateCustomer', {customer, paymentCard, deliveryAddress});
};