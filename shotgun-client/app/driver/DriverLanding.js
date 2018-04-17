import React, {Component} from 'react';
import {connect, Route, ReduxRouter, Redirect} from 'custom-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverOrderDetail from './DriverOrderDetail';
import DriverOrderRequests from './DriverOrderRequests';
import DriverOrderRequestDetail from './DriverOrderRequestDetail';
import DriverOrderInProgress from './DriverOrderInProgress';
import DriverSettings from './Settings/DriverSettings';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import {driverServicesRegistrationAction, getBankAccount, watchPosition} from 'driver/actions/DriverActions';
import {isAnyLoading, getDaoState} from 'common/dao';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/CustomerOrderInProgress';
import {LoadingScreen} from 'common/components';
import Logger from 'common/Logger';
import shotgun from 'native-base-theme/variables/shotgun';

class DriverLanding extends Component {
  constructor(props) {
    super(props);
    this.loadData = this.loadData.bind(this);
    Logger.info('Creating a new instance of driver landing');
  }

  beforeNavigateTo(){
    Logger.info('Mounting driver landing');
    this.loadData();
    const {history, path} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri));
  }

  loadData(){
    const {dispatch, client, userId, user} = this.props;
    if (!user){
      throw new Error('You shouldnt be on this page without a user specified how did this happen');
    }
    dispatch(driverServicesRegistrationAction(client, userId));
    dispatch(customerServicesRegistrationAction(client));
    dispatch(watchPosition());
    dispatch(getBankAccount());
    dispatch(getPaymentCards());
  }

  render() {
    const {busy, path, isLoggedIn, history} = this.props;
    const completeProps = {...this.props, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/DriverOrders/Posted`, ordersRoot: `${path}`};
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return  busy ? <LoadingScreen text="Loading"/> :
      [<ReduxRouter key='router' name="DriverLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps} defaultRoute={'Checkout'}>
        <Route path={'Checkout'} component={Checkout}/>
        <Route path={'DriverOrderRequests'} exact component={DriverOrderRequests}/>
        <Route path={'DriverOrderRequestDetail'} exact component={DriverOrderRequestDetail}/>
        <Route path={'DriverOrders'} exact component={DriverOrders}/>
        <Route path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route path={'CustomerOrderInProgress'} exact component={CustomerOrderInProgress}/>
        <Route path={'Orders'} exact component={DriverOrders}/>
        <Route path={'DriverOrderDetail'} exact component={DriverOrderDetail}/>
        <Route path={'DriverOrderInProgress'} exact component={DriverOrderInProgress}/>
        <Route path={'Settings'} component={DriverSettings}/>
        <Route path={'UserRelationships'} component={UserRelationships}/>
      </ReduxRouter>,
      <DriverMenuBar key='menuBar' {...this.props}/>];
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const {match: parentMatch, isLoggedIn} = nextOwnProps;
  return {
    ...nextOwnProps,
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    busy: isLoggedIn && isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']) || !user,
    user
  };
};

export default connect(mapStateToProps)(DriverLanding);


