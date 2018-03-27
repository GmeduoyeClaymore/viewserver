import React, {Component} from 'react';
import CustomerRegistrationLanding from 'customer/registration/CustomerRegistrationLanding';
import UserDetails from 'common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from 'common/registration/AddressDetails';
import CustomerLogin from 'customer/registration/CustomerLogin';
import CustomerDao from 'customer/dao/CustomerDao';
import {unregisterAllDaos, registerNakedDao} from 'common/actions/CommonActions';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';
import AddressLookup from 'common/components/maps/AddressLookup';
import { withRouter } from 'react-router';
import { withExternalState } from 'custom-redux';

class CustomerRegistration extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'customerRegistration';
  constructor(props) {
    super(props);
  }

  componentDidMount(){
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    registerNakedDao(dispatch, new CustomerDao(client));
  }

  render() {
    const registrationProps = {...this.props, stateKey: CustomerRegistration.stateKey};
    return <Switch>
      <Route path={'/Customer/Registration/CustomerRegistrationLanding'} exact render={() => <CustomerRegistrationLanding {...registrationProps}/>} />
      <Route path={'/Customer/Registration/Login'} exact render={() => <CustomerLogin {...registrationProps}/>} />
      <Route path={'/Customer/Registration/UserDetails'} exact render={() => <UserDetails {...registrationProps} next="/Customer/Registration/AddressDetails"/>} />
      <Route path={'/Customer/Registration/AddressDetails'} exact render={() => <AddressDetails {...registrationProps} next="/Customer/Registration/PaymentCardDetails"/>} />
      <Route path={'/Customer/Registration/AddressLookup'} exact render={() => <AddressLookup {...registrationProps}/>} />
      <Route path={'/Customer/Registration/PaymentCardDetails'} exact render={() => <PaymentCardDetails {...registrationProps}/>} />
      <Redirect to={'/Customer/Registration/CustomerRegistrationLanding'}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    ...nextOwnProps
  };
};


export default withRouter(withExternalState(mapStateToProps)(CustomerRegistration));
