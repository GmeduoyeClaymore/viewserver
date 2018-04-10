import React, {Component} from 'react';
import {connect, Route, ReduxRouter, Redirect} from 'custom-redux';
import ReactNativeModal from 'react-native-modal';
import {View, Dimensions} from 'react-native';
import {Container, Text, Spinner} from 'native-base';
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
import {isAnyLoading, isAnyOperationPending, getDaoState, isAnyUnregistered, getLoadingMessage} from 'common/dao';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/CustomerOrderInProgress';
import Logger from 'common/Logger';
import shotgun from 'native-base-theme/variables/shotgun';
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
    const {history, path} = this.props;
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(history, path, actionUri));
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
    const {busy, path, loadingMessage, isLoggedIn, history} = this.props;
    if (!isLoggedIn){
      <Redirect just to="/" history={history}/>;
    }
    return  <Container>
      <ReactNativeModal
        isVisible={busy}
        backdropOpacity={0.4}>
        <View style={styles.modalContainer}>
          <View style={styles.innerContainer}>
            <Spinner/>
            <Text>{`Loading Driver Landing Screen\n${loadingMessage} `}</Text>
          </View>
        </View>
      </ReactNativeModal>
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
  const {match: parentMatch, isLoggedIn} = nextOwnProps;
  return {
    ...nextOwnProps,
    parentMatch,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    paymentDaoReady: !!getDaoState(state, 'paymentDao'),
    awaitingDaos: isAnyUnregistered(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']),
    busy: isLoggedIn && isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']) || isAnyOperationPending(state, [{ userDao: 'getCurrentPosition'}]) || !user,
    loadingMessage: getLoadingMessage(state, [{ 'userDao': 'Loading user data' }, {'driverDao': 'Loading driver data' }, {'vehicleDao': 'Loading vehicle data'}, {'paymentDao': 'Loading payment data'}, {'contentTypeDao': 'Loading content type data'}]),
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


export default connect(mapStateToProps)(DriverLanding);


