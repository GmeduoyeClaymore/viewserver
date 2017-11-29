import React, {Component} from 'react';
import {connect} from 'react-redux';
import {isAnyLoading} from 'common/dao';
import UserDetails from 'common/registration/UserDetails';
import VehicleDetails from './VehicleDetails';
import DriverRegistrationConfirmation from './DriverRegistrationConfirmation';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {Text, Content} from 'native-base';
import {INITIAL_STATE} from './DriverRegistrationInitialState';

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
    const {match, busy} = this.props;

    return busy ? <Content><Text>Loading Driver Registration.....</Text></Content> : <Switch>
      <Route path={`${match.path}/UserDetails`} exact render={() => <UserDetails {...this.props} context={this} next="VehicleDetails"/>} />
      <Route path={`${match.path}/VehicleDetails`} exact render={() => <VehicleDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/DriverRegistrationConfirmation`} exact render={() => <DriverRegistrationConfirmation {...this.props} context={this}/>} />
      <Redirect to={`${match.path}/UserDetails`}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['vehicleTypeDao']),
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverRegistration);

