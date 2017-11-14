import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import OrderItemsDao from './data/OrderItemsDao';

class OrderDetail extends Component{
  constructor(props) {
    super(props);
    const {customerId} = props.screenProps.customerService;
    const {orderId} = props.navigation.state.params;
    this.orderItemsDao = new OrderItemsDao(props.screenProps.client, props.dispatch, customerId, orderId);
  }

  componentWillMount(){
    this.orderItemsDao.subscribe();
  }

  render() {
    const {customer} = this.props;
    const {orderId, isCompleted} = this.props.navigation.state.params;
    let orderType = 'complete';

    if (!isCompleted) {
      orderType = 'incomplete';
    }
    const orderSummary = customer.orders[orderType].find(o => o.orderId == orderId);
    const paymentCard = customer.paymentCards.find(a => a.paymentId == orderSummary.paymentId);

    const renderOrderItem = (item) => {
      return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
        <Text>{`Quantity: ${item.quantity}`}</Text>
        <Text>{`Price: ${item.price}`}</Text>
        <Text>{`Total: ${item.totalPrice}`}</Text>
      </View>;
    };

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>{orderSummary.orderId}</Text>
    {customer.orderDetail.items.map(c => renderOrderItem(c))}
      <Text>{`Total Items ${orderSummary.totalQuantity}`}</Text>
      <Text>{`Total Price ${orderSummary.totalPrice}`}</Text>
      <Text>Payment {paymentCard.cardNumber}</Text>
    </View>;
  }
}

OrderDetail.PropTypes = {
  customer: PropTypes.object
};

OrderDetail.navigationOptions = {header: null};

const mapStateToProps = ({CustomerReducer}) => ({
  customer: CustomerReducer.customer
});

export default connect(
  mapStateToProps
)(OrderDetail);

