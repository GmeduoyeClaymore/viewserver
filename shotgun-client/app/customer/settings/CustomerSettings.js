import React from 'react';
import CustomerSettingsLanding from './CustomerSettingsLanding';
import AddressLookup from 'common/components/maps/AddressLookup';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateAddressDetails from 'common/settings/UpdateAddressDetails';
import UpdatePaymentCardDetails from './UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updateCustomer} from 'customer/actions/CustomerActions';
import AddPropsToRoute from 'common/AddPropsToRoute';


class CustomerSettings extends React.Component {
  constructor(props){
    super(props);
  }
  render(){
    const {props} = this;
    const {height, width} = props;
    const onUpdate = (user) => updateCustomer(user, () => props.history.push('/Customer/Settings/CustomerSettings'));
    return <ReduxRouter height={height} width={width} defaultRoute='/Customer/Settings/CustomerSettingsLanding'>
      <Route path={'/Customer/Settings/CustomerSettingsLanding'} exact component={CustomerSettingsLanding}/>
      <Route path={'/Customer/Settings/UpdateUserDetails'} exact component={AddPropsToRoute(UpdateUserDetails, {onUpdate})} />
      <Route path={'/Customer/Settings/UpdateAddressDetails'} exact component={AddPropsToRoute(UpdateAddressDetails, {next: '/Customer/Settings/HomeAddressDetails'})}/>
      <Route path={'/Customer/Settings/UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route path={'/Customer/Settings/AddressLookup'} exact component={AddPropsToRoute(AddressLookup, props)} />
    </ReduxRouter>;
  }
}
  

export default CustomerSettings;
