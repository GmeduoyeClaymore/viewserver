import React, {Component} from 'react';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
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
import { customerServicesRegistrationAction } from 'customer/actions/CustomerActions';
import CustomerSettings from './CustomerSettings';
import {StackNavigator} from 'react-navigation';
import {isAnyLoading} from 'common/dao';

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
  busy ? <View><Text>Loading Application.....</Text></View> :  <View style={{flexDirection: 'column', flex: 1}}>
  <CustomerMenuBar navigation={navigation}/>
  <CustomerLandingNavigator navigation={navigation}  screenProps={screenProps} />
</View>
);

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, [
      'orderItems',
      'cartItems',
      'cartSummary',
      'order',
      'orderItems',
      'customer',
      'paymentCards',
      'deliveryAddresses',
      'delivery']), ...nextOwnProps
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
    this.client = client;
  }

  async componentWillMount() {
    const {dispatch, customerId, client} = this;
    dispatch(customerServicesRegistrationAction(client, customerId, () => this.setState({isReady: true})));
  }

  render() {
    if (!this.state.isReady) {
      return <View><Text>Loading Application.....</Text></View>;
    }
    const {dispatch, client} = this;
    const screenProps = {client, dispatch};
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
    OrderDetail: {screen: OrderDetail},
    CustomerSettings: {screen: CustomerSettings}
  }, {
    initialRouteName: CustomerLanding.INITIAL_ROOT_NAME,
    headerMode: 'screen'
  });

CustomerLanding.router = CustomerLandingNavigator.router;

export default CustomerLanding;


