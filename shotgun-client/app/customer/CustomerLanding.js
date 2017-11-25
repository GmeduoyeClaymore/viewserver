import React, {Component} from 'react';
import {connect} from 'react-redux';
import {setLocale} from 'yup/lib/customLocale';
import ProductList from './product/ProductList';
import ProductCategoryList from './product/ProductCategoryList';
import ProductDetails from './product/ProductDetails';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from './checkout/Checkout';
import Cart from './Cart';

import Orders from './Orders';
import OrderDetail from './OrderDetail';
import { customerServicesRegistrationAction } from 'customer/actions/CustomerActions';
import CustomerSettings from './CustomerSettings';
import {isAnyLoading} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Text, Content, Container} from 'native-base';

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

const CustomerLandingContent = ({busy, ...props}) => {
  const {match} = props;

  return busy ? <Content><Text>Loading Application.....</Text></Content> :
    <Container>
      <Switch>
        <Route path={`${match.path}/ProductCategoryList`} exact render={() => <ProductCategoryList {...props} />}/>
        <Route path={`${match.path}/ProductList`} exact render={() => <ProductList {...props} />}/>
        <Route path={`${match.path}/ProductDetails`} exact render={() => <ProductDetails {...props} />}/>
        <Route path={`${match.path}/Cart`} exact render={() => <Cart {...props} />}/>
        <Route path={`${match.path}/Checkout`} render={() => <Checkout {...props} />}/>
        <Route path={`${match.path}/Orders`} exact render={() => <Orders {...props} />}/>
        <Route path={`${match.path}/OrderDetail`} exact render={() => <OrderDetail {...props} />}/>
        <Route path={`${match.path}/CustomerSettings`} exact render={() => <CustomerSettings {...props} />}/>
        <Redirect to={`${match.path}/${CustomerLanding.INITIAL_ROOT_NAME}`}/>
      </Switch>
      <CustomerMenuBar/>
    </Container>;
};

const ConnectedCustomerLandingContent =  connect(mapStateToProps)(CustomerLandingContent);

export default class CustomerLanding extends Component {
  static INITIAL_ROOT_NAME = 'ProductCategoryList';

  constructor(props) {
    super(props);
    this.state = {
      isReady: false
    };
  }

  async componentWillMount() {
    const {dispatch, customerId, client} = this.props;
    dispatch(customerServicesRegistrationAction(client, customerId, () => this.setState({isReady: true})));
  }

  render() {
    return this.state.isReady ? <ConnectedCustomerLandingContent {...this.props}/> : <Container><Text>Loading Application.....</Text></Container>;
  }
}


