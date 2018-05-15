import React, {Component} from 'react';
import {connect, ReduxRouter, Route, Redirect} from 'custom-redux';
import {setLocale} from 'yup/lib/customLocale';
import CustomerMenuBar from './CustomerMenuBar';
import Checkout from 'common/components/checkout/Checkout';
import CustomerMyOrders from './orders/CustomerMyOrders';
import CustomerOrderDetail from './orders/CustomerOrderDetail';
import {customerServicesRegistrationAction} from 'customer/actions/CustomerActions';
import {watchPosition} from 'partner/actions/PartnerActions';
import CustomerSettings from './settings/CustomerSettings';
import {isAnyLoading, getDaoState} from 'common/dao';
import {LoadingScreen} from 'common/components';
import {registerActionListener, getActionFromNotification} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import shotgun from 'native-base-theme/variables/shotgun';
import FCM from 'react-native-fcm';
import Logger from 'common/Logger';

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
    dispatch(customerServicesRegistrationAction(client));
    dispatch(watchPosition());
    const handler = (actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri);
    registerActionListener(handler);
    FCM.getInitialNotification().then(notif => {
      if (notif){
        Logger.info('Got initial notificaton: ' + JSON.stringify(notif));
        handler(getActionFromNotification(notif));
      }
    });
  }

  render() {
    const {busy, client, path, isLoggedIn, history} = this.props;
    const completeProps = {client, ...this.props, ordersRoot: path, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/Orders` };
    if (!isLoggedIn){
      return <Redirect just to="/" history={history}/>;
    }
    return busy ? <LoadingScreen text="Loading Customer landing"/> :
      [<ReduxRouter key='router' name="CustomerLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps} defaultRoute="UserRelationships" /*defaultRoute={{pathname: 'CustomerOrderDetail', state: {orderId: '2c2f5e22-54f2-4464-8d25-5b0a0dcc2ec9'}}}*/>
        <Route path={'Checkout'} component={Checkout}/>
        <Route path={'CustomerMyOrders'} exact component={CustomerMyOrders}/>
        <Route path={'Orders'} exact component={CustomerMyOrders}/>
        <Route path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route path={'Settings'} parentPath={path} component={CustomerSettings}/>
        <Route path={'UserRelationships'} component={UserRelationships} showAll={true}/>
      </ReduxRouter>,
      <CustomerMenuBar key='menuBar' {...this.props}/>];
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    user: getDaoState(state, ['user'], 'userDao'),
    ...nextOwnProps,
    busy: isAnyLoading(state, ['paymentDao', 'userDao', 'contentTypeDao', 'singleOrderSummaryDao']),
  };
};

export default connect(mapStateToProps)(CustomerLanding);


