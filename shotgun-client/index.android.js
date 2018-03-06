import {AppRegistry} from 'react-native';
import App from './app/App';

import {Alert} from 'react-native';
import RNRestart from 'react-native-restart';
import {setJSExceptionHandler} from 'react-native-exception-handler';

AppRegistry.registerComponent('ShotgunClient', () => App);
