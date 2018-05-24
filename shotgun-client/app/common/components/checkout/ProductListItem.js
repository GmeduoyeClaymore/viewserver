import React from 'react';
import {View} from 'react-native';
import {Button, Text} from 'native-base';
import {withExternalState} from 'custom-redux';
import {Icon} from 'common/components';

const onChangeProduct = ({order, product, setState}) => {
  const {productId} = product;
  setState({ selectedProduct: product, order: {...order, orderProduct: product, productId}});
};

const ProductListItem = ({product, order, setState, selectedProduct = {}, index: i, dispatch}) => {
  if (!product){
    return null;
  }

  return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
    <Button style={{height: 'auto'}} large active={selectedProduct.productId == product.productId} onPress={() => onChangeProduct({context: this, order, product, setState: part => setState(part, undefined, dispatch)})}>
      <Icon name={product.productId}/>
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


