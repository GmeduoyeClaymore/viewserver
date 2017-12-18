import React from 'react';
import {Text, Content, Button} from 'native-base';
import {merge} from 'lodash';

const ProductSelect = ({context, history}) => {
  const selectProduct = (productId) => {
    context.setState({order: merge({}, context.state.order, {productId})});
    history.push('/Customer/Checkout/DeliveryMap');
  };

  return (
    <Content>
      <Button onPress={() => selectProduct('PROD_Disposal')}><Text>Waste Disposal</Text></Button>
      <Button onPress={() => selectProduct('PROD_Delivery')}><Text>Deliver an Item</Text></Button>
    </Content>
  );
};

export default ProductSelect;
