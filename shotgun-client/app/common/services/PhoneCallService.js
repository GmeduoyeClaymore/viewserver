import PermissionsService from 'common/services/PermissionsService';
import RNImmediatePhoneCall from 'react-native-immediate-phone-call';

export default class PhoneCallsService {
  static async call(phoneNumber){
    await PermissionsService.requestPhoneCallPermission();
    RNImmediatePhoneCall.immediatePhoneCall(`+${phoneNumber}`);
  }
}
