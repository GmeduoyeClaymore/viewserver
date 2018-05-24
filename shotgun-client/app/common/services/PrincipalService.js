import Logger from 'common/Logger';
import {NativeModules, AsyncStorage  as DefaultAsyncStorage, Platform} from 'react-native';
import invariant from 'invariant';

//Async storage hangs on android sim so we swap it out here
const AsyncStorage = Platform.OS === 'android' ? NativeModules.RNAsyncStorage : DefaultAsyncStorage;

export default class PrincipalService {
  static userEmailKey = '@shotgun:email';
  static userPasswordKey = '@shotgun:password';

  static async getLoginDetailsFromDevice(){
    try {
      const email = await AsyncStorage.getItem(PrincipalService.userEmailKey).timeoutWithError(5000, 'Unable to get user email within 5 second timespan');
      const password = await AsyncStorage.getItem(PrincipalService.userPasswordKey).timeoutWithError(5000, 'Unable to get user password within 5 second timespan');

      return {email, password};
    } catch (error) {
      throw new Error('Error getting user login details from device ' + error);
    }
  }

  static async setLoginDetailsOnDevice(email, password){
    try {
      invariant(email, 'Email cannot be null');
      invariant(password, 'Password cannot be null');

      AsyncStorage.setItem(PrincipalService.userEmailKey, email).timeoutWithError(5000, 'Unable to set user email on device within 5 second timespan');
      AsyncStorage.setItem(PrincipalService.userPasswordKey, password).timeoutWithError(5000, 'Unable to set user password on device within 5 second timespan');
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error saving user login details on device ' + error);
    }
  }

  static async removeLoginDetailsFromDevice(){
    try {
      await AsyncStorage.clear();
    } catch (error) {
      //TODO - error handling here
      Logger.error('Error removing user login details from device' + error);
    }
  }
}
