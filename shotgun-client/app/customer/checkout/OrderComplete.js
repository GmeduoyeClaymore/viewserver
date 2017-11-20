import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import ActionButton from 'common/components/ActionButton';
import {getDaoCommandResult} from 'common/dao';

const OrderComplete = ({navigation, order}) => {
  return <View style={{flex: 1, flexDirection: 'column'}}>
    <Text>Your Order Has Been Placed</Text>
    <Text>{`Order Id ${order.orderId}`}</Text>
    <ActionButton buttonText="Continue Shopping" icon={null} action={() => navigation.navigate('ProductCategoryList')}/>
  </View>;
};

OrderComplete.PropTypes = {
  order: PropTypes.object
};

OrderComplete.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => ({
  orderId: getDaoCommandResult(state, 'purchaseCartItems', 'cartItemsDao'),
  ...initialProps
});

export default connect(
  mapStateToProps
)(OrderComplete);


