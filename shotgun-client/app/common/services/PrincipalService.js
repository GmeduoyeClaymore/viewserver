import Logger from 'common/Logger';
import {AsyncStorage} from 'react-native';

export default class PrincipalService {
  static customerIdKey = '@shotgun:customerId';

  static async getCustomerIdFromDevice(){
    try {
      const customerId = await AsyncStorage.getItem(PrincipalService.customerIdKey);
      return customerId;
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error getting customer id from device');
    }
  }

  static async setCustomerIdOnDevice(customerId){
    try {
      await AsyncStorage.setItem(PrincipalService.customerIdKey, customerId);
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error saving customer id on device');
    }
  }

  static async removeCustomerIdFromDevice(){
    try {
      await AsyncStorage.removeItem(PrincipalService.customerIdKey);
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error removing customer id from device');
    }
  }
}
