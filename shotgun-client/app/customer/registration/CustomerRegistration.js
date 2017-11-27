import React, {Component} from 'react';
import CustomerDetails from './CustomerDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from './AddressDetails';
import RegistrationConfirmation from './RegistrationConfirmation';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './RegistrationInitialState';

export default class CustomerRegistration extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  render() {
    const {match} = this.props;

    return <Switch>
      <Route path={`${match.path}/CustomerDetails`} exact render={() => <CustomerDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/AddressDetails`} exact render={() => <AddressDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/PaymentCardDetails`} render={() => <PaymentCardDetails {...this.props} context={this}/>} />
      <Route path={`${match.path}/RegistrationConfirmation`} render={() => <RegistrationConfirmation {...this.props} context={this}/>} />
      <Redirect to={`${match.path}/CustomerDetails`}/>
    </Switch>;
  }
}
