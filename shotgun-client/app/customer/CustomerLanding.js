import React, {Component} from 'react';
import {connect, ReduxRouter, Route, Redirect} from 'custom-redux';
import {setLocale} from 'yup/lib/customLocale';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrders from './orders/CustomerOrders';
import CustomerOrderDetail from './orders/CustomerOrderDetail';
import {customerServicesRegistrationAction} from 'customer/actions/CustomerActions';
import {watchPosition} from 'partner/actions/PartnerActions';
import CustomerSettings from './settings/CustomerSettings';
import {isAnyLoading, getDaoState} from 'common/dao';
import {LoadingScreen} from 'common/components';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
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

  beforeNavigateTo() {
    const {dispatch, client, history, path} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri));
    dispatch(customerServicesRegistrationAction(client));
    dispatch(watchPosition());
  }

  render() {
    const {busy, client, path, isLoggedIn, history} = this.props;
    const completeProps = {client, ...this.props, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/Orders` };
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return busy ? <LoadingScreen text="Loading"/> :
      [<ReduxRouter key='router' name="CustomerLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps} defaultRoute={{pathname: 'CustomerOrderDetail', state: {orderId: '2c2f5e22-54f2-4464-8d25-5b0a0dcc2ec9'}}}>
        <Route path={'Checkout'} component={Checkout}/>
        <Route path={'CustomerOrders'} exact component={CustomerOrders}/>
        <Route path={'Orders'} exact component={CustomerOrders}/>
        <Route path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route path={'Settings'} parentPath={path} component={CustomerSettings}/>
        <Route path={'UserRelationships'} component={UserRelationships}/>
      </ReduxRouter>,
      <CustomerMenuBar key='menuBar' {...this.props}/>];
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


