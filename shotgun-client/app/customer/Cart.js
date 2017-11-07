import React, {Component, PropTypes} from 'react';
import {View, Text} from 'react-native';
import ActionButton from '../common/components/ActionButton';
import icon from '../common/assets/truck-fast.png';

export default class Cart extends Component {
    static PropTypes = {
      customerService: PropTypes.object
    };

    static navigationOptions = {header: null};

    constructor(props) {
      super(props);
      this.purchaseItems = this.purchaseItems.bind(this);
      this.updateCartSummary = this.updateCartSummary.bind(this);
      this.updateCartItems = this.updateCartItems.bind(this);
      this.customerService = this.props.screenProps.customerService;
      this.state = {
        busy: false,
        totalQuantity: 0,
        totalPrice: 0,
        items: []
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

    updateCartItems(){
      this.setState({items: this.customerService.cartItemsDao.rows});
    }

    renderCartItem(item) {
      return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{`Product: ${item.name} - (${item.productId})`}</Text>
        <Text>{`Quantity: ${item.quantity}`}</Text>
        <Text>{`Price: ${item.price}`}</Text>
        <Text>{`Total: ${item.totalPrice}`}</Text>
      </View>;
    }

    async purchaseItems() {
      try {
        this.setState({busy: true});
        const orderId = await this.customerService.orderDao.createOrder();
        await this.customerService.orderItemsDao.purchaseCartItems(orderId);
      } finally {
        this.setState({busy: false});
        this.props.navigation.navigate('Home');
      }
    }

    render() {
      const {busy, totalQuantity, totalPrice, items} = this.state;

      return <View style={{flex: 1, flexDirection: 'column'}}>
        {items.map(c => this.renderCartItem(c))}
        <Text>{`Total Items ${totalQuantity}`}</Text>
        <Text>{`Total Price ${totalPrice}`}</Text>
        {!busy ? <ActionButton buttonText="Purchase" icon={icon} action={this.purchaseItems}/> : null}
      </View>;
    }
}
