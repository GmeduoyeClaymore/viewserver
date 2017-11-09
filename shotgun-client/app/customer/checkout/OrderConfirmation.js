import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import icon from '../../common/assets/truck-fast.png';

export default class OrderConfirmation extends Component {
  static PropTypes = {
    customerService: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.purchase = this.purchase.bind(this);
    this.updateCartSummary = this.updateCartSummary.bind(this);
    this.updateCartItems = this.updateCartItems.bind(this);
    this.customerService = this.props.screenProps.customerService;
    this.navigation = props.navigation;
    this.state = {
      order: this.props.navigation.state.params.order,
      delivery: this.props.navigation.state.params.delivery,
      orderItems: []
    };
  }

  componentWillMount(){
    this.cartSummarySubscription = this.customerService.cartSummaryDao.subscribe(this.updateCartSummary);
    this.ordersSubscription = this.customerService.cartItemsDao.onSnapshotCompleteObservable.subscribe(this.updateCartItems);
  }

  componentWillUnmount(){
    if (this.cartSummarySubscription){
      this.cartSummarySubscription.dispose();
    }

    if (this.ordersSubscription){
      this.ordersSubscription.dispose();
    }
  }

  updateCartSummary(cartSummary){
    const summary = cartSummary ? cartSummary : { totalQuantity: 0, totalPrice: 0};
    this.setState({...summary});
  }

  updateCartItems(orderItems){
    this.setState({orderItems});
  }

  async purchase() {
      this.setState({busy: true});
      const deliveryId = await this.customerService.deliveryDao.createDelivery(this.state.delivery);
      this.setState({order: Object.assign({}, this.state.order, {deliveryId})});
      const orderId = await this.customerService.orderDao.createOrder(this.state.order);
      await this.customerService.orderItemsDao.purchaseCartItems(orderId);
      this.setState({busy: false});
      this.props.navigation.navigate('OrderComplete', {orderId});
  }

  renderCartItem(item) {
    return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
      <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
      <Text>{`Quantity: ${item.quantity}`}</Text>
      <Text>{`Price: ${item.price}`}</Text>
      <Text>{`Total: ${item.totalPrice}`}</Text>
    </View>;
  }

  render() {
    const {busy, totalQuantity, totalPrice, orderItems, order, delivery} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      {orderItems.map(c => this.renderCartItem(c))}
      <Text>{`Total Items ${totalQuantity}`}</Text>
      <Text>{`Total Price ${totalPrice}`}</Text>
      <Text>Payment {order.paymentId}</Text>
      <Text>Delivery Details {delivery.deliveryAddressId}</Text>
      <Text>Delivery Requested in {delivery.eta}</Text>
      {!busy ? <ActionButton buttonText="Place Order" icon={icon} action={() => this.purchase()}/> : null}
    </View>;
  }
}
