import React, {Component} from 'react';
import {connect} from 'react-redux';
import {setLocale} from 'yup/lib/customLocale';
import ProductSelect from './product/ProductSelect';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from './checkout/Checkout';
import Cart from './Cart';
import Orders from './Orders';
import OrderDetail from './OrderDetail';
import {customerServicesRegistrationAction} from 'customer/actions/CustomerActions';
import CustomerSettings from './CustomerSettings';
import {isAnyLoading} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';

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
  static INITIAL_ROOT_NAME = 'ProductSelect';

  constructor(props) {
    super(props);
  }

  async componentWillMount() {
    const {dispatch, userId, client} = this.props;
    dispatch(customerServicesRegistrationAction(client, userId));
  }

  render() {
    const {match, busy} = this.props;

    return busy ? <LoadingScreen text="Loading Customer Landing Screen"/> :
      <Container>
        <Switch>
          {/*  <Route path={`${match.path}/ProductCategoryList`} exact component={ProductCategoryList}/>
          <Route path={`${match.path}/ProductList`} exact component={ProductList}/>
          <Route path={`${match.path}/ProductDetails`} exact component={ProductDetails}/>*/}
          <Route path={`${match.path}/ProductSelect`} exact component={ProductSelect}/>
          <Route path={`${match.path}/Cart`} exact component={Cart}/>
          <Route path={`${match.path}/Checkout`} component={Checkout}/>
          <Route path={`${match.path}/Orders`} exact component={Orders}/>
          <Route path={`${match.path}/OrderDetail`} exact component={OrderDetail}/>
          <Route path={`${match.path}/CustomerSettings`} exact component={CustomerSettings}/>
          <Redirect to={`${match.path}/${CustomerLanding.INITIAL_ROOT_NAME}`}/>
        </Switch>
        <CustomerMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, [
    'orderItemsDao',
    'cartItemsDao',
    'cartSummaryDao',
    'orderDao',
    'customerDao',
    'paymentCardsDao',
    'deliveryAddressDao',
    'deliveryDao']), ...nextOwnProps
});

export default connect(mapStateToProps)(CustomerLanding);


