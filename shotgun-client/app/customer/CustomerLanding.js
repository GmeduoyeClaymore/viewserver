import React, {Component} from 'react';
import {View} from 'react-native';
import ProductList from './ProductList';
import ProductDetails from './ProductDetails';
import CustomerMenuBar from './CustomerMenuBar';
import Cart from './Cart';
import CustomerServiceFactory from './data/CustomerServiceFactory';
import Logger from '../viewserver-client/Logger';
import {StackNavigator} from 'react-navigation';

export default class CustomerLanding extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isReady: false
    };

    this.client = this.props.screenProps.client;
    this.principal = this.props.screenProps.principal;
    this.customerServiceFactory = new CustomerServiceFactory(this.client);
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
    ProductList: {screen: ProductList},
    ProductDetails: { screen: ProductDetails },
    Cart: { screen: Cart }
  }, {
    initialRouteName: 'ProductList'
  });

CustomerLanding.router = CustomerLandingNavigator.router;
