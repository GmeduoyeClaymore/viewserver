import React from 'react';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import {connect} from 'react-redux';
import ActionButton from '../../common/components/ActionButton';
import icon from '../../common/assets/truck-fast.png';

const Cart = ({navigation, cart, status}) => {
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
      {!status.busy ? <ActionButton buttonText="Proceed to Checkout" icon={icon} action={() => navigation.navigate('Payment')}/> : null}
    </View>;
};

Cart.PropTypes = {
  status: PropTypes.object,
  cart: PropTypes.object
};

Cart.navigationOptions = {header: null};

const mapStateToProps = ({CheckoutReducer}) => ({
  status: CheckoutReducer.status,
  cart: CheckoutReducer.cart
});

export default connect(
  mapStateToProps
)(Cart);
