import React from 'react';
import PropTypes from 'prop-types';
import * as constants from '../../redux/ActionConstants';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import {Spinner} from 'native-base';
import ActionButton from '../../common/components/ActionButton';
import icon from '../../common/assets/truck-fast.png';

const OrderConfirmation = ({cart, order, delivery, customer, status, dispatch, navigation, screenProps}) => {
    const purchase = async() => {
      dispatch({type: constants.UPDATE_STATUS, status: {busy: true}});
      const deliveryId = await dispatch(screenProps.customerService.deliveryDao.createDelivery());

      dispatch({type: constants.UPDATE_ORDER, order: {deliveryId}});
      const orderId = await dispatch(screenProps.customerService.orderDao.createOrder());
      await screenProps.customerService.cartItemsDao.purchaseCartItems(orderId);

      dispatch({type: constants.UPDATE_STATUS, status: {busy: false}});

      navigation.navigate('OrderComplete');
    };

    const deliveryAddress = customer.deliveryAddresses.find(a => a.deliveryAddressId == delivery.deliveryAddressId);
    const paymentCard = customer.paymentCards.find(a => a.paymentId == order.paymentId);

    const renderCartItem = (item) => {
      return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
        <Text>{`Quantity: ${item.quantity}`}</Text>
        <Text>{`Price: ${item.price}`}</Text>
        <Text>{`Total: ${item.totalPrice}`}</Text>
      </View>;
    };

    return <View style={{flex: 1, flexDirection: 'column'}}>
      {cart.items.map(c => renderCartItem(c))}
      <Text>{`Total Items ${cart.totalQuantity}`}</Text>
      <Text>{`Total Price ${cart.totalPrice}`}</Text>
      <Text>Payment {paymentCard.cardNumber}</Text>
      <Text>Delivery Details {deliveryAddress.line1}</Text>
      <Text>Delivery Requested in {delivery.eta} hours</Text>
      {!status.busy ? <ActionButton buttonText="Place Order" icon={icon} action={purchase}/> :  <Spinner />}
    </View>;
};

OrderConfirmation.PropTypes = {
  status: PropTypes.object,
  cart: PropTypes.object,
  order: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

OrderConfirmation.navigationOptions = {header: null};

const mapStateToProps = ({CheckoutReducer, CustomerReducer}) => ({
  status: CheckoutReducer.status,
  cart: CheckoutReducer.cart,
  order: CheckoutReducer.order,
  delivery: CheckoutReducer.delivery,
  customer: CustomerReducer.customer
});

export default connect(
  mapStateToProps
)(OrderConfirmation);

