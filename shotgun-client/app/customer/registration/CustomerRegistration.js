import React, {Component} from 'react';
import UserDetails from '../../common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from '../../common/registration/AddressDetails';
import RegistrationConfirmation from './CustomerRegistrationConfirmation';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';
import {Text, Content} from 'native-base';

export default class CustomerRegistration extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  render() {
    const {match, busy} = this.props;

    return busy ? <Content><Text>Loading Driver Registration.....</Text></Content> : <Switch>
      <Route path={`${match.path}/UserDetails`} exact render={() => <UserDetails {...this.props} context={this} next="AddressDetails"/>} />
      <Route path={`${match.path}/AddressDetails`} exact render={() => <AddressDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/PaymentCardDetails`} exact render={() => <PaymentCardDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/RegistrationConfirmation`} exact render={() => <RegistrationConfirmation {...this.props} context={this}/>} />
      <Redirect to={`${match.path}/UserDetails`}/>
    </Switch>;
  }
}
