import {invokeDaoCommand} from 'common/dao';
import {register} from 'common/actions/CommonActions';
import ProductDao from 'common/dao/ProductDao';
import UserDao from 'common/dao/UserDao';
import DriverDao from 'driver/dao/DriverDao';
import VehicleDao from 'driver/dao/VehicleDao';

export const driverServicesRegistrationAction = (client, userId, continueWith) => {
  return async (dispatch, getState) => {
    const state = getState();
    if (!state.getIn(['dao', 'driverDao'])){
      register(dispatch, new ProductDao(client));
      const userDao = register(dispatch, new UserDao(client), {userId});
      const vehicleDao = register(dispatch, new VehicleDao(client), {userId});
      register(dispatch, new DriverDao(client, userDao, vehicleDao), {userId}, continueWith);
    } else if (continueWith) {
      continueWith();
    }
  };
};

export const addOrUpdateDriver = (driver, vehicle, continueWith) => {
  return invokeDaoCommand('driverDao', 'addOrUpdateDriver', {driver, vehicle}, continueWith);
};
