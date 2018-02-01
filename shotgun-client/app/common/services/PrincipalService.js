import Logger from 'common/Logger';
import {AsyncStorage} from 'react-native';

export default class PrincipalService {
  static userIdKey = '@shotgun:userId';

  static async getUserIdFromDevice(){
    try {
      return await AsyncStorage.getItem(PrincipalService.userIdKey).timeoutWithError(5000, 'Unable to find get userid within 5 second timespan');
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error getting user id from device ' + error);
    }
  }

  static async setUserIdOnDevice(userId){
    try {
      await AsyncStorage.setItem(PrincipalService.userIdKey, userId).timeoutWithError(5000, 'Unable to find set userid within 5 second timespan');
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error saving user id on device ' + error);
    }
  }

  static async removeUserIdFromDevice(){
    try {
      await AsyncStorage.removeItem(PrincipalService.userIdKey);
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error removing user id from device' + error);
    }
  }
}
