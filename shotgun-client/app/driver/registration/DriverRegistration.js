import React, {Component} from 'react';
import {connect} from 'react-redux';
import {isAnyLoading} from 'common/dao';
import UserDetails from 'common/registration/UserDetails';
import VehicleDetails from './VehicleDetails';
import DriverRegistrationConfirmation from './DriverRegistrationConfirmation';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './DriverRegistrationInitialState';
import LoadingScreen from 'common/components/LoadingScreen';

class DriverRegistration extends Component {
  constructor() {
    super();
  }

  componentWillMount(){
    const {dispatch, client} = this.props;
    this.state = INITIAL_STATE;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client));
  }

  render() {
    const {match, busy, ...rest} = this.props;

    return busy ? <LoadingScreen text="Loading Driver Registration"/> : <Switch>
      <Route path={`${match.path}/UserDetails`} exact render={() => <UserDetails {...rest} context={this} next="VehicleDetails"/>} />
      <Route path={`${match.path}/VehicleDetails`} exact render={() => <VehicleDetails {...rest} context={this}/>} />
      <Route path={`${match.path}/DriverRegistrationConfirmation`} exact render={() => <DriverRegistrationConfirmation {...rest} context={this}/>} />
      <Redirect to={`${match.path}/DriverRegistrationConfirmation`}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['vehicleTypeDao']),
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverRegistration);

