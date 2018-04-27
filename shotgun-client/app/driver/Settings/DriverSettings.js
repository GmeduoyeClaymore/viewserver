import React, {Component} from 'react';
import DriverSettingsLanding from './DriverSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import UpdatePaymentCardDetails from 'common/settings/UpdatePaymentCardDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updateDriver} from 'driver/actions/DriverActions';
import ConfigureServices from './ConfigureServices';
import DriverOrders from 'driver/DriverOrders';

class  DriverSettings extends Component{
  constructor(props){
    super(props);
    const {path} = props;
    this.onUpdate = (user) => updateDriver(user, () => props.history.push(`${path}/DriverSettingsLanding`));
  }
  render(){
    const {path, height, width} = this.props;
    const driverSettingsProps = {...this.props, stateKey: 'driverSettings'};
    return <ReduxRouter  name="DriverSettingsRouter" path={path} defaultRoute={'DriverSettingsLanding'} height={height} width={width} {...driverSettingsProps}>
      <Route transition='left' path={'DriverSettingsLanding'} exact component={DriverSettingsLanding}/>
      <Route transition='left' path={'UpdateUserDetails'} onUpdate={this.onUpdate} exact component={UpdateUserDetails}/>
      <Route transition='left' path={'UpdateBankAccountDetails'} exact component={UpdateBankAccountDetails} next={`${path}/DriverSettingsLanding`}/>
      <Route transition='left' path={'ConfigureServices'} exact stateKey={'configureServices'} component={ConfigureServices}/>
      <Route transition='left' path={'UpdatePaymentCardDetails'} exact component={UpdatePaymentCardDetails}/>
      <Route transition='left' path={'DriverOrders'} exact component={DriverOrders} canGoBack={true} isCompleted={true}/>
    </ReduxRouter>;
  }
}

export default DriverSettings;
