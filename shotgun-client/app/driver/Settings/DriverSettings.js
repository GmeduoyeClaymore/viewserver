import React, {Component} from 'react';
import DriverSettingsLanding from './DriverSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import {Route, ReduxRouter} from 'custom-redux';
import {updateDriver} from 'driver/actions/DriverActions';
import UpdateVehicleDetails from './UpdateVehicleDetails';
import ConfigureServices from './ConfigureServices';
class  DriverSettings extends Component{
  constructor(props){
    super(props);
    this.onUpdate = (user) => updateDriver(user, () => props.history.push(`${path}/DriverSettings`));
  }
  render(){
    const {path, height, width} = this.props;
    const driverSettingsProps = {...this.props, stateKey: 'driverSettings'};
    return <ReduxRouter defaultRoute={`${path}/DriverSettingsLanding`} height={height} width={width} {...driverSettingsProps}>
      <Route path={`${path}/DriverSettingsLanding`} exact component={DriverSettingsLanding}/>
      <Route path={`${path}/UpdateUserDetails`} onUpdate={this.onUpdate} exact component={UpdateUserDetails}/>
      <Route path={`${path}/UpdateBankAccountDetails`} exact component={UpdateBankAccountDetails}/>
      <Route path={`${path}/UpdateVehicleDetails`} exact component={UpdateVehicleDetails}/>
      <Route path={`${path}/ConfigureServices`} exact stateKey={'configureServices'} component={ConfigureServices}/>
    </ReduxRouter>;
  }
};

export default DriverSettings;
