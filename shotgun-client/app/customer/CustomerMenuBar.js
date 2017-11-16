import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Button, Icon, Text } from 'native-base';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';

const CustomerMenuBar = ({cart, navigation}) => {
    return <View style={styles.container}>
      <Button transparent dark onPress={() => navigation.navigate('ProductCategoryList')}><Icon name='home'/></Button>
      <Button transparent dark onPress={() => navigation.navigate('Cart')}><Icon name='cart'/><Text>{cart.totalQuantity}</Text></Button>
      <Button transparent dark onPress={() => navigation.navigate('Orders')}><Icon name='list'/></Button>
      <Button transparent dark onPress={() => navigation.navigate('CustomerSettings')}><Icon name='settings'/></Button>
    </View>;
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingTop: 10,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});

CustomerMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = ({CheckoutReducer}) => ({
  cart: CheckoutReducer.cart
});

export default connect(
  mapStateToProps
)(CustomerMenuBar);

