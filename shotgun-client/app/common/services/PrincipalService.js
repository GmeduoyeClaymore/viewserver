import Logger from 'common/Logger';
import {NativeModules, AsyncStorage  as DefaultAsyncStorage, Platform} from 'react-native';
import invariant from 'invariant';

//Async storage hangs on android sim so we swap it out here
const AsyncStorage = Platform.OS === 'android' ? NativeModules.RNAsyncStorage : DefaultAsyncStorage;

export default class PrincipalService {
  static userIdKey = '@shotgun:userId';

  static async getUserIdFromDevice(){
    try {
      return await AsyncStorage.getItem(PrincipalService.userIdKey).timeoutWithError(5000, 'Unable to find get userid within 5 second timespan');
    } catch (error) {
      throw new Error('Error getting user id from device ' + error);
    }
  }

  static async setUserIdOnDevice(userId){
    try {
      invariant(userId, 'User cannot be null');
      await AsyncStorage.setItem(PrincipalService.userIdKey, userId).timeoutWithError(5000, 'Unable to find set userid within 5 second timespan');
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error saving user id on device ' + error);
    }
  }

  static async removeUserIdFromDevice(){
    try {
      await AsyncStorage.clear();
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error removing user id from device' + error);
    }
  }
}
