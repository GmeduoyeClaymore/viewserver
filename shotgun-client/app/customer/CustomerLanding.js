import React, {Component} from 'react';
import {connect} from 'react-redux';
import {setLocale} from 'yup/lib/customLocale';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from './checkout/Checkout';
import Orders from './Orders';
import OrderDetail from './OrderDetail';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import CustomerSettings from './CustomerSettings';
import {isAnyLoading, getDaoState} from 'common/dao';
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
  static INITIAL_ROOT_NAME = 'Checkout';

  constructor(props) {
    super(props);
  }

  async componentWillMount() {
    const {dispatch, userId, client, user} = this.props;
    dispatch(customerServicesRegistrationAction(client, userId));
    dispatch(getPaymentCards(user.stripeCustomerId));
  }

  render() {
    const {match, busy, client} = this.props;

    return busy ? <LoadingScreen text="Loading Customer Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={`${match.path}/Checkout`} render={() => <Checkout client={client} {...this.props}/>}/>
          <Route path={`${match.path}/Orders`} exact render={() => <Orders client={client} {...this.props}/>}/>
          <Route path={'/Customer/OrderDetail'} exact render={() => <OrderDetail client={client} {...this.props}/>}/>
          <Route path={`${match.path}/CustomerSettings`} exact component={CustomerSettings}/>
          <Redirect to={`${match.path}/${CustomerLanding.INITIAL_ROOT_NAME}`}/>
        </Switch>
        <CustomerMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['vehicleTypeDao', 'paymentDao', 'userDao']),
  user: getDaoState(state, ['user'], 'userDao'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(CustomerLanding);


