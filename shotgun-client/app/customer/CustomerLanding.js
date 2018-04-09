import React, {Component} from 'react';
import {connect, ReduxRouter, Route, Redirect} from 'custom-redux';
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
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {getCurrentPosition} from 'common/actions/CommonActions';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import shotgun from 'native-base-theme/variables/shotgun';
import {Dimensions} from 'react-native';
const { height, width } = Dimensions.get('window');

const contentHeight = height - shotgun.footerHeight;
const contentWidth = width;

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

  beforeNavigateTo() {
    const {dispatch, client, history, path} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri));
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
    const {busy, client, path, isLoggedIn, history} = this.props;
    const completeProps = {client, ...this.props, height: contentHeight, width: contentWidth, ordersPath: `${path}/Orders` };
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return busy ? <LoadingScreen text="Loading Customer Landing Screen"/> :
      <Container>
        <ReduxRouter  name="CustomerLandingRouter"  {...completeProps} defaultRoute={'/CustomerOrders'}>
          <Route path={'/Checkout'} component={Checkout}/>
          <Route path={'/CustomerOrders'} exact component={CustomerOrders}/>
          <Route path={'/Orders'} exact component={CustomerOrders}/>
          <Route path={'/CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
          <Route path={'/CustomerOrderInProgress'} exact component={CustomerOrderInProgress}/>
          <Route path={'/Settings'} parentPath={path} component={CustomerSettings}/>
          <Route path={'/UserRelationships'} component={UserRelationships}/>
        </ReduxRouter>
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


