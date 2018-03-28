import React, {Component} from 'react';
import {connect} from 'custom-redux';
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
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';
import UserRelationships from 'common/components/relationships/UserRelationships';
import Checkout from 'common/components/checkout/Checkout';
import CustomerOrderDetail from 'customer/CustomerOrderDetail';
import CustomerOrderInProgress from 'customer/CustomerOrderInProgress';
import { withRouter } from 'react-router';

class DriverLanding extends Component {
  constructor(props) {
    super(props);
    this.hasLoadedData = false;
  }

  componentDidMount(){
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
    const {busy, client} = this.props;
    const ordersRoot = () => <DriverOrders client={client} {...this.props}/>;

    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={'/Driver/Checkout'} render={() => <Checkout client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderRequests'} exact render={() => <DriverOrderRequests client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderRequestDetail'} exact render={() => <DriverOrderRequestDetail client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrders'} exact render={ordersRoot}/>
          <Route path={'/Driver/CustomerOrderDetail'} exact render={() => <CustomerOrderDetail client={client} {...this.props}/>}/>
          <Route path={'/Driver/CustomerOrderInProgress'} exact render={() => <CustomerOrderInProgress client={client} {...this.props}/>}/>
          <Route path={'/Driver/Orders'} exact render={ordersRoot}/>
          <Route path={'/Driver/DriverOrderDetail'} exact render={() => <DriverOrderDetail client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderInProgress'} exact render={() => <DriverOrderInProgress client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderInProgress'} exact render={() => <DriverOrderInProgress client={client} {...this.props}/>}/>
          <Route path={'/Driver/Settings'} render={() => <DriverSettings client={client} {...this.props}/>}/>
          <Route path={'/Driver/UserRelationships'} render={() => <UserRelationships client={client} {...this.props}/>}/>
          <Redirect to={'/Driver/DriverOrderRequests'}/>
        </Switch>
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

export default withRouter(connect(mapStateToProps)(DriverLanding));


