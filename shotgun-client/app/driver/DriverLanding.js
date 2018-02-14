import React, {Component} from 'react';
import {connect} from 'custom-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverOrderDetail from './DriverOrderDetail';
import DriverOrderRequests from './DriverOrderRequests';
import DriverOrderRequestDetail from './DriverOrderRequestDetail';
import DriverOrderInProgress from './DriverOrderInProgress';
import DriverSettings from './Settings/DriverSettings';
import {driverServicesRegistrationAction, stopWatchingPosition, getBankAccount} from 'driver/actions/DriverActions';
import {getCurrentPosition} from 'common/actions/CommonActions';
import {isAnyLoading, isAnyOperationPending, getDaoState} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import {LoadingScreen} from 'common/components';
import {registerActionListener} from 'common/Listeners';
import NotificationActionHandlerService from 'common/services/NotificationActionHandlerService';

class DriverLanding extends Component {
  constructor(props) {
    super(props);
    this.hasLoadedData = false;
  }

  componentWillMount(){
    this.loadData(this.props);
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
        dispatch(getCurrentPosition());
        dispatch(getBankAccount());
        this.hasLoadedData = true;
      }
    }
  }

  componentWillUnmount() {
    const {dispatch} = this.props;
    dispatch(stopWatchingPosition());
  }

  render() {
    const {busy, client} = this.props;

    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={'/Driver/DriverOrderRequests'} exact render={() => <DriverOrderRequests client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderRequestDetail'} exact render={() => <DriverOrderRequestDetail client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrders'} exact render={() => <DriverOrders client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderDetail'} exact render={() => <DriverOrderDetail client={client} {...this.props}/>}/>
          <Route path={'/Driver/DriverOrderInProgress'} exact render={() => <DriverOrderInProgress client={client} {...this.props}/>}/>
          <Route path={'/Driver/Settings'} render={() => <DriverSettings client={client} {...this.props}/>}/>
          <Redirect to={'/Driver/DriverOrders'}/>
        </Switch>
        <DriverMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  return {
    ...nextOwnProps,
    busy: isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao']) || isAnyOperationPending(state, [{ userDao: 'getCurrentPosition'}]) || !user,
    user
  };
};

export default connect(mapStateToProps)(DriverLanding);


