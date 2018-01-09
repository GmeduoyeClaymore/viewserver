import React, {Component} from 'react';
import {connect} from 'react-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverOrderDetail from './DriverOrderDetail';
import DriverOrderRequests from './DriverOrderRequests';
import DriverOrderRequestDetail from './DriverOrderRequestDetail';
import DriverOrderInProgress from './DriverOrderInProgress';
import DriverSettings from './DriverSettings';
import {driverServicesRegistrationAction} from 'driver/actions/DriverActions';
import {getCurrentPosition} from 'common/actions/CommonActions';
import {isAnyLoading, isAnyOperationPending} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';

class DriverLanding extends Component {
  static INITIAL_ROOT_NAME = 'DriverOrderRequests';

  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, client, userId} = this.props;
    dispatch(driverServicesRegistrationAction(client, userId));
    dispatch(getCurrentPosition());
  }

  render() {
    const {match, busy, client} = this.props;

    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
      <Container>
        <Switch>
          <Route path={`${match.path}/DriverOrderRequests`} exact render={() => <DriverOrderRequests client={client} {...this.props}/>}/>
          <Route path={`${match.path}/DriverOrderRequestDetail`} exact render={() => <DriverOrderRequestDetail client={client} {...this.props}/>}/>
          <Route path={`${match.path}/DriverOrders`} exact render={() => <DriverOrders client={client} {...this.props}/>}/>
          <Route path={`${match.path}/DriverOrderDetail`} exact render={() => <DriverOrderDetail client={client} {...this.props}/>}/>
          <Route path={`${match.path}/DriverOrderInProgress`} exact render={() => <DriverOrderInProgress client={client} {...this.props}/>}/>
          <Route path={`${match.path}/DriverSettings`} exact component={DriverSettings} />}/>
          <Redirect to={`${match.path}/${DriverLanding.INITIAL_ROOT_NAME}`}/>
        </Switch>
        <DriverMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['userDao', 'driverDao', 'vehicleDao']) || isAnyOperationPending(state, { userDao: 'getCurrentPosition'}),
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverLanding);


