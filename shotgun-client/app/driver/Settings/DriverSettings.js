import React from 'react';
import DriverSettingsLanding from './DriverSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import {Route, Redirect, Switch} from 'react-router-native';
import {updateDriver} from 'driver/actions/DriverActions';
import UpdateVehicleDetails from './UpdateVehicleDetails';
import ConfigureServices from './ConfigureServices';
import AddPropsToRoute from 'common/AddPropsToRoute';
const DriverSettings = (props) => {
  const driverSettingsProps = {...props, stateKey: 'driverSettings'};
  const onUpdate = (user) => updateDriver(user, () => props.history.push('/Driver/Settings/DriverSettings'));
  return <Switch>
    <Route path={'/Driver/Settings/DriverSettingsLanding'} exact component={DriverSettingsLanding}/>
    <Route path={'/Driver/Settings/UpdateUserDetails'} exact component={AddPropsToRoute(UpdateUserDetails, {...driverSettingsProps, onUpdate})}/>
    <Route path={'/Driver/Settings/UpdateBankAccountDetails'} exact component={AddPropsToRoute(UpdateBankAccountDetails, driverSettingsProps)}/>
    <Route path={'/Driver/Settings/UpdateVehicleDetails'} exact component={AddPropsToRoute(UpdateVehicleDetails, driverSettingsProps)}/>
    <Route path={'/Driver/Settings/ConfigureServices'} exact component={AddPropsToRoute(ConfigureServices, {...driverSettingsProps, stateKey: 'configureServices'  })}/>
    <Redirect to={'/Driver/Settings/DriverSettingsLanding'}/>
  </Switch>;
};

export default DriverSettings;
