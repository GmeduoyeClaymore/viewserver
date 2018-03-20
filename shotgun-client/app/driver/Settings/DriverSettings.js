import React from 'react';
import DriverSettingsLanding from './DriverSettingsLanding';
import UpdateUserDetails from 'common/settings/UpdateUserDetails';
import UpdateBankAccountDetails from './UpdateBankAccountDetails';
import {Route, Redirect, Switch} from 'react-router-native';
import {updateDriver} from 'driver/actions/DriverActions';
import UpdateVehicleDetails from './UpdateVehicleDetails';
import ConfigureServices from './ConfigureServices';

export default DriverSettings = (props) => {
  return <Switch>
    <Route path={'/Driver/Settings/DriverSettingsLanding'} exact component={DriverSettingsLanding}/>
    <Route path={'/Driver/Settings/UpdateUserDetails'} exact render={() => <UpdateUserDetails onUpdate={(user) => updateDriver(user, () => props.history.push('/Driver/Settings/DriverSettings'))}/>} />
    <Route path={'/Driver/Settings/UpdateBankAccountDetails'} exact component={UpdateBankAccountDetails}/>
    <Route path={'/Driver/Settings/UpdateVehicleDetails'} exact render={() => <UpdateVehicleDetails {...props}/>}/>
    <Route path={'/Driver/Settings/ConfigureServices'} exact render={() => <ConfigureServices {...props}/>}/>
    <Redirect to={'/Driver/Settings/DriverSettingsLanding'}/>
  </Switch>;
};
