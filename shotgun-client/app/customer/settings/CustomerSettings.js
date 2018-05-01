import React from 'react';
import CustomerSettingsLanding from './CustomerSettingsLanding';
import AddressLookup from 'common/components/maps/AddressLookup';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateAddressDetails from 'common/settings/UpdateAddressDetails';
import UpdatePaymentCardDetails from 'common/settings/UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updateCustomer} from 'customer/actions/CustomerActions';


class CustomerSettings extends React.Component {
  static stateKey = 'CustomerSettings';
  constructor(props){
    super(props);
    const {path} = props;
    this.onUpdate = (user) => updateCustomer(user, () => props.history.replace(`${path}/CustomerSettingsLanding`));
  }
  render(){
    const {props} = this;
    const {height, width, path} = props;
    return <ReduxRouter  name="CustomerSettingsRouter"  {...props} height={height} width={width} defaultRoute={'CustomerSettingsLanding'}>
      <Route stateKey={CustomerSettings.stateKey} path={'CustomerSettingsLanding'} parentPath={path} exact component={CustomerSettingsLanding}/>
      <Route stateKey={CustomerSettings.stateKey} transition='left' path={'UpdateUserDetails'} onUpdate={this.onUpdate} exact component={UpdateUserDetails} />
      <Route stateKey={CustomerSettings.stateKey} transition='left' path={'UpdateAddressDetails'} next={`${path}/CustomerSettingsLanding`} exact component={UpdateAddressDetails}/>
      <Route stateKey={CustomerSettings.stateKey} transition='left' path={'UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route stateKey={CustomerSettings.stateKey} transition='left' path={'AddressLookup'} exact component={AddressLookup} />
    </ReduxRouter>;
  }
}

export default CustomerSettings;
