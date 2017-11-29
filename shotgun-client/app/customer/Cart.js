import React from 'react';
import PropTypes from 'prop-types';
import {Text, Button} from 'native-base';
import {TouchableOpacity} from 'react-native';
import { Col, Row, Grid } from 'react-native-easy-grid';
import {connect} from 'react-redux';
import {isAnyOperationPending, getDaoState} from 'common/dao';
import {updateCartItemQuantityAction} from 'customer/actions/CustomerActions';
import Currency from 'common/components/Currency';

const Cart = ({history, cart, busy, summary, dispatch}) => {
  const updateQuantity = (productId, quantity) => {
    dispatch(updateCartItemQuantityAction({productId, quantity}));
  };

  const renderCartItem = (item) => {
    return <Row key={item.key}>
      <Col size={80}>
        <Row><Text>{`Product: ${item.name} - (${item.productId})`}</Text></Row>
        <Row>
          <TouchableOpacity disabled={busy} onPress={() => updateQuantity(item.productId, item.quantity - 1)}><Text style={{color: '#007AFF'}}>-</Text></TouchableOpacity>
          <Text>{`Quantity: ${item.quantity}`}</Text>
          <TouchableOpacity disabled={busy} onPress={() => updateQuantity(item.productId, item.quantity + 1)}><Text style={{color: '#007AFF'}}>+</Text></TouchableOpacity>
        </Row>
        <Row>
          <Text>Price: <Currency value={item.price}/></Text><Text>Total: <Currency value={item.totalPrice}/></Text>
        </Row>
      </Col>
      <Col size={20}><TouchableOpacity disabled={busy} onPress={() => updateQuantity(item.productId, 0)}><Text style={{color: '#007AFF'}}>Remove</Text></TouchableOpacity></Col>
    </Row>;
  };

  return <Grid>
    {cart.items.map(c => renderCartItem(c))}
    <Row>
      <Col><Text>{`Total Items ${summary.totalQuantity}`}</Text></Col>
      <Col><Text>Total Price <Currency value={summary.totalPrice}/></Text></Col>
    </Row>
    <Button disabled={busy || summary.totalQuantity == 0} onPress={() => history.push('/Customer/Checkout')}><Text>Proceed to Checkout</Text></Button>
  </Grid>;
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
