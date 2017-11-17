import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, StyleSheet } from 'react-native';
import ProductActionBar from './ProductActionBar';
import ActionButton from '../../common/components/ActionButton';
import backIcon from '../../common/assets/back.png';

const ProductDetails = ({navigation, screenProps}) => {
    const {customerService} = screenProps;
    const { product } = navigation.state.params;
    if (product) {
      return (
        <View style={styles.container}>
          <Image source={require('../assets/cement.jpg')} style={styles.picture} />
          <View style={styles.header}>
            <ActionButton buttonText={null} icon={backIcon} action={() => navigation.goBack()}/>
            <Text style={styles.bigText}>{product.name}</Text>
          </View>
          <Text style={[styles.mediumText, styles.lightText]}>{product.description}</Text>
          <ProductActionBar product={product} cartItemsDao={customerService.cartItemsDao}/>
        </View>
      );
    }
    return null;
};

ProductDetails.PropTypes = {
  customerService: PropTypes.object,
  product: PropTypes.object
};

ProductDetails.navigationOptions = {header: null};

export default ProductDetails;

const styles = StyleSheet.create({
  container: {
    marginTop: 5,
    marginLeft: 10,
    marginRight: 10,
    backgroundColor: '#FFFFFF',
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'flex-start',
    alignItems: 'center'
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    height: 30
  },
  picture: {
    width: 80,
    height: 80,
    borderRadius: 40,
    marginBottom: 4,
    marginTop: 10
  },
  mediumText: {
    fontSize: 16,
  },
  bigText: {
    fontSize: 20,
    flex: 5
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  list: {
    flex: 1,
  },
  emptyList: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  lightText: {
    color: '#C7C7CC'
  }
});