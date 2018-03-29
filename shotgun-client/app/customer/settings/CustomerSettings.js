import React from 'react';
import CustomerSettingsLanding from './CustomerSettingsLanding';
import AddressLookup from 'common/components/maps/AddressLookup';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateAddressDetails from 'common/settings/UpdateAddressDetails';
import UpdatePaymentCardDetails from './UpdatePaymentCardDetails';
import {Route, Redirect, Switch} from 'react-router-native';
import {updateCustomer} from 'customer/actions/CustomerActions';
import AddPropsToRoute from 'common/AddPropsToRoute';


class CustomerSettings extends React.Component {
  constructor(props){
    super(props);
  }
  render(){
    const {props} = this;
    const onUpdate = (user) => updateCustomer(user, () => props.history.push('/Customer/Settings/CustomerSettings'));
    return <Switch>
      <Route path={'/Customer/Settings/CustomerSettingsLanding'} exact component={CustomerSettingsLanding}/>
      <Route path={'/Customer/Settings/UpdateUserDetails'} exact component={AddPropsToRoute(UpdateUserDetails, {onUpdate})} />
      <Route path={'/Customer/Settings/UpdateAddressDetails'} exact component={AddPropsToRoute(UpdateAddressDetails, {next: '/Customer/Settings/HomeAddressDetails'})}/>
      <Route path={'/Customer/Settings/UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route path={'/Customer/Settings/AddressLookup'} exact component={AddPropsToRoute(AddressLookup, props)} />
      <Redirect to={'/Customer/Settings/CustomerSettingsLanding'}/>
    </Switch>;
  }
}
  

export default CustomerSettings;
