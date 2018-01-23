import { Platform, AsyncStorage } from 'react-native';

import FCM, {FCMEvent, RemoteNotificationResult, WillPresentNotificationResult, NotificationType} from 'react-native-fcm';
export const getLastNotification = async () => {
  const data = await AsyncStorage.getItem('lastNotification');
  if (data){
    // if notification arrives when app is killed, it should still be logged here
    console.log('last notification', data);
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
    console.log('Notification', notif);
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
    if (context && context.onNotificationClicked){
      context.onNotificationClicked(localNotification);
    }

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
    console.log('TOKEN (refreshUnsubscribe)', token);
    if (this.onChangeToken){
      this.onChangeToken(token);
    }
  });

  FCM.enableDirectChannel();
  FCM.on(FCMEvent.DirectChannelConnectionChanged, (data) => {
    console.log('direct channel connected' + data);
  });
  setTimeout(() => {
    FCM.isDirectChannelEstablished().then(d => console.log(d));
  }, 1000);
};
