import React, {Component, PropTypes} from 'react';
import {View, Text} from 'react-native';
import ActionButton from '../common/ActionButton';
import icon from '../common/assets/truck-fast.png';

export default class ShoppingCart extends Component {
    static PropTypes = {
      shoppingCartItems: PropTypes.array,
      customerService: PropTypes.object
    };

    constructor(props) {
      super(props);
      this.purchaseItems = this.purchaseItems.bind(this);
      this.state = {
        busy: false
      };
    }

    renderItem(item) {
      return <View key={item.key} style={{flexDirection: 'column', flex: 1}}>
        <Text>{'Product: ' + item.productId}</Text>
        <Text>{'Quantity: ' + item.quantity}</Text>
      </View>;
    }

    async purchaseItems() {
      try {
        this.setState({busy: true});
        const {orderDao, shoppingCartDao} = this.props.screenProps.customerService;
        const orderId = await orderDao.createOrder();
        await shoppingCartDao.purchaseCartItems(orderId);
      } finally {
        this.setState({busy: false});
      }
    }

    render() {
      const {rows} = this.props.navigation.state.params;
      const {busy} = this.state;
      return <View style={{flex: 1, flexDirection: 'column'}}>
        {rows.map(c => this.renderItem(c))}
        {!busy ? <ActionButton buttonText="Purchase" icon={icon} action={this.purchaseItems}/> : null}
      </View>;
    }
}
