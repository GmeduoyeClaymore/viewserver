import {Platform} from 'react-native';
import Rx from 'rxjs/Rx';
import FCM, {FCMEvent, RemoteNotificationResult, WillPresentNotificationResult, NotificationType} from 'react-native-fcm';

// this shall be called regardless of app state: running, background or not running. Won't be called when app is killed by user in iOS
export default class NotificationService {
  constructor(){
    this._tokenUpdated = new Rx.ReplaySubject();
  }

  sendLocalNotification(){
    FCM.presentLocalNotification({
      id: 'UNIQ_ID_STRING',                               // (optional for instant notification)
      title: 'My Notification Title',                     // as FCM payload
      body: 'My Notification Message',                    // as FCM payload (required)
      sound: 'default',                                   // as FCM payload
      priority: 'high',                                   // as FCM payload
      click_action: 'ACTION',                             // as FCM payload
      badge: 10,                                          // as FCM payload IOS only, set 0 to clear badges
      number: 10,                                         // Android only
      ticker: 'My Notification Ticker',                   // Android only
      auto_cancel: true,                                  // Android only (default true)
      large_icon: 'ic_launcher',                           // Android only
      icon: 'ic_launcher',                                // as FCM payload, you can relace this with custom icon you put in mipmap
      big_text: 'Show when notification is expanded',     // Android only
      sub_text: 'This is a subText',                      // Android only
      color: 'red',                                       // Android only
      vibrate: 300,                                       // Android only default: 300, no vibration if you pass 0
      wake_screen: true,                                  // Android only, wake up screen when notification arrives
      group: 'group',                                     // Android only
      picture: 'https://google.png',                      // Android only bigPicture style
      ongoing: true,                                      // Android only
      my_custom_data: 'my_custom_field_value',             // extra data you want to throw
      lights: true,                                       // Android only, LED blinking (default false)
      show_in_foreground                                  // notification when app is in foreground (local & remote)
    });
  }

  get tokenUpdated(){
    return this._tokenUpdated;
  }

  start(){
    const {_tokenUpdated} = this;
    FCM.on(FCMEvent.RefreshToken, (token) => {
      console.log(token);
      _tokenUpdated.next(token);
      // fcm token may not be available on first load, catch it here
    });
  }

  async requestPermissions(){
    return FCM.requestPermissions();
  }

  async getToken(){
    return FCM.getFCMToken();
  }

  onNotification(notif){
    // there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
    if (notif.local_notification){
      //this is a local notification
    }
    if (notif.opened_from_tray){
      //iOS: app is open/resumed because user clicked banner
      //Android: app is open/resumed because user clicked banner or tapped app icon
    }
    // await someAsyncCall();
        
    if (Platform.OS === 'ios'){
      //optional
      //iOS requires developers to call completionHandler to end notification process. If you do not call it your background remote notifications could be throttled, to read more about it see https://developer.apple.com/documentation/uikit/uiapplicationdelegate/1623013-application.
      //This library handles it for you automatically with default behavior (for remote notification, finish with NoData; for WillPresent, finish depend on "show_in_foreground"). However if you want to return different result, follow the following code to override
      //notif._notificationType is available for iOS platfrom
      switch (notif._notificationType){
      case NotificationType.Remote:
        notif.finish(RemoteNotificationResult.NewData); //other types available: RemoteNotificationResult.NewData, RemoteNotificationResult.ResultFailed
        break;
      case NotificationType.NotificationResponse:
        notif.finish();
        break;
      case NotificationType.WillPresent:
        notif.finish(WillPresentNotificationResult.All); //other types available: WillPresentNotificationResult.None
        break;
      }
    }
  }
}
