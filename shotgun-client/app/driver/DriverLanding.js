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


class TestComponent extends React.Component{
  constructor(props){
    super(props);
  }

  componentWillMount(){
    this.log('Component mounting');
  }

  render(){
    this.log('Component rendering');
    return <Text>Test Component</Text>;
  }

  componentWillUnmount(){
    this.log('Component unmounting');
  }

  log(message){
    Logger.info('TestComponent-' + message);
  }
}

class DriverLanding extends Component {
  constructor(props) {
    super(props);
    Logger.info('Creating a new instance of driver landing');
  }

  static oneOffInitialization(props){
    Logger.info('Mounting driver landing');
    DriverLanding.loadData(props);
    DriverLanding.attemptPaymentCards(props);
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(props.history, 'Driver', actionUri));
  }

  static oneOffDestruction(props) {
    const {dispatch} = props;
    dispatch(stopWatchingPosition());
  }

  static loadData(newProps){
    const {dispatch, client, userId, user} = newProps;
    if (user){
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
        <ReduxRouter  {...this.props}  height={contentHeight} width={contentWidth}   defaultRoute={`${path}/DriverOrderRequests`} ordersPath={`${path}/DriverOrders/Posted`}>
          <Route path={`${path}/Checkout`} component={Checkout}/>
          <Route path={`${path}/DriverOrderRequests`} exact component={DriverOrderRequests}/>
          <Route path={`${path}/DriverOrderRequestDetail`} exact component={DriverOrderRequestDetail}/>
          <Route path={`${path}/DriverOrders`} exact component={DriverOrders}/>
          <Route path={`${path}/CustomerOrderDetail`} exact component={CustomerOrderDetail}/>
          <Route path={`${path}/CustomerOrderInProgress`} exact component={CustomerOrderInProgress}/>
          <Route path={`${path}/Orders`} exact component={DriverOrders}/>
          <Route path={`${path}/DriverOrderDetail`} exact component={DriverOrderDetail}/>
          <Route path={`${path}/DriverOrderInProgress`} exact component={DriverOrderInProgress}/>
          <Route path={`${path}/Settings`} component={DriverSettings}/>
          <Route path={`${path}/UserRelationships`} component={UserRelationships}/>
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


