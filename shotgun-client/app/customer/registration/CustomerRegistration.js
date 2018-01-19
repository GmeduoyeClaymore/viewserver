import React, {Component} from 'react';
import CustomerRegistrationLanding from 'customer/registration/CustomerRegistrationLanding';
import UserDetails from 'common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from 'common/registration/AddressDetails';
import CustomerLogin from 'customer/registration/CustomerLogin';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';
import AddressLookup from 'common/components/maps/AddressLookup';

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
      <Route path={'/Customer/Registration/Login'} exact render={() => <CustomerLogin {...this.props} context={this}/>} />
      <Route path={'/Customer/Registration/UserDetails'} exact render={() => <UserDetails {...this.props} context={this} next="/Customer/RegistrationAddressDetails"/>} />
      <Route path={'/Customer/Registration/AddressDetails'} exact render={() => <AddressDetails {...this.props} context={this} next="/Customer/Registration/PaymentCardDetails"/>} />
      <Route path={'/Customer/Registration/AddressLookup'} exact render={() => <AddressLookup {...this.props}/>} />
      <Route path={'/Customer/Registration/PaymentCardDetails'} exact render={() => <PaymentCardDetails {...this.props} context={this}/>} />
      <Redirect to={'/Customer/Registration/CustomerRegistrationLanding'}/>
    </Switch>;
  }
}
