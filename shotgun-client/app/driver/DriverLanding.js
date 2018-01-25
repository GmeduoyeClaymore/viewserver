import React, {Component} from 'react';
import {connect} from 'react-redux';
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
import LoadingScreen from 'common/components/LoadingScreen';

class DriverLanding extends Component {
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, client, userId, user} = this.props;
    dispatch(driverServicesRegistrationAction(client, userId));
    dispatch(getCurrentPosition());
    dispatch(getBankAccount(user.stripeAccountId));
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

const mapStateToProps = (state, nextOwnProps) => ({
  ...nextOwnProps,
  busy: isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao']) || isAnyOperationPending(state, [{ userDao: 'getCurrentPosition'}]),
  user: getDaoState(state, ['user'], 'userDao')
});

export default connect(mapStateToProps)(DriverLanding);


