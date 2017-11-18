import React, {Component} from 'react';
import {connect} from 'react-redux';
import {View} from 'react-native';
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
import CustomerServiceFactory from './data/CustomerServiceFactory';
import Logger from 'common/Logger';
import {StackNavigator} from 'react-navigation';
import {isPaging as isLoading} from 'common/dao';

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

const CustomerLandingContent = ({navigation, screenProps, busy}) => (
  busy ? null :  <View style={{flexDirection: 'column', flex: 1}}>
  <CustomerMenuBar navigation={navigation}/>
  <CustomerLandingNavigator navigation={navigation}  screenProps={screenProps} />
</View>
);

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isLoading(state, 'orderItems') ||
        isLoading(state, 'cartItems') ||
        isLoading(state, 'cartSummary') ||
        isLoading(state, 'order')  ||
        isLoading(state, 'orderItems') ||
        isLoading(state, 'customer')  ||
        isLoading(state, 'paymentCards')  ||
        isLoading(state, 'deliveryAddresses')  ||
        isLoading(state, 'delivery'), ...nextOwnProps
});

const ConnectedCustomerLandingContent =  connect(mapStateToProps)(CustomerLandingContent);

class CustomerLanding extends Component {
  static INITIAL_ROOT_NAME = 'ProductCategoryList';

  constructor(props) {
    super(props);
    this.state = {
      isReady: false
    };

    const { client, dispatch, customerId} = this.props.screenProps;
    this.customerId = customerId;
    this.dispatch = dispatch;
    this.customerServiceFactory = new CustomerServiceFactory(client, dispatch);
  }

  async componentWillMount() {
    try {
      this.customerService = await this.customerServiceFactory.create(this.customerId);
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
    const {dispatch} = this;
    const screenProps = {customerService: this.customerService, client: this.client, dispatch};
    return <ConnectedCustomerLandingContent {...this.props} screenProps={screenProps}/>;
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
    OrderDetail: {screen: OrderDetail}
  }, {
    initialRouteName: CustomerLanding.INITIAL_ROOT_NAME,
    headerMode: 'screen'
  });

CustomerLanding.router = CustomerLandingNavigator.router;

export default CustomerLanding;


