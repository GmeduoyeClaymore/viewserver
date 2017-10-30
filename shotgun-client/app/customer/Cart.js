import React, {Component, PropTypes} from 'react';
import {View, Text} from 'react-native';
import ActionButton from '../common/ActionButton';
import icon from '../common/assets/truck-fast.png';

export default class Cart extends Component {
    static PropTypes = {
      customerService: PropTypes.object
    };

    constructor(props) {
      super(props);
      this.purchaseItems = this.purchaseItems.bind(this);
      this.state = {
        busy: false
      };
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
        const {orderDao, orderItemsDao} = this.props.screenProps.customerService;
        const orderId = await orderDao.createOrder();
        await orderItemsDao.purchaseCartItems(orderId);
      } finally {
        this.setState({busy: false});
      }
    }

    render() {
      const {cartItemsDao, cartSummaryDao} = this.props.screenProps.customerService;
      const {busy} = this.state;
      const cartSummary = cartSummaryDao.rows[0];

      return <View style={{flex: 1, flexDirection: 'column'}}>
        {cartItemsDao.rows.map(c => this.renderCartItem(c))}
        <Text>{`Total Items ${cartSummary.totalQuantity}`}</Text>
        <Text>{`Total Price ${cartSummary.totalPrice}`}</Text>
        {!busy ? <ActionButton buttonText="Purchase" icon={icon} action={this.purchaseItems}/> : null}
      </View>;
    }
}
