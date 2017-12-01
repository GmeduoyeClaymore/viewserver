import React from 'react';
import {Container, Text} from 'native-base';
import {Provider} from 'react-redux';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import Logger from 'common/Logger';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import PrincipalService from './common/services/PrincipalService';
import LandingCommon from './LandingCommon';
import RegistrationCommon from './RegistrationCommon';
import CustomerRegistration from './customer/registration/CustomerRegistration';
import DriverRegistration from './driver/registration/DriverRegistration';
import CustomerLanding from './customer/CustomerLanding';
import DriverLanding from './driver/DriverLanding';
import {NativeRouter, Route, Redirect, Switch, AndroidBackButton} from 'react-router-native';
import LoadingScreen from 'common/components/LoadingScreen';

const store = configureStore();

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'LandingCommon';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false
    };
    this.client = new Client('ws://localhost:8080/');
    this.dispatch = store.dispatch;
  }

  async componentDidMount() {
    let isConnected = false;
    try {
      Logger.debug('Mounting App Component');
      await ProtoLoader.loadAll();
      await this.setUserId();
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

  async setUserId(){
    this.userId = await PrincipalService.getUserIdFromDevice();
  }

  setInitialRoot(){
    if (this.userId == undefined){
      App.INITIAL_ROOT_NAME = '/RegistrationCommon';
    } else {
      App.INITIAL_ROOT_NAME = '/LandingCommon';
      Logger.info(`Loading with customer id ${this.userId}`);
    }
  }

  render() {
    if (!this.state.isReady) {
      return <LoadingScreen text="Connecting"/>;
    } else if (!this.state.isConnected){
      return  <Container style={{flexDirection: 'column', flex: 1}}>
        <Text>Uh-oh spaghetti-Os. Unable to connect to the server</Text>
      </Container>;
    }
    const globalProps = {client: this.client, userId: this.userId, dispatch: this.dispatch};

    return <Provider store={store}>
      <NativeRouter>
        <AndroidBackButton>
          <Switch>
            <Route path="/Root" component={App}/>
            <Route path="/RegistrationCommon" exact render={(props) => <RegistrationCommon {...globalProps} {...props}/>}/>
            <Route path="/LandingCommon" exact render={(props) => <LandingCommon {...globalProps} {...props}/>}/>
            <Route path="/Customer/Registration" render={(props) => <CustomerRegistration {...globalProps} {...props}/>}/>
            <Route path="/Driver/Registration" render={(props) => <DriverRegistration {...globalProps} {...props}/>}/>
            <Route path="/Customer" render={(props) => <CustomerLanding {...globalProps} {...props}/>}/>
            <Route path="/Driver" render={(props) => <DriverLanding {...globalProps} {...props}/>}/>
            <Redirect to={App.INITIAL_ROOT_NAME}/>
          </Switch>
        </AndroidBackButton>
      </NativeRouter>
    </Provider>;
  }
}
