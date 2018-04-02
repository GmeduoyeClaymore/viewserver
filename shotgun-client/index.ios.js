import {AppRegistry} from 'react-native';
import App from './app/App';
import {setJSExceptionHandler, setNativeExceptionHandler} from 'react-native-exception-handler';
import errorHandler from 'common/GlobalErrorHandler';

//setJSExceptionHandler(errorHandler, false);
//setNativeExceptionHandler(() => {}, false);

AppRegistry.registerComponent('ShotgunClient', () => App);
