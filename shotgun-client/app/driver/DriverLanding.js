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
import {isAnyLoading, isAnyOperationPending, getDaoState} from 'common/dao';
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/CustomerOrderInProgress';
import AddPropsToRoute from 'common/AddPropsToRoute';
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
    this.hasLoadedData = false;
  }

  componentDidMount(){
    Logger.info('Mounting driver landing');
    this.loadData(this.props);
    this.attemptPaymentCards(this.props);
    registerActionListener((actionUri) => NotificationActionHandlerService.handleAction(this.props.history, 'Driver', actionUri));
  }

  componentWillReceiveProps(newProps){
    this.loadData(newProps);
  }

  loadData(newProps){
    if (!this.hasLoadedData ){
      const {dispatch, client, userId, user} = newProps;
      if (user){
        dispatch(driverServicesRegistrationAction(client, userId));
        dispatch(customerServicesRegistrationAction(client));
        dispatch(getCurrentPosition());
        dispatch(watchPosition());
        dispatch(getBankAccount());
        this.hasLoadedData = true;
      }
    }
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

  componentWillUnmount() {
    const {dispatch} = this.props;
    dispatch(stopWatchingPosition());
  }

  render() {
    const {busy} = this.props;
    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
      <Container>
        <ReduxRouter height={contentHeight} width={contentWidth}  defaultRoute="/Driver/DriverOrderRequests">
          <Route path={'/Driver/Checkout'} component={AddPropsToRoute(Checkout, this.props)}/>
          <Route path={'/Driver/DriverOrderRequests'} exact component={AddPropsToRoute(DriverOrderRequests, this.props)}/>
          <Route path={'/Driver/DriverOrderRequestDetail'} exact component={AddPropsToRoute(DriverOrderRequestDetail, this.props)}/>
          <Route path={'/Driver/DriverOrders'} exact component={AddPropsToRoute(DriverOrders, this.props)}/>
          <Route path={'/Driver/CustomerOrderDetail'} exact component={AddPropsToRoute(CustomerOrderDetail, this.props)}/>
          <Route path={'/Driver/CustomerOrderInProgress'} exact component={AddPropsToRoute(CustomerOrderInProgress, this.props)}/>
          <Route path={'/Driver/Orders'} exact component={AddPropsToRoute(DriverOrders, this.props)}/>
          <Route path={'/Driver/DriverOrderDetail'} exact component={AddPropsToRoute(DriverOrderDetail, this.props)}/>
          <Route path={'/Driver/DriverOrderInProgress'} exact component={AddPropsToRoute(DriverOrderInProgress, this.props)}/>
          <Route path={'/Driver/DriverOrderInProgress'} exact component={AddPropsToRoute(DriverOrderInProgress, this.props)}/>
          <Route path={'/Driver/Settings'} component={AddPropsToRoute(DriverSettings, this.props)}/>
          <Route path={'/Driver/UserRelationships'} component={AddPropsToRoute(UserRelationships, this.props)}/>
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
    busy: isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao', 'paymentDao', 'contentTypeDao']) || isAnyOperationPending(state, [{ userDao: 'getCurrentPosition'}]) || !user,
    user
  };
};

export default connect(mapStateToProps)(DriverLanding);


