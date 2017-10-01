import {AppRegistry} from 'react-native';
import { Tester, TestHookStore } from 'cavy';

import App from './app/App';
import React, {Component} from 'react';
import AppSpec from './specs/EmployeeListSpec';
const testHookStore = new TestHookStore();

class AppWrapper extends Component {
  render() {
    return (
      <Tester specs={[AppSpec]} store={testHookStore} waitTime={4000}>
        <div>Foo</div>
      </Tester>
    );
  }
}
AppRegistry.registerComponent('ShotgunClient', () => App);