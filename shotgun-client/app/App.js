import React from 'react';
import {View} from 'react-native';
import {Provider} from 'react-redux';
import {StackNavigator} from 'react-navigation';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import Logger from './viewserver-client/Logger';
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
      isReady: false
    };
    this.client = new Client('ws://192.168.0.5:8080/');
    //this.client = new Client('ws://localhost:8080/');
    this.applicationMode = 'customer';
  }

  async componentWillMount() {
    try {
      await ProtoLoader.loadAll();
      await this.setCustomerId();
      Logger.debug('Mounting component !!' + ProtoLoader.Dto.AuthenticateCommandDto);
      await this.client.connect();
    } catch (error){
      Logger.error(error);
    }
    Logger.debug('Network connected !!');
    this.setState({ isReady: true });
  }

  async setCustomerId(){
    this.customerId = await PrincipalService.getCustomerIdFromDevice();
  }

  render() {
    if (!this.state.isReady) {
      return null;
    }

    if (this.customerId == undefined){
      App.INITIAL_ROOT_NAME = 'Registration';
    } else {
      App.INITIAL_ROOT_NAME = 'Home'
      Logger.info(`Loading with customer id ${this.customerId}`);
    }

    const screenProps = {client: this.client, customerId: this.customerId};

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
