import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {setLocale} from 'yup/lib/customLocale';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrders from './CustomerOrders';
import CustomerOrderDetail from './CustomerOrderDetail';
import CustomerOrderInProgress from './CustomerOrderInProgress';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import {watchPosition} from 'driver/actions/DriverActions';
import CustomerSettings from './settings/CustomerSettings';
import {isAnyLoading, getDaoState} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {getCurrentPosition} from 'common/actions/CommonActions';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import AddPropsToRoute from 'common/AddPropsToRoute';

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

  async componentDidMount() {
    const {dispatch, client, history} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, 'Customer', actionUri));
    dispatch(customerServicesRegistrationAction(client));
    this.attemptPaymentCards(this.props);
    dispatch(getCurrentPosition());
    dispatch(watchPosition());
  }

  componentWillReceiveProps(props){
    this.attemptPaymentCards(props);
  }

  attemptPaymentCards(props){
    const {dispatch, user} = props;
    if (!this.paymentCardsRequested && user){
      dispatch(getPaymentCards());
      this.paymentCardsRequested = true;
    }
  }

  render() {
    const {busy, client} = this.props;
    const completeProps = {client, ...this.props};
    return busy ? <LoadingScreen text="Loading Customer Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={'/Customer/Checkout'} component={AddPropsToRoute(Checkout, completeProps)}/>
          <Route path={'/Customer/CustomerOrders'} exact component={AddPropsToRoute(CustomerOrders, completeProps)}/>
          <Route path={'/Customer/Orders'} exact component={AddPropsToRoute(CustomerOrders, completeProps)}/>
          <Route path={'/Customer/CustomerOrderDetail'} exact component={AddPropsToRoute(CustomerOrderDetail, completeProps)}/>
          <Route path={'/Customer/CustomerOrderInProgress'} exact component={AddPropsToRoute(CustomerOrderInProgress, completeProps)}/>
          <Route path={'/Customer/Settings'} component={AddPropsToRoute(CustomerSettings, completeProps)}/>
          <Route path={'/Customer/UserRelationships'} component={AddPropsToRoute(UserRelationships, completeProps)}/>
          <Redirect to={'/Customer/Checkout'}/>
        </Switch>
        <CustomerMenuBar {...this.props}/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    busy: isAnyLoading(state, ['paymentDao', 'userDao', 'contentTypeDao']),
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    user: getDaoState(state, ['user'], 'userDao'),
    ...nextOwnProps
  };
};

export default connect(mapStateToProps)(CustomerLanding);


