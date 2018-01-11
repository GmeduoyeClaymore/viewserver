import React, {Component} from 'react';
import CustomerRegistrationLanding from 'customer/registration/CustomerRegistrationLanding';
import UserDetails from 'common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from 'customer/registration/AddressDetails';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';

export default class CustomerRegistration extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  componentWillMount(){
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client));
  }

  render() {
    return <Switch>
      <Route path={'/Customer/Registration/CustomerRegistrationLanding'} exact render={() => <CustomerRegistrationLanding {...this.props} context={this}/>} />
      <Route path={'/Customer/Registration/UserDetails'} exact render={() => <UserDetails {...this.props} context={this} next="AddressDetails"/>} />
      <Route path={'/Customer/Registration/AddressDetails'} exact render={() => <AddressDetails {...this.props} context={this}/>} />
      <Route path={'/Customer/Registration/PaymentCardDetails'} exact render={() => <PaymentCardDetails {...this.props} context={this}/>} />
      <Redirect to={'/Customer/Registration/UserDetails'}/>
    </Switch>;
  }
}
