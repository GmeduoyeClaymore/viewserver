import React from 'react';
import {View} from 'react-native';
import {Spinner, Text} from 'native-base';
import {Provider} from 'react-redux';
import {StackNavigator} from 'react-navigation';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import Logger from 'common/Logger';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import PrincipalService from './common/services/PrincipalService';
import CustomerLanding from './customer/CustomerLanding';
import CustomerRegistration from './customer/registration/CustomerRegistration';

const store = configureStore();

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'Home';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false
    };
    //this.client = new Client('ws://192.168.0.5:8080/');
    this.client = new Client('ws://localhost:8080/');
    this.applicationMode = 'customer';
    this.dispatch = store.dispatch;
  }

  async componentWillMount() {
    let isConnected = false;
    try {
      await ProtoLoader.loadAll();
      await this.setCustomerId();
      Logger.debug('Mounting App Component');
      await this.client.connect();
      isConnected = true;
    } catch (error){
      Logger.debug('Unable to connect to server');
    }
    Logger.debug('App Component Mounted');
    this.setState({ isReady: true, isConnected });
  }

  async setCustomerId(){
    this.customerId = await PrincipalService.getCustomerIdFromDevice();
  }

  render() {
    if (!this.state.isReady) {
      return <Spinner/>;
    } else if (!this.state.isConnected){
      return  <View style={{flexDirection: 'column', flex: 1}}>
        <Text>Uh-oh spaghetti-Os. Unable to connect to the server</Text>
      </View>;
    }

    if (this.customerId == undefined){
      App.INITIAL_ROOT_NAME = 'Registration';
    } else {
      App.INITIAL_ROOT_NAME = 'Home';
      Logger.info(`Loading with customer id ${this.customerId}`);
    }

    const screenProps = {client: this.client, customerId: this.customerId, dispatch: this.dispatch};

    //TODO - change the home screen based on the current application mode
    const AppNavigator = StackNavigator(
      {
        Root: {screen: App},
        Home: { screen: CustomerLanding },
        Registration: { screen: CustomerRegistration }
      }, {
        initialRouteName: App.INITIAL_ROOT_NAME,
        headerMode: 'none'
      });

    return <Provider store={store}>
      <View style={{flexDirection: 'column', flex: 1}}>
      <AppNavigator screenProps={screenProps} />
    </View>
    </Provider>;
  }
}
