import React, {Component} from 'react';
import {connect, Route, ReduxRouter} from 'custom-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverOrderDetail from './DriverOrderDetail';
import DriverOrderRequests from './DriverOrderRequests';
import DriverOrderRequestDetail from './DriverOrderRequestDetail';
import DriverOrderInProgress from './DriverOrderInProgress';
import DriverSettings from './Settings/DriverSettings';
import {customerServicesRegistrationAction, getPaymentCards} from 'customer/actions/CustomerActions';
import {driverServicesRegistrationAction, stopWatchingPosition, getBankAccount, watchPosition} from 'driver/actions/DriverActions';
import {getCurrentPosition} from 'common/actions/CommonActions';
import {isAnyLoading, isAnyOperationPending, getDaoState, isAnyUnregistered} from 'common/dao';
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/CustomerOrderInProgress';
import Logger from 'common/Logger';
import shotgun from 'native-base-theme/variables/shotgun';
import {Dimensions} from 'react-native';
const { height, width } = Dimensions.get('window');

const contentHeight = height - shotgun.footerHeight;
const contentWidth = width;

class DriverLanding extends Component {
  constructor(props) {
    super(props);
    Logger.info('Creating a new instance of driver landing');
  }

  beforeNavigateTo(){
    Logger.info('Mounting driver landing');
    DriverLanding.loadData(this.props);
    DriverLanding.attemptPaymentCards(this.props);
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(this.props.history, 'Driver', actionUri));
  }

  static oneOffDestruction(props) {
    const {dispatch} = props;
    dispatch(stopWatchingPosition());
  }

  static loadData(newProps){
    const {dispatch, client, userId, user} = newProps;
    if (!user){
      throw new Error('You shouldnt be on this page without a user specified how did this happen');
    }
    dispatch(driverServicesRegistrationAction(client, userId));
    dispatch(customerServicesRegistrationAction(client));
    dispatch(getCurrentPosition());
    dispatch(watchPosition());
    dispatch(getBankAccount());
  }

  static attemptPaymentCards(props){
    const {dispatch} = props;
    dispatch(getPaymentCards());
  }

  render() {
    const {busy, path} = this.props;
    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
      <Container>
        <ReduxRouter  name="DriverLandingRouter"  {...this.props}  height={contentHeight} width={contentWidth}   defaultRoute={'Checkout'} ordersPath={`${path}/DriverOrders/Posted`} ordersRoot={`${path}`}>
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
        </ReduxRouter>
        <DriverMenuBar {...this.props}/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const {match: parentMatch} = nextOwnProps;
  return {
    ...nextOwnProps,
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    paymentDaoReady: !!getDaoState(state, 'paymentDao'),
    awaitingDaos: isAnyUnregistered(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']),
    busy: isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']) || isAnyOperationPending(state, [{ userDao: 'getCurrentPosition'}]) || !user,
    user
  };
};

export default connect(mapStateToProps)(DriverLanding);


