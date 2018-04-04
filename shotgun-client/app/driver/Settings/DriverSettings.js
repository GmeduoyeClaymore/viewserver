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
    const {path} = props;
    this.onUpdate = (user) => updateDriver(user, () => props.history.push(`${path}/DriverSettingsLanding`));
  }
  render(){
    const {path, height, width} = this.props;
    const driverSettingsProps = {...this.props, stateKey: 'driverSettings'};
    return <ReduxRouter  name="DriverSettingsRouter"  path={path} defaultRoute={'DriverSettingsLanding'} height={height} width={width} {...driverSettingsProps}>
      <Route path={'DriverSettingsLanding'} exact component={DriverSettingsLanding}/>
      <Route path={'UpdateUserDetails'} onUpdate={this.onUpdate} exact component={UpdateUserDetails}/>
      <Route path={'UpdateBankAccountDetails'} exact component={UpdateBankAccountDetails}/>
      <Route path={'UpdateVehicleDetails'} exact component={UpdateVehicleDetails}/>
      <Route path={'ConfigureServices'} exact stateKey={'configureServices'} component={ConfigureServices}/>
    </ReduxRouter>;
  }
}

export default DriverSettings;
