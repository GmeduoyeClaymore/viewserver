import React, {Component} from 'react';
import {connect} from 'react-redux';
import UserDetails from 'common/registration/UserDetails';
import VehicleDetails from './VehicleDetails';
import OffloadDetails from './OffloadDetails';
import DriverRegistrationLanding from './DriverRegistrationLanding';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './DriverRegistrationInitialState';

class DriverRegistration extends Component {
  constructor() {
    super();
    this.state = INITIAL_STATE;
  }

  componentWillMount(){
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client));
  }

  render() {
    return <Switch>
      <Route path={'/Driver/Registration/DriverRegistrationLanding'} exact render={() => <DriverRegistrationLanding {...this.props} context={this}/>} />
      <Route path={'/Driver/Registration/UserDetails'} exact render={() => <UserDetails {...this.props} context={this} next="VehicleDetails"/>} />
      <Route path={'/Driver/Registration/VehicleDetails'} exact render={() => <VehicleDetails {...this.props} context={this}/>} />
      <Route path={'/Driver/Registration/OffloadDetails'} exact render={() => <OffloadDetails {...this.props} context={this}/>} />
      <Redirect to={'/Driver/Registration/VehicleDetails'}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverRegistration);

