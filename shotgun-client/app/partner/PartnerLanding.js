import React, {Component} from 'react';
import {connect, Route, ReduxRouter, Redirect} from 'custom-redux';
import PartnerMenuBar from './PartnerMenuBar';
import PartnerMyOrders from './PartnerMyOrders';
import PartnerOrderDetail from './PartnerOrderDetail';
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
    const {busy, path, isLoggedIn, history} = this.props;
    const completeProps = {...this.props, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/PartnerMyOrders/Posted`, ordersRoot: `${path}`};
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return  busy ? <LoadingScreen text="Loading"/> :
      [<ReduxRouter key='router' name="PartnerLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps} defaultRoute={{pathname: 'CustomerOrderDetail', state: {orderId: '7e2a0adf-04eb-4bb9-91d1-d6b1e877e1b9'}}}>
        <Route path={'Checkout'} component={Checkout}/>
        <Route path={'PartnerAvailableOrders'} exact component={PartnerAvailableOrders}/>
        <Route path={'PartnerAvailableOrderDetail'} exact component={PartnerAvailableOrderDetail}/>
        <Route path={'PartnerMyOrders'} exact component={PartnerMyOrders}/>
        <Route path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route path={'CustomerOrderInProgress'} exact component={CustomerOrderInProgress}/>
        <Route path={'Orders'} exact component={PartnerMyOrders}/>
        <Route path={'PartnerOrderDetail'} exact component={PartnerOrderDetail}/>
        <Route path={'PartnerOrderInProgress'} exact component={PartnerOrderInProgress}/>
        <Route path={'Settings'} component={PartnerSettings}/>
        <Route path={'UserRelationships'} component={UserRelationships}/>
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

export default connect(mapStateToProps)(PartnerLanding);


