import React, {Component} from 'react';
import PartnerSettingsLanding from './PartnerSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import UpdatePaymentCardDetails from 'common/settings/UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updatePartner} from 'partner/actions/PartnerActions';
import ConfigureServices from './ConfigureServices';
import PartnerMyOrders from 'partner/PartnerMyOrders';
import UpdateAddressDetails from 'common/settings/UpdateAddressDetails';
import AddressLookup from 'common/components/maps/AddressLookup';

class PartnerSettings extends Component{
  static stateKey = 'PartnerSettings';

  render(){
    const {path, height, width, ...rest} = this.props;
    return <ReduxRouter  name="PartnerSettingsRouter" path={path} defaultRoute={'PartnerSettingsLanding'} height={height} width={width} {...rest}>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'PartnerSettingsLanding'} exact component={PartnerSettingsLanding}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'UpdateUserDetails'} onUpdate={updatePartner} next={`${path}/PartnerSettingsLanding`} exact component={UpdateUserDetails}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'UpdateAddressDetails'} onUpdate={updatePartner} next={`${path}/PartnerSettingsLanding`} exact component={UpdateAddressDetails}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'UpdateBankAccountDetails'} exact component={UpdateBankAccountDetails} next={`${path}/PartnerSettingsLanding`}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'ConfigureServices'} exact stateKey={'configureServices'} component={ConfigureServices}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'PartnerMyOrders'} exact component={PartnerMyOrders} canGoBack={true} isCompleted={true}/>
      <Route stateKey={PartnerSettings.stateKey} transition='left' path={'AddressLookup'} exact component={AddressLookup} />
    </ReduxRouter>;
  }
}

export default PartnerSettings;
