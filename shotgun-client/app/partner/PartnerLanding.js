import React, {Component} from 'react';
import {withExternalState, Route, ReduxRouter, Redirect} from 'custom-redux';
import PartnerMenuBar from './PartnerMenuBar';
import PartnerMyOrders from './PartnerMyOrders';
import PartnerMyOrderDetail from './PartnerMyOrderDetail';
import PartnerAvailableOrders from './PartnerAvailableOrders';
import PartnerAvailableOrderDetail from './PartnerAvailableOrderDetail';
import PartnerOrderInProgress from './PartnerOrderInProgress';
import PartnerSettings from './Settings/PartnerSettings';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import {partnerServicesRegistrationAction, getBankAccount, watchPosition} from 'partner/actions/PartnerActions';
import {isAnyLoading, getDaoState} from 'common/dao';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/orders/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/orders/CustomerOrderInProgress';
import {LoadingScreen} from 'common/components';
import Logger from 'common/Logger';
import shotgun from 'native-base-theme/variables/shotgun';

class PartnerLanding extends Component {
  constructor(props) {
    super(props);
    this.loadData = this.loadData.bind(this);
    Logger.info('Creating a new instance of partner landing');
  }

  beforeNavigateTo(){
    Logger.info('Mounting partner landing');
    this.loadData();
    const {history, path} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri));
  }

  loadData(){
    const {dispatch, client, userId, user} = this.props;
    if (!user){
      throw new Error('You shouldnt be on this page without a user specified how did this happen');
    }
    dispatch(partnerServicesRegistrationAction(client, userId));
    dispatch(customerServicesRegistrationAction(client));
    dispatch(watchPosition());
    dispatch(getBankAccount());
    dispatch(getPaymentCards());
  }

  render() {
    const stateKey = 'partner';
    const {busy, path, isLoggedIn, history} = this.props;
    const completeProps = {...this.props, stateKey, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/PartnerMyOrders/Posted`, ordersRoot: `${path}`};
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return  busy ? <LoadingScreen text="Loading"/> :
      [<ReduxRouter key='router' name="PartnerLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps} defaultRoute={'PartnerMyOrders'}>
        <Route stateKey={stateKey} path={'Checkout'} component={Checkout}/>
        <Route stateKey={stateKey} path={'PartnerAvailableOrders'} exact component={PartnerAvailableOrders}/>
        <Route stateKey={stateKey} path={'PartnerAvailableOrderDetail'} exact component={PartnerAvailableOrderDetail}/>
        <Route stateKey={stateKey} path={'PartnerMyOrders'} exact component={PartnerMyOrders}/>
        <Route stateKey={stateKey} path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route stateKey={stateKey} path={'CustomerOrderInProgress'} exact component={CustomerOrderInProgress}/>
        <Route stateKey={stateKey} path={'Orders'} exact component={PartnerMyOrders}/>
        <Route stateKey={stateKey} path={'PartnerMyOrderDetail'} exact component={PartnerMyOrderDetail}/>
        <Route stateKey={stateKey} path={'PartnerOrderInProgress'} exact component={PartnerOrderInProgress}/>
        <Route stateKey={stateKey} path={'Settings'} component={PartnerSettings}/>
        <Route stateKey={stateKey} path={'UserRelationships'} component={UserRelationships}/>
      </ReduxRouter>,
      <PartnerMenuBar key='menuBar' {...this.props}/>];
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const {match: parentMatch, isLoggedIn} = nextOwnProps;
  return {
    ...nextOwnProps,
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    busy: isLoggedIn && isAnyLoading(state, ['userDao', 'partnerDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']) || !user,
    user
  };
};

export default withExternalState(mapStateToProps)(PartnerLanding);


