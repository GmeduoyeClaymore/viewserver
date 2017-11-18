import React from 'react';
import { View, StyleSheet } from 'react-native';
import {connect} from 'react-redux';
import ActionButton from 'common/components/ActionButton';
import cartIcon from  'common/assets/cart-outline.png';
import homeIcon from  'common/assets/home.png';
import orderIcon from  'common/assets/orders.png';
import PropTypes from 'prop-types';

const CustomerMenuBar = ({cart, navigation}) => {
    return cart ? <View style={styles.container}>
      <ActionButton buttonText={null} icon={homeIcon} action={() => navigation.navigate('ProductCategoryList')}/>
      <ActionButton buttonText={`(${cart.totalQuantity})`} icon={cartIcon} action={() => navigation.navigate('Cart')}/>
      <ActionButton buttonText={null} icon={orderIcon} action={() => navigation.navigate('Orders')}/>
    </View> : null;
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingTop: 10,
    flexDirection: 'row'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});

CustomerMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = ({cartItems}) => ({
  cart: cartItems && cartItems.cart
});

export default connect(
  mapStateToProps
)(CustomerMenuBar);

