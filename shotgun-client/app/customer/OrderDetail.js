import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import {getDaoState, updateSubscriptionAction, isAnyLoading} from 'common/dao';
import {Spinner} from 'native-base';

class OrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, location} = this.props;
    const {state = {}} = location;
    const {orderId} = state;
    dispatch(updateSubscriptionAction('orderItemsDao', {orderId}));
  }

  render() {
    const {busy, orders, orderItems = [], paymentCards, location} = this.props;
    const {state = {}} = location;
    const {orderId} = state;

    const orderSummary = orders.find(o => o.orderId == orderId);
    const paymentCard = paymentCards.find(a => a.paymentId == orderSummary.paymentId);

    const renderOrderItem = (item) => {
      return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
        <Text>{`Quantity: ${item.quantity}`}</Text>
        <Text>{`Price: ${item.price}`}</Text>
        <Text>{`Total: ${item.totalPrice}`}</Text>
      </View>;
    };

    return busy ? <Spinner/> : <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>{orderSummary.orderId}</Text>
      {orderItems.map(c => renderOrderItem(c))}
      <Text>{`Total Items ${orderSummary.totalQuantity}`}</Text>
      <Text>{`Total Price ${orderSummary.totalPrice}`}</Text>
      <Text>Payment {paymentCard.cardNumber}</Text>
    </View>;
  }
}

OrderDetail.PropTypes = {
  orders: PropTypes.object,
  paymentCards: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  paymentCards: getDaoState(state, ['customer', 'paymentCards'], 'paymentDao'),
  orders: getDaoState(state, ['customer', 'orders'], 'orderSummaryDao'),
  orderItems: getDaoState(state, ['customer', 'orderDetail', 'items'], 'orderItemsDao'),
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentDao']),
  ...initialProps
});

export default connect(
  mapStateToProps
)(OrderDetail);

