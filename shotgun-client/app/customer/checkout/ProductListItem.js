import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';
import { merge } from 'lodash';
import {resolveProductIcon} from 'common/assets';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  },
  picture: {
    width: 60,
    height: 60,
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 12
  },
  summary: {
    fontSize: 10
  }
});

const onChangeProduct = ({navigationStrategy, context, product}) => {
  if (!context){
    return;
  }
  const { orderItem } = context.state;
  const {productId} = product;
  context.setState({ selectedProduct: product, orderItem: merge({}, orderItem, {productId}) });
  if (navigationStrategy){
    navigationStrategy.next();
  }
};

const ProductListItem = ({context, product, navigationStrategy}) => {
  if (!product){
    return null;
  }

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80, backgroundColor: 'white'}} onPress={() => onChangeProduct({context, product, navigationStrategy}) } underlayColor={'#EEEEEE'}>
    <View style={styles.container}>
      <Image resizeMode="contain" source={resolveProductIcon(product)}  style={styles.picture}/>
      <View style={{flex: 1, padding: 5}}>
        <Text style={styles.title}>{product.name}</Text>
        <Text style={styles.summary}>{product.description}</Text>
      </View>
    </View>
  </TouchableHighlight>;
};

ProductListItem.propTypes = {
  product: PropTypes.object,
  navigationStrategy: PropTypes.object
};

export default ProductListItem;


