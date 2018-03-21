import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';
import {Button} from 'native-base';
import { merge } from 'lodash';
import {Icon} from 'common/components';


const onChangeProduct = ({navigationStrategy, context, product}) => {
  if (!context){
    return;
  }
  const { orderItem } = context.state;
  const {productId} = product;
  context.setState({ selectedProduct: product, orderItem: merge({}, orderItem, {productId}) });
};

const ProductListItem = ({context, product, navigationStrategy, selectedProduct = {}, index: i}) => {
  if (!product){
    return null;
  }
  return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
    <Button style={{height: 'auto'}} large active={selectedProduct.productId == product.productId} onPress={() => onChangeProduct({context, product, navigationStrategy})}>
      <Icon name={product.imageUrl || 'dashed'}/>
    </Button>
    <Text style={styles.productSelectText}>{product.name}</Text>
  </View>;
};

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

export default ProductListItem;


