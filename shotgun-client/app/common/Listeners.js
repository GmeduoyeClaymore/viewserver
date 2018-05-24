import FCM, {FCMEvent,
  RemoteNotificationResult,
  WillPresentNotificationResult,
  NotificationType} from 'react-native-fcm';
import {Linking, Platform} from 'react-native';
import Logger from 'common/Logger';


export const registerTokenListener = () => {
  FCM.on(FCMEvent.RefreshToken, token => {
    Logger.debug('TOKEN (refreshUnsubscribe)', token);
    if (this.onChangeToken) {
      this.onChangeToken(token);
    }
  });
};

export const registerActionListener = (handler) => {
  //this callback will be triggered on clicking on a notification when the app is killed
  if (Platform.OS === 'android') {
    Linking.getInitialURL().then(async() => {
      await handleInitialNotification(handler);
    });
  } else {
    Linking.addEventListener('url', async() => {
      await handleInitialNotification(handler);
    });
  }

  FCM.on(FCMEvent.Notification, notif => {
    // console.log('Notification', notif);

    if (Platform.OS === 'ios') {
      //optional
      //iOS requires developers to call completionHandler to end notification process. If you do not call it your background remote notifications could be throttled, to read more about it see the above documentation link.
      //This library handles it for you automatically with default behavior (for remote notification, finish with NoData; for WillPresent, finish depend on "show_in_foreground"). However if you want to return different result, follow the following code to override
      //notif._notificationType is available for iOS platfrom
      switch (notif._notificationType) {
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
      if (notif.opened_from_tray) {
        handler(getActionFromNotification(notif));
      }
    } else {
      Logger.debug('Notification', notif);
      if (notif.opened_from_tray) {
        handler(getActionFromNotification(notif));
      }
    }
  });
};

export const getActionFromNotification = notif => {
  if (Platform.OS === 'ios') {
    if (notif.aps){ /// For some reason remote notifications populate this strange object with the action
      return notif.aps.category;
    }
    return notif.action;
  }

  if (notif.opened_from_tray) {
    return notif.action || notif.click_action;
  }
};


const handleInitialNotification = async(handler) => {
  const notif = await FCM.getInitialNotification();
  const action = notif.action || notif.click_action;

  if (action) {
    handler(action);
  }
};
