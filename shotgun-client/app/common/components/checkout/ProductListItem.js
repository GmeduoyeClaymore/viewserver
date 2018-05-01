import React from 'react';
import { View, Text} from 'react-native';
import {Button} from 'native-base';
import {Icon} from 'common/components';
import {withExternalState} from 'custom-redux';

const onChangeProduct = ({order, product, setState}) => {
  const {productId, name} = product;
  setState({ selectedProduct: product, order: {...order, orderProduct: product, title: name, productId, amount: product.price}});
};

const ProductListItem = ({product, order, setState, selectedProduct = {}, index: i, dispatch}) => {
  if (!product){
    return null;
  }
  return <View key={i} style={{width: '50%', paddingRight: 5, paddingLeft: 5, maxWidth: 250, maxHeight: 250}}>
    <Button style={{height: 'auto'}} large active={selectedProduct.productId == product.productId} onPress={() => onChangeProduct({context: this, order, product, setState: part => setState(part, undefined, dispatch)})}>
      <Icon name={product.imageUrl || 'dashed'}/>
    </Button>
    <Text style={styles.productSelectText}>{product.name}</Text>
  </View>;
};

ProductListItem.stateKey = 'checkout';

const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30,
    fontSize: 13
  },
  productSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

export default withExternalState()(ProductListItem);


