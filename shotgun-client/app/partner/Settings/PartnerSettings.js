import React, {Component} from 'react';
import PartnerSettingsLanding from './PartnerSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import UpdatePaymentCardDetails from 'common/settings/UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updatePartner} from 'partner/actions/PartnerActions';
import ConfigureServices from './ConfigureServices';
import PartnerMyOrders from 'partner/PartnerMyOrders';

class  PartnerSettings extends Component{
  constructor(props){
    super(props);
    const {path, history} = props;
    //if we move this function out of the constructor stuff breaks for some reason
    this.onUpdate = (user) => updatePartner(user, () => history.push(`${path}/PartnerSettingsLanding`));
  }

  render(){
    const {path, height, width} = this.props;
    const partnerSettingsProps = {...this.props, stateKey: 'partnerSettings'};
    return <ReduxRouter  name="PartnerSettingsRouter" path={path} defaultRoute={'PartnerSettingsLanding'} height={height} width={width} {...partnerSettingsProps}>
      <Route transition='left' path={'PartnerSettingsLanding'} exact component={PartnerSettingsLanding}/>
      <Route transition='left' path={'UpdateUserDetails'} onUpdate={this.onUpdate} exact component={UpdateUserDetails}/>
      <Route transition='left' path={'UpdateBankAccountDetails'} exact component={UpdateBankAccountDetails} next={`${path}/PartnerSettingsLanding`}/>
      <Route transition='left' path={'ConfigureServices'} exact stateKey={'configureServices'} component={ConfigureServices}/>
      <Route transition='left' path={'UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route transition='left' path={'PartnerMyOrders'} exact component={PartnerMyOrders} canGoBack={true} isCompleted={true}/>
    </ReduxRouter>;
  }
}

export default PartnerSettings;
