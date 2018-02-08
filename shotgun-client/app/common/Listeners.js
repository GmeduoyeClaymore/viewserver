import FCM, {FCMEvent} from 'react-native-fcm';
import {Linking, Platform} from 'react-native';
import Logger from 'common/Logger';

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

  // this callback will be triggered only when app is foreground or background
  FCM.on(FCMEvent.Notification, notif => {
    Logger.debug('Notification', notif);

    if (notif.opened_from_tray) {
      handler(notif.click_action);
    }
  });

  FCM.on(FCMEvent.RefreshToken, token => {
    Logger.debug('TOKEN (refreshUnsubscribe)', token);
    if (this.onChangeToken) {
      this.onChangeToken(token);
    }
  });
};

const handleInitialNotification = async(handler) => {
  const notif = await FCM.getInitialNotification();
  if (notif.click_action) {
    handler(notif.click_action);
  }
};
