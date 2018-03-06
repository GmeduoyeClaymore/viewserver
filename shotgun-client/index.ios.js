import {AppRegistry, Alert} from 'react-native';
import App from './app/App';
import RNRestart from 'react-native-restart';
import {setJSExceptionHandler, setNativeExceptionHandler} from 'react-native-exception-handler';

const errorHandler = (e, isFatal) => {
  if (isFatal) {
    const message = typeof e == 'object' ? e.message : e;

    Alert.alert(
      'Sorry. An unexpected error occurred',
      `You will need to restart the app.\n\nError: ${message}`,
      [{
        text: 'Restart',
        onPress: () => {
          RNRestart.Restart();
        }
      }]
    );
  } else {
    console.log(e); // So that we can see it in the ADB logs in case of Android if needed
  }
};

setJSExceptionHandler(errorHandler, true);
setNativeExceptionHandler(() => {}, false);

AppRegistry.registerComponent('ShotgunClient', () => App);
