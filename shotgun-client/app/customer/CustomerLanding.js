import React, {Component} from 'react';
import {View} from 'react-native';
import {connect} from 'react-redux';
import ProductList from './product/ProductList';
import ProductCategoryList from './product/ProductCategoryList';
import CustomerRegistration from './registration/CustomerRegistration';
import ProductDetails from './product/ProductDetails';
import CustomerMenuBar from './CustomerMenuBar';
import Cart from './checkout/Cart';
import Payment from './checkout/Payment';
import Delivery from './checkout/Delivery';
import DeliveryOptions from './checkout/DeliveryOptions';
import OrderConfirmation from './checkout/OrderConfirmation';
import OrderComplete from './checkout/OrderComplete';
import Orders from './Orders';
import OrderDetail from './OrderDetail';
import CustomerServiceFactory from './data/CustomerServiceFactory';
import Logger from '../viewserver-client/Logger';
import {StackNavigator} from 'react-navigation';

class CustomerLanding extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isReady: false
    };

    this.client = this.props.screenProps.client;
    this.principal = this.props.screenProps.principal;
    this.customerServiceFactory = new CustomerServiceFactory(this.client, this.props.dispatch, this.props.getState);
  }

  async componentWillMount() {
    try {
      this.customerService = await this.customerServiceFactory.create(this.principal.customerId);
    } catch (error) {
      Logger.error(error);
    }
    Logger.debug('Network connected !!');
    this.setState({isReady: true});
  }

  render() {
    if (!this.state.isReady) {
      return null;
    }
    const screenProps = {customerService: this.customerService, client: this.client};

    return <View style={{flexDirection: 'column', flex: 1}}>
      <CustomerMenuBar navigation={this.props.navigation} cartSummaryDao={this.customerService.cartSummaryDao}/>
      <CustomerLandingNavigator navigation={this.props.navigation}  screenProps={screenProps} />
    </View>;
  }
}

const CustomerLandingNavigator = StackNavigator(
  {
    ProductCategoryList: {screen: ProductCategoryList},
    ProductList: {screen: ProductList},
    ProductDetails: { screen: ProductDetails },
    CustomerRegistration: { screen: CustomerRegistration },
    Cart: { screen: Cart },
    Payment: { screen: Payment },
    Delivery: { screen: Delivery },
    DeliveryOptions: { screen: DeliveryOptions },
    OrderConfirmation: { screen: OrderConfirmation },
    OrderComplete: { screen: OrderComplete },
    Orders: {screen: Orders},
    OrderDetail: {screen: OrderDetail}
  }, {
    initialRouteName: 'ProductCategoryList',
    headerMode: 'screen'
  });

CustomerLanding.router = CustomerLandingNavigator.router;

export default connect()(CustomerLanding);
