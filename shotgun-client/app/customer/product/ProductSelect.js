import React from 'react';
import {Text, Content, Button} from 'native-base';
import {isOperationPending, getOperationError} from 'common/dao';
import {connect} from 'react-redux';
import {addItemToCartAction} from 'customer/actions/CustomerActions';

const ProductSelect = ({busy, dispatch, history}) => {
  const addToCart = (productId) => {
    dispatch(addItemToCartAction({productId, quantity: 1}));
    history.push('/Customer/Checkout');
  };

  return (
    <Content>
      <Button onPress={() => addToCart('PROD_Disposal')} disabled={busy}><Text>Waste Disposal</Text></Button>
      <Button onPress={() => addToCart('PROD_Delivery')} disabled={busy}><Text>Deliver an Item</Text></Button>
    </Content>
  );
};

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isOperationPending(state, 'cartItemsDao', 'addItemToCart'),
  errors: getOperationError(state, 'cartItemsDao', 'addItemToCart'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(ProductSelect);
