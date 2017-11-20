import React from 'react';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import {connect} from 'react-redux';
import ActionButton from '../../common/components/ActionButton';
import icon from '../../common/assets/truck-fast.png';
import {isAnyOperationPending, getDaoState} from 'common/dao';

const Cart = ({navigation, cart, busy, summary}) => {
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
      {!busy ? <ActionButton buttonText="Proceed to Checkout" icon={icon} action={() => navigation.navigate('Payment')}/> : null}
    </View>;
};

Cart.PropTypes = {
  busy: PropTypes.bool,
  cart: PropTypes.object,
  summary: PropTypes.object
};

Cart.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => ({
  cart: getDaoState(state, ['cart'], 'cartItemsDao'),
  summary: getDaoState(state, [], 'cartSummaryDao'),
  busy: isAnyOperationPending(state, { deliveryDao: 'createDelivery', orderDao: 'createOrder'}),
  ...initialProps
});

export default connect(
  mapStateToProps
)(Cart);
