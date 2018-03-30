import React, {Component} from 'react';
import CustomerRegistrationLanding from 'customer/registration/CustomerRegistrationLanding';
import UserDetails from 'common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from 'common/registration/AddressDetails';
import CustomerLogin from 'customer/registration/CustomerLogin';
import CustomerDao from 'customer/dao/CustomerDao';
import {unregisterAllDaos, registerNakedDao} from 'common/actions/CommonActions';
import {Route, ReduxRouter, withExternalState} from 'custom-redux';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';
import AddressLookup from 'common/components/maps/AddressLookup';
import AddPropsToRoute from 'common/AddPropsToRoute';

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
    const {parentMatch: match} = this.props;
    const basePath = match.path;
    const registrationProps = {...this.props, stateKey: CustomerRegistration.stateKey};
    return <ReduxRouter>
      <Route path={`${basePath}/Login`} exact component={AddPropsToRoute(CustomerLogin, registrationProps)}/>
      <Route path={`${basePath}/UserDetails`} exact component={AddPropsToRoute(UserDetails, {...registrationProps, next: `${basePath}/AddressDetails`})}/>
      <Route path={`${basePath}/AddressDetails`} exact component={AddPropsToRoute(AddressDetails, {...registrationProps, next: `${basePath}/PaymentCardDetails`})}/>
      <Route path={`${basePath}/AddressLookup`} exact component={AddPropsToRoute(AddressLookup, registrationProps)}/>
      <Route path={`${basePath}/PaymentCardDetails`} exact component={AddPropsToRoute(PaymentCardDetails, registrationProps)}/>
      <Route path={`${basePath}`} exact component={AddPropsToRoute(CustomerRegistrationLanding, registrationProps)}/>
    </ReduxRouter>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch, history: parentHistory, ...rest} = nextOwnProps;
  return {
    parentMatch,
    parentHistory,
    ...rest
  };
};


export default withExternalState(mapStateToProps)(CustomerRegistration);
