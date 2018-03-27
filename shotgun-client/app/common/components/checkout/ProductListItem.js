import React from 'react';
import PropTypes from 'prop-types';
import { View, Text} from 'react-native';
import {Button} from 'native-base';
import {Icon} from 'common/components';
import {withExternalState} from 'custom-redux';


const onChangeProduct = ({orderItem, product, setState}) => {
  const {productId} = product;
  setState({ selectedProduct: product, orderItem: {...orderItem, productId}});
};

const ProductListItem = ({product, navigationStrategy, setState, selectedProduct = {}, index: i}) => {
  if (!product){
    return null;
  }
  return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
    <Button style={{height: 'auto'}} large active={selectedProduct.productId == product.productId} onPress={() => onChangeProduct({context: this, product, navigationStrategy, setState})}>
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


ProductListItem.propTypes = {
  product: PropTypes.object,
  navigationStrategy: PropTypes.object
};

export default withExternalState()(ProductListItem);


