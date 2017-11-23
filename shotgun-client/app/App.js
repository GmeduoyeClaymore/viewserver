import React from 'react';
import {Container, Spinner, Text} from 'native-base';
import {Provider} from 'react-redux';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import Logger from 'common/Logger';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import PrincipalService from './common/services/PrincipalService';
import CustomerLanding from './customer/CustomerLanding';
import CustomerRegistration from './customer/registration/CustomerRegistration';
import {NativeRouter, Route, Redirect, Switch, AndroidBackButton} from 'react-router-native';

const store = configureStore();

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'Home';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false
    };
    this.client = new Client('ws://localhost:8080/');
    this.applicationMode = 'customer';
    this.dispatch = store.dispatch;
  }

  async componentWillMount() {
    let isConnected = false;
    try {
      await ProtoLoader.loadAll();
      await this.setCustomerId();
      this.setInitialRoot();
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

  setInitialRoot(){
    if (this.customerId == undefined){
      App.INITIAL_ROOT_NAME = '/CustomerRegistration';
    } else {
      App.INITIAL_ROOT_NAME = '/CustomerLanding';
      Logger.info(`Loading with customer id ${this.customerId}`);
    }
  }

  render() {
    if (!this.state.isReady) {
      return <Spinner/>;
    } else if (!this.state.isConnected){
      return  <Container style={{flexDirection: 'column', flex: 1}}>
        <Text>Uh-oh spaghetti-Os. Unable to connect to the server</Text>
      </Container>;
    }
    const globalProps = {client: this.client, customerId: this.customerId, dispatch: this.dispatch};

    return <Provider store={store}>
      <NativeRouter>
        <AndroidBackButton>
          <Switch>
            <Route path="/Root" component={App}/>
            <Route path="/CustomerRegistration" render={(props) => <CustomerRegistration {...globalProps} {...props}/>}/>
            <Route path="/CustomerLanding" render={(props) => <CustomerLanding {...globalProps} {...props}/>}/>
            <Redirect to={App.INITIAL_ROOT_NAME}/>
          </Switch>
        </AndroidBackButton>
      </NativeRouter>
    </Provider>;
  }
}
