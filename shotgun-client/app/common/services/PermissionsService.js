import { Platform, PermissionsAndroid } from 'react-native';
import Logger from 'common/Logger';

const IS_ANDROID = Platform.OS === 'android';
export default class PermissionsService {
  static requestLocationPermission(){
    PermissionsService.requestPermission(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
      title: 'Shotgun Location Permission',
      message: 'Shotgun uses your location to make deliveries easier'
    });
  }

  static async requestCameraPermission(){
    await PermissionsService.requestPermission(PermissionsAndroid.PERMISSIONS.CAMERA, {
      title: 'Shotgun Camera Permission',
      message: 'Allow Shotgun to access your camera so you can add photos to your orders'
    });

    await PermissionsService.requestPermission(PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE, {
      title: 'Shotgun Storage Permission',
      message: 'Allow Shotgun save photos you take to your device'
    });
  }

  static async requestPickerPermission(){
    await PermissionsService.requestPermission(PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE, {
      title: 'Shotgun Storage Permission',
      message: 'Allow Shotgun to use images you select from your phone'
    });
  }

  static async requestPhoneCallPermission(){
    await PermissionsService.requestPermission(PermissionsAndroid.PERMISSIONS.CALL_PHONE, {
      title: 'Shotgun Phone Permission',
      message: 'Used to allow you to call other users from Shotgun'
    });
  }

  static async requestPermission(permission, popup){
    if (!IS_ANDROID){
      return;
    }
    try {
      const alreadyGranted = await PermissionsAndroid.check(permission);
      Logger.debug(`Permission ${permission} is ${alreadyGranted ? '' : 'not'} granted`);
      if (!alreadyGranted) {
        const granted = await PermissionsAndroid.request(permission, popup);
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          Logger.debug(`Permission ${permission} granted`);
        } else {
          Logger.warning(`Permission ${permission} denied with ${granted}`);
        }
      }
    } catch (err) {
      Logger.warning(err);
    }
  }
}
