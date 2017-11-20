import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import {getDaoState, updateSubscriptionAction} from 'common/dao';
import {Spinner} from 'native-base';

class OrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch} = this.props;
    const {isCompleted} = this.props.navigation.state.params;
    dispatch(updateSubscriptionAction('orderSummaryDao', {isCompleted}));
  }

  render() {
    const {customer, busy} = this.props;
    const {orderId, isCompleted} = this.props.navigation.state.params;
    let orderType = 'complete';

    if (!isCompleted) {
      orderType = 'incomplete';
    }
    const orderSummary = orders[orderType].find(o => o.orderId == orderId);
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
    {customer.orderDetail.items.map(c => renderOrderItem(c))}
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

OrderDetail.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => ({
  paymentCards: getDaoState(state, ['paymentCards'], 'paymentCardsDao'),
  orders: getDaoState(state, ['orders'], 'orderSummaryDao'),
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentCardsDao']),
  ...initialProps
});

export default connect(
  mapStateToProps
)(OrderDetail);

