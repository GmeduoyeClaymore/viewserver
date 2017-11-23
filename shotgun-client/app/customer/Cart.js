import React from 'react';
import PropTypes from 'prop-types';
import {View} from 'react-native';
import {Text, Button} from 'native-base';
import {connect} from 'react-redux';
import {isAnyOperationPending, getDaoState} from 'common/dao';

const Cart = ({history, match, cart, busy, summary}) => {
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
    <Text>{`Total Items ${summary.totalQuantity}`}</Text>
    <Text>{`Total Price ${summary.totalPrice}`}</Text>
    {!busy ? <Button onPress={() => history.push(`${match.path}/Checkout`)}><Text>Proceed to Checkout</Text></Button> : null}
  </View>;
};

Cart.PropTypes = {
  busy: PropTypes.bool,
  cart: PropTypes.object,
  summary: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  cart: getDaoState(state, ['cart'], 'cartItemsDao'),
  summary: getDaoState(state, [], 'cartSummaryDao'),
  busy: isAnyOperationPending(state, { deliveryDao: 'createDelivery', orderDao: 'createOrder'}),
  ...initialProps
});

export default connect(
  mapStateToProps
)(Cart);
