import React, {Component} from 'react';
import {connect} from 'react-redux';
import {setLocale} from 'yup/lib/customLocale';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from './checkout/Checkout';
import CustomerOrders from './CustomerOrders';
import CustomerOrderDetail from './CustomerOrderDetail';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import CustomerSettings from './CustomerSettings';
import {isAnyLoading, getDaoState} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';
import {getCurrentPosition} from 'common/actions/CommonActions';
import shotgun from 'native-base-theme/variables/shotgun';

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
  constructor(props) {
    super(props);
  }

  async componentWillMount() {
    const {dispatch, userId, client} = this.props;
    dispatch(customerServicesRegistrationAction(client, userId));
    this.attemptPaymentCards(this.props);
    dispatch(getCurrentPosition());
  }

  componentWillReceiveProps(props){
    this.attemptPaymentCards(props);
  }

  attemptPaymentCards(props){
    const {dispatch, user} = props;
    if (!this.paymentCardsRequested && user){
      dispatch(getPaymentCards(user.stripeCustomerId));
      this.paymentCardsRequested = true;
    }
  }

  render() {
    const {busy, client} = this.props;

    return busy ? <LoadingScreen text="Loading Customer Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={'/Customer/Checkout'} render={() => <Checkout client={client} {...this.props}/>}/>
          <Route path={'/Customer/CustomerOrders'} exact render={() => <CustomerOrders client={client} {...this.props}/>}/>
          <Route path={'/Customer/CustomerOrderDetail'} exact render={() => <CustomerOrderDetail client={client} {...this.props}/>}/>
          <Route path={'/Customer/CustomerSettings'} exact component={CustomerSettings}/>
          <Redirect to={'/Customer/Checkout'}/>
        </Switch>
        <CustomerMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['paymentDao', 'userDao']),
  user: getDaoState(state, ['user'], 'userDao'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(CustomerLanding);


