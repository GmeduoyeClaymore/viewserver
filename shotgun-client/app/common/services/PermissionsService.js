import { Platform, PermissionsAndroid } from 'react-native';
import Logger from 'common/Logger';
const IS_ANDROID = Platform.OS === 'android';
export default class PermissionsService {
  static async requestLocationPermission(){
    if (!IS_ANDROID){
      return;
    }
    try {
      const alreadyGranted = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
      if (!alreadyGranted) {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            'title': 'Shotgun Location Permission',
            'message': 'Shotgun uses your location to make deliveries easier'
          }
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          Logger.log('You can use the camera');
        } else {
          Logger.log('Camera permission denied');
        }
      }
    } catch (err) {
      Logger.warning(err);
    }
  }
}
