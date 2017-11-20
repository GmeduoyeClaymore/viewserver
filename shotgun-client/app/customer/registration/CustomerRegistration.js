import React, {Component} from 'react';
import {StackNavigator} from 'react-navigation';
import CustomerDetails from './CustomerDetails';
import PaymentCardDetails from './PaymentCardDetails';
import AddressDetails from './AddressDetails';
import RegistrationConfirmation from './RegistrationConfirmation';

class CustomerRegistration extends Component {
  constructor(){
    super();
    this.state = {};
  }

  render() {
    const screenProps = {context: this, client: this.props.screenProps.client};

    return <CustomerRegistrationNavigator navigation={this.props.navigation}  screenProps={screenProps} />;
  }
}

const CustomerRegistrationNavigator = StackNavigator(
  {
    CustomerDetails: {screen: CustomerDetails},
    AddressDetails: {screen: AddressDetails},
    PaymentCardDetails: {screen: PaymentCardDetails},
    RegistrationConfirmation: {screen: RegistrationConfirmation},
  }, {
    initialRouteName: 'CustomerDetails',
    headerMode: 'screen'
  });

CustomerRegistration.router = CustomerRegistrationNavigator.router;

export default CustomerRegistration;
