import RNRestart from 'react-native-restart';
import {Alert, BackHandler} from 'react-native';

export default errorHandler = (e, isFatal) => {
  if (isFatal) {
    const message = typeof e == 'object' ? e.message : e;

    Alert.alert(
      'Sorry. An unexpected error occurred',
      `You will need to restart the app.\n\nError: ${message}`,
      [{
        text: 'Close',
        onPress: () => BackHandler.exitApp()
      },
      {
        text: 'Restart',
        onPress: () => RNRestart.Restart()
      }]
    );
  } else {
    console.log(e); // So that we can see it in the ADB logs in case of Android if needed
  }
};
