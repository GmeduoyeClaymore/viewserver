import React, {Component, PropTypes} from 'react';
import {View, Text} from 'react-native';
import ActionButton from '../common/components/ActionButton';
import icon from '../common/assets/truck-fast.png';

export default class Orders extends Component {
    static PropTypes = {
      customerService: PropTypes.object
    };

    constructor(props) {
      super(props);
      this.updateOrders = this.updateOrders.bind(this);
      this.customerService = this.props.screenProps.customerService;
      this.state = {
        busy: false,
        orders: []
      };
    }

    componentWillMount(){
      this.ordersSubscription = this.customerService.orderSummaryDao.onSnapshotCompleteObservable.subscribe(this.updateOrders);
    }

    componentWillUnmount(){
      if (this.ordersSubscription){
        this.ordersSubscription.dispose();
      }
    }

    updateOrders(){
      this.setState({orders: this.customerService.orderSummaryDao.rows});
    }

    renderOrder(order) {
      return <View key={order.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Order: ${order.orderId}`}</Text>
        <Text>{`£${order.totalPrice} (${order.totalQuantity} items)`}</Text>
      </View>;
    }

    render() {
      const {orders} = this.state;

      return <View style={{flex: 1, flexDirection: 'column'}}>
        {orders.map(c => this.renderOrder(c))}
      </View>;
    }
}
