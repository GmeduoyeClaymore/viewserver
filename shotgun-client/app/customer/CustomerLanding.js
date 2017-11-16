import React, {Component} from 'react';
import {View} from 'react-native';
import {connect} from 'react-redux';
import {setLocale} from 'yup/lib/customLocale';
import ProductList from './product/ProductList';
import ProductCategoryList from './product/ProductCategoryList';
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
import CustomerSettings from './CustomerSettings';
import CustomerServiceFactory from './data/CustomerServiceFactory';
import Logger from '../viewserver-client/Logger';
import {StackNavigator} from 'react-navigation';

//TODO - we should be able to put this in App.js but it doesn't work for some reason
setLocale({
  mixed: {
    required: 'is required',
    matches: 'is invalid',
    max: 'is too long',
    min: 'is too short'
  },
  string: {
    email: 'is not a valid email'
  }
});

class CustomerLanding extends Component {
  static INITIAL_ROOT_NAME = 'ProductCategoryList';

  constructor(props) {
    super(props);
    this.state = {
      isReady: false
    };

    this.client = this.props.screenProps.client;
    this.customerId = this.props.screenProps.customerId;
    this.customerServiceFactory = new CustomerServiceFactory(this.client, this.props.dispatch);
  }

  async componentWillMount() {
    try {
      this.customerService = await this.customerServiceFactory.create(this.customerId);
    } catch (error) {
      Logger.error(error);
    }
    Logger.debug('CustomerLanding mounted');
    this.setState({isReady: true});
  }

  render() {
    if (!this.state.isReady) {
      return null;
    }
    Logger.debug('CustomerLanding rendering');

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
    Cart: { screen: Cart },
    Payment: { screen: Payment },
    Delivery: { screen: Delivery },
    DeliveryOptions: { screen: DeliveryOptions },
    OrderConfirmation: { screen: OrderConfirmation },
    OrderComplete: { screen: OrderComplete },
    Orders: {screen: Orders},
    OrderDetail: {screen: OrderDetail},
    CustomerSettings: {screen: CustomerSettings}
  }, {
    initialRouteName: CustomerLanding.INITIAL_ROOT_NAME,
    headerMode: 'screen'
  });

CustomerLanding.router = CustomerLandingNavigator.router;

export default connect()(CustomerLanding);
