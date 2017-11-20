import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import {Spinner} from 'native-base';
import ActionButton from 'common/components/ActionButton';
import icon from 'common/assets/truck-fast.png';
import {purchaseCartItemsAction} from 'customer/CustomerActions';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';

const OrderConfirmation = ({cart, eta, paymentId, deliveryAddressId, status, dispatch, navigation, errors}) => {
    const purchase = async() => {
      dispatch(purchaseCartItemsAction(eta, paymentId, deliveryAddressId, deliveryType), () => navigation.navigate('OrderComplete'));
    };

    const renderCartItem = (item) => {
      return  <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
        <Text>{`Quantity: ${item.quantity}`}</Text>
        <Text>{`Price: ${item.price}`}</Text>
        <Text>{`Total: ${item.totalPrice}`}</Text>
      </View>;
    };

    return <ErrorRegion errors={errors}><View style={{flex: 1, flexDirection: 'column'}}>
      {cart.items.map(c => renderCartItem(c))}
      <Text>{`Total Items ${cart.totalQuantity}`}</Text>
      <Text>{`Total Price ${cart.totalPrice}`}</Text>
      <Text>Payment {paymentCard.cardNumber}</Text>
      <Text>Delivery Details {deliveryAddress.line1}</Text>
      <Text>Delivery Requested in {eta} hours</Text>
      {!status.busy ? <ActionButton buttonText="Place Order" icon={icon} action={purchase}/> :  <Spinner />}
    </View></ErrorRegion>;
};

OrderConfirmation.PropTypes = {
  status: PropTypes.object,
  cart: PropTypes.object,
  order: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

OrderConfirmation.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => {
  const customer = getDaoState(state, [], 'customerDao');
  const navigationParams = props.navigation.state.params;
  const {eta, paymentId, deliveryAddressId, deliveryType} = navigationParams;
  const deliveryAddress = customer.deliveryAddresses.find(a => a.deliveryAddressId == deliveryAddressId);
  const paymentCard = customer.paymentCards.find(a => a.paymentId == paymentId);
  
  return {
    cart: getDaoState(state, ['cart'], 'cartItemsDao'),
    errors: getOperationError(state, 'cartItemsDao', 'purchaseCartItems'),
    deliveryAddress,
    paymentCard,
    eta,
    deliveryType,
    deliveryAddressId,
    paymentId,
    busy: isAnyOperationPending(state, { cartItemsDao: 'purchaseCartItems'}),
    ...initialProps
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

