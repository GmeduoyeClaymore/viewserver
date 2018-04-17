import React, {Component} from 'react';
import CustomerRegistrationLanding from 'customer/registration/CustomerRegistrationLanding';
import UserDetails from 'common/registration/UserDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from 'common/registration/AddressDetails';
import CustomerLogin from 'customer/registration/CustomerLogin';
import CustomerDao from 'customer/dao/CustomerDao';
import {registerNakedDao} from 'common/actions/CommonActions';
import {Route, ReduxRouter, withExternalState} from 'custom-redux';
import {INITIAL_STATE} from './CustomerRegistrationInitialState';
import AddressLookup from 'common/components/maps/AddressLookup';

class CustomerRegistration extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'customerRegistration';
  constructor(props) {
    super(props);
  }

  componentDidMount(){
    const {dispatch, client} = this.props;
    registerNakedDao(dispatch, new CustomerDao(client));
  }

  render() {
    const {path} = this.props;
    const registrationProps = {...this.props, stateKey: CustomerRegistration.stateKey};
    return <ReduxRouter  name="CustomerRegistrationRouter" resizeForKeyboard={true} {...registrationProps} defaultRoute={'RegistrationLanding'}>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'RegistrationLanding'} exact component={CustomerRegistrationLanding}/>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'Login'} exact component={CustomerLogin}/>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'UserDetails'} exact component={UserDetails} next={`${path}/AddressDetails`}/>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'AddressDetails'} exact component={AddressDetails} next={`${path}/PaymentCardDetails`}/>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'AddressLookup'} exact component={AddressLookup}/>
      <Route stateKey={CustomerRegistration.stateKey} transition='left' path={'PaymentCardDetails'} exact component={PaymentCardDetails}/>
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
