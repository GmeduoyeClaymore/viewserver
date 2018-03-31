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
    const {height, width, path} = props;
    return <ReduxRouter height={height} width={width} defaultRoute={`${path}/CustomerSettingsLanding`}>
      <Route path={`${path}/CustomerSettingsLanding`} parentPath={path} exact component={CustomerSettingsLanding}/>
      <Route path={`${path}/UpdateUserDetails`} onUpdate={this.onUpdate} exact component={UpdateUserDetails} />
      <Route path={`${path}/UpdateAddressDetails`} next={`${path}/HomeAddressDetails`} exact component={UpdateAddressDetails}/>
      <Route path={`${path}/UpdatePaymentCardDetails`} exact component={UpdatePaymentCardDetails}/>
      <Route path={`${path}/AddressLookup`} exact component={AddressLookup} />
    </ReduxRouter>;
  }
}
  

export default CustomerSettings;
