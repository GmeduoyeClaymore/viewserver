import React from 'react';
import {Image, View} from 'react-native';
import {Button, Text} from 'native-base';
import {withExternalState} from 'custom-redux';
import {ProductImages} from 'common/assets/img/Images';

const onChangeProduct = ({order, product, setState}) => {
  const {productId, name} = product;
  setState({ selectedProduct: product, order: {...order, orderProduct: product, title: name, productId}});
};

const ProductListItem = ({product, order, setState, selectedProduct = {}, index: i, dispatch}) => {
  if (!product){
    return null;
  }

  return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
    <Button style={{height: 'auto'}} large active={selectedProduct.productId == product.productId} onPress={() => onChangeProduct({context: this, order, product, setState: part => setState(part, undefined, dispatch)})}>
      <Image source={ProductImages[product.productId]} style={styles.image}/>
    </Button>
    <Text style={styles.productSelectText}>{product.name}</Text>
  </View>;
};

ProductListItem.stateKey = 'checkout';

const styles = {
  image: {
    resizeMode: 'contain',
    height: '70%',
    width: '100%',
  },
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


