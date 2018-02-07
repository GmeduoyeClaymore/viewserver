import { Platform, AsyncStorage } from 'react-native';

import FCM, {FCMEvent, RemoteNotificationResult, WillPresentNotificationResult, NotificationType} from 'react-native-fcm';
import Logger from 'common/Logger';

export const getLastNotification = async () => {
  const data = await AsyncStorage.getItem('lastNotification');
  if (data){
    // if notification arrives when app is killed, it should still be logged here
    Logger.debug('last notification', data);
    AsyncStorage.removeItem('lastNotification');
    return JSON.parse(data);
  }
  return data;
};


export const registerKilledListener = () => {
  // these callback will be triggered even when app is killed
  FCM.on(FCMEvent.Notification, notif => {
    AsyncStorage.setItem('lastNotification', JSON.stringify(notif));
  });
};

// these callback will be triggered only when app is foreground or background
export const  registerAppListener = (context) => {
  FCM.on(FCMEvent.Notification, notif => {
    Logger.debug('Notification', notif);
    if (notif.local_notification){
      return;
    }
    if (notif.opened_from_tray){
      return;
    }

    const content = notif.fcm;
    const localNotification = {
      ...notif,
      ...content,
      vibrate: 500,
      priority: 'high',
      show_in_foreground: true,
      group: 'test',
      number: 10
    };
    FCM.presentLocalNotification(localNotification);

    if (Platform.OS === 'ios'){
      //optional
      //iOS requires developers to call completionHandler to end notification process. If you do not call it your background remote notifications could be throttled, to read more about it see the above documentation link.
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
      default:
        break;
      }
    }
  });

  FCM.on(FCMEvent.RefreshToken, token => {
    Logger.debug('TOKEN (refreshUnsubscribe)', token);
    if (this.onChangeToken){
      this.onChangeToken(token);
    }
  });

  FCM.enableDirectChannel();
  FCM.on(FCMEvent.DirectChannelConnectionChanged, (data) => {
    Logger.debug('direct channel connected' + data);
  });
  setTimeout(() => {
    FCM.isDirectChannelEstablished().then(d => Logger.debug(d));
  }, 1000);
};
