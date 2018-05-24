import React, {Component} from 'react';
import {withExternalState, Route, ReduxRouter, Redirect} from 'custom-redux';
import PartnerMenuBar from './PartnerMenuBar';
import PartnerMyOrders from './PartnerMyOrders';
import PartnerOrderDetail from './PartnerOrderDetail';
import PartnerAvailableOrders from './PartnerAvailableOrders';
import PartnerSettings from './Settings/PartnerSettings';
import {customerServicesRegistrationAction} from 'customer/actions/CustomerActions';
import {partnerServicesRegistrationAction, watchPosition} from 'partner/actions/PartnerActions';
import {isAnyLoading, getDaoState, getDao} from 'common/dao';
import {registerActionListener, getActionFromNotification} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import CustomerOrderDetail from 'customer/orders/CustomerOrderDetail';
import Checkout from 'common/components/checkout/Checkout';
import UserDetail from 'common/components/relationships/UserDetail';
import {LoadingScreen} from 'common/components';
import Logger from 'common/Logger';
import shotgun from 'native-base-theme/variables/shotgun';
import FCM from 'react-native-fcm';
import ReactNativeModal from 'react-native-modal';
import {View, Spinner, Text} from 'native-base';

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
    const handler = (actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri);
    registerActionListener(handler);
    FCM.getInitialNotification().then(notif => {
      if (notif){
        Logger.info('Got initial notificaton: ' + JSON.stringify(notif));
        handler(getActionFromNotification(notif));
      }
    });
  }

  loadData(){
    const {dispatch, client, userId, user} = this.props;
    if (!user){
      throw new Error('You shouldnt be on this page without a user specified how did this happen');
    }
    dispatch(partnerServicesRegistrationAction(client, userId));
    dispatch(customerServicesRegistrationAction(client));
    dispatch(watchPosition());
  }

  render() {
    const {busy, path, isLoggedIn, isConnected, history, daosHaveBeenRegistered} = this.props;
    const completeProps = {...this.props, height: shotgun.contentHeight, width: shotgun.deviceWidth, ordersPath: `${path}/PartnerMyOrders/Posted`, ordersRoot: `${path}`};

    if (!isLoggedIn && isConnected){
      return <Redirect just to="/" history={history}/>;
    }

    return !daosHaveBeenRegistered ? <LoadingScreen text="Loading Partner Landing Screen ..."/> :
      [<ReactNativeModal key='connectingModal'
        isVisible={busy}
        backdropOpacity={0.4}>
        <View style={styles.modalContainer}>
          <View style={styles.innerContainer}>
            <Spinner/>
            <Text>Loading Partner Landing Screen ...</Text>
          </View>
        </View>
      </ReactNativeModal>,
      <ReduxRouter key='router' name="PartnerLandingRouter" resizeForKeyboard={true} hasFooter={true} {...completeProps}  defaultRoute="PartnerAvailableOrders"  /*defaultRoute={{pathname: 'PartnerOrderDetail', state: {orderId: 'f75c1d96-cc7b-41ef-a86c-be7381307c83'}}}*/>
        <Route path={'Checkout'} component={Checkout}/>
        <Route path={'PartnerAvailableOrders'} exact component={PartnerAvailableOrders}/>
        <Route path={'PartnerOrderDetail'} exact component={PartnerOrderDetail}/>
        <Route path={'PartnerMyOrders'} exact component={PartnerMyOrders}/>
        <Route path={'Orders'} exact component={PartnerMyOrders}/>
        <Route path={'CustomerOrderDetail'} exact component={CustomerOrderDetail}/>
        <Route path={'UserDetail'} exact component={UserDetail}/>
        <Route path={'Settings'} component={PartnerSettings}/>
        <Route path={'UserRelationships'} component={UserRelationships} justFriends={true}/>
      </ReduxRouter>,
      <PartnerMenuBar key='menuBar' {...this.props}/>];
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const {match: parentMatch, isLoggedIn} = nextOwnProps;
  const daosHaveBeenRegistered =  getDao(state, 'partnerOrderResponseDao') && getDao(state, 'orderSummaryDao') && getDao(state, 'userDao')  && getDao(state, 'contentTypeDao');
  return {
    ...nextOwnProps,
    daosHaveBeenRegistered,
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    busy: isLoggedIn && isAnyLoading(state, ['userDao', 'partnerDao', 'paymentDao', 'contentTypeDao']) || !user,
    user
  };
};

const styles = {
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  innerContainer: {
    alignItems: 'center',
  },
};

export default withExternalState(mapStateToProps)(PartnerLanding);


