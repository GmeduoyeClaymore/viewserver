import Logger from 'common/Logger';
import {AsyncStorage} from 'react-native';

export default class PrincipalService {
  static userIdKey = '@shotgun:userId';

  static async getUserIdFromDevice(){
    try {
      const userId = await AsyncStorage.getItem(PrincipalService.userIdKey);
      return userId;
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error getting user id from device');
    }
  }

  static async setUserIdOnDevice(userId){
    try {
      await AsyncStorage.setItem(PrincipalService.userIdKey, userId);
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error saving user id on device');
    }
  }

  static async removeUserIdFromDevice(){
    try {
      await AsyncStorage.removeItem(PrincipalService.userIdKey);
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error removing user id from device');
    }
  }
}
