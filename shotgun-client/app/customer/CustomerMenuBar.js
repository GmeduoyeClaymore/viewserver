import React from 'react';
import { View, StyleSheet } from 'react-native';
import {connect} from 'react-redux';
import ActionButton from 'common/components/ActionButton';
import cartIcon from  'common/assets/cart-outline.png';
import homeIcon from  'common/assets/home.png';
import orderIcon from  'common/assets/orders.png';
import PropTypes from 'prop-types';
import {getDaoState} from 'common/dao';

const CustomerMenuBar = ({cart, navigation, summary}) => {
    return cart && summary ? <View style={styles.container}>
      <ActionButton buttonText={null} icon={homeIcon} action={() => navigation.navigate('ProductCategoryList')}/>
      <ActionButton buttonText={`(${summary.totalQuantity})`} icon={cartIcon} action={() => navigation.navigate('Cart')}/>
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

const mapStateToProps = (state, initialProps) => ({
  cart: getDaoState(state, ['cart'], 'cartItemsDao'),
  summary: getDaoState(state, [], 'cartSummaryDao'),
  ...initialProps
});

export default connect(
  mapStateToProps
)(CustomerMenuBar);

