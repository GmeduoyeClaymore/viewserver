import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';
import { merge } from 'lodash';
import {resolveProductIcon} from 'common/assets';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5
  },
  picture: {
    width: 80,
    height: 80,
    borderRadius: 20,
    marginRight: 8
  },
  title: {
    fontWeight: 'bold',
    color: '#848484'
  }
});

const onChangeProduct = ({history, context, product}) => {
  if (!context){
    return;
  }
  const { orderItem } = context.state;
  const {productId} = product;
  context.setState({ selectedProduct: product, orderItem: merge({}, orderItem, {productId}) });
  if (history){
    history.push('/Customer/Checkout/ProductCategoryList');
  }
};

const ProductListItem = ({history, context, product}) => {
  if (!product){
    return null;
  }

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80}} onPress={() => onChangeProduct({history, context, product}) } underlayColor={'#EEEEEE'}>
    <View style={styles.container}>
      <Image resizeMode="contain" source={resolveProductIcon(product)}  style={styles.picture}/>
      <View style={{flex: 1}}>
        <Text style={styles.title}>{product.name}</Text>
        <Text>{product.description}</Text>
      </View>
    </View>
  </TouchableHighlight>;
};

ProductListItem.propTypes = {
  product: PropTypes.object,
  history: PropTypes.object
};

export default ProductListItem;


