import React from 'react';
import CustomerSettingsLanding from './CustomerSettingsLanding';
import AddressLookup from 'common/components/maps/AddressLookup';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateAddressDetails from 'common/settings/UpdateAddressDetails';
import UpdatePaymentCardDetails from './UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updateCustomer} from 'customer/actions/CustomerActions';


class CustomerSettings extends React.Component {
  constructor(props){
    super(props);
    this.onUpdate = (user) => updateCustomer(user, () => props.history.push('/Customer/Settings/CustomerSettings'));
  }
  render(){
    const {props} = this;
    const {height, width} = props;
    return <ReduxRouter height={height} width={width} defaultRoute='/Customer/Settings/CustomerSettingsLanding'>
      <Route path={'/Customer/Settings/CustomerSettingsLanding'} exact component={CustomerSettingsLanding}/>
      <Route path={'/Customer/Settings/UpdateUserDetails'} onUpdate={this.onUpdate} exact component={UpdateUserDetails} />
      <Route path={'/Customer/Settings/UpdateAddressDetails'} next={'/Customer/Settings/HomeAddressDetails'} exact component={UpdateAddressDetails}/>
      <Route path={'/Customer/Settings/UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route path={'/Customer/Settings/AddressLookup'} exact component={AddressLookup} />
    </ReduxRouter>;
  }
}
  

export default CustomerSettings;
