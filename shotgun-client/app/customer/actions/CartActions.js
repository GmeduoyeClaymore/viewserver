import {invokeDaoCommand, getDaoOptions} from 'common/dao';

export const addItemToCartAction = ({quantity, productId}, continueWith) => {
    return async (dispatch, getState) => {
        const existingOptions = getDaoOptions(getState(), 'customerDao');
        dispatch(invokeDaoCommand('cartItemsDao', 'addItemToCart', {quantity, productId, customerId: existingOptions.customerId }, continueWith));
    };
};

export const purchaseCartItemsAction = (orderId, continueWith) => {
    return invokeDaoCommand('cartItemsDao', 'purchaseCartItems', orderId, continueWith);
};
