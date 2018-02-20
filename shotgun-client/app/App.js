import React from 'react';
import {UIManager, View} from 'react-native';
import {Container, Text, StyleProvider, Root, Button} from 'native-base';
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
import TermsAndConditions from 'common/registration/TermsAndConditions';
import CustomerLanding from './customer/CustomerLanding';
import DriverLanding from './driver/DriverLanding';
import {NativeRouter, Route, Redirect, Switch, AndroidBackButton} from 'react-router-native';
import {LoadingScreen} from 'common/components';
import getTheme from './native-base-theme/components';
import shotgun from 'native-base-theme/variables/shotgun';
import FCM from 'react-native-fcm';

const signOut = async () => {
  await PrincipalService.removeUserIdFromDevice();
};

const store = configureStore();
if (UIManager.setLayoutAnimationEnabledExperimental){
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'LandingCommon';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false,
    };
    //this.client = new Client('ws://shotgun.ltd:6060/');
    this.client = new Client('ws://localhost:6060/');
    Client.setCurrent(this.client);
    this.dispatch = store.dispatch;
    this.onChangeToken = this.onChangeToken.bind(this);
    this.setUserId = this.setUserId.bind(this);
    this.initMessaging = this.initMessaging.bind(this);
    this.setInitialRoot = this.setInitialRoot.bind(this);
    this.handleConnectionStatusChanged = this.handleConnectionStatusChanged.bind(this);
    this.client.connection.connectionObservable.subscribe(this.handleConnectionStatusChanged);
  }

  async componentDidMount() {
    try {
      Logger.debug('Mounting App Component');
      await ProtoLoader.loadAll();
      await this.client.connect(true);
    } catch (error){
      this.setState({ error});
    }
    Logger.debug('App Component Mounted');
  }

  async handleConnectionStatusChanged(isReady){
    Logger.info(`Connection status changed to :\"${isReady}\"`);
    if (isReady){
      try {
        await this.setUserId();
        await this.initMessaging();
        this.setInitialRoot();
      } catch (error){
        this.setState({ error, isReady: true, isConnected: false});
        return;
      }
    }
    this.setState({ isReady, isConnected: isReady});
  }

  async initMessaging(){
    Logger.info('......Initializing firebase messaging');
    if (!this.userId){
      Logger.warning('No userid has been specified not initializing messaging');
      return;
    }

    try {
      await FCM.requestPermissions({badge: false, sound: true, alert: true});
      const token = await FCM.getFCMToken();
      await this.onChangeToken(token);
      this.setState({token: token || ''});
    } catch (error){
      //Logger.error(error);
      this.setState({error});
    }
    Logger.info('!!! Finished initializing firebase messaging');
  }

  async onChangeToken(token){
    await this.client.invokeJSONCommand('messagingController', 'updateUserToken', token);
  }

  async setUserId(){
    Logger.info('......Attempting to get userid from device');
    this.userId = await PrincipalService.getUserIdFromDevice();
    const {userId} = this;
    if (userId){
      //TODO this is really unsafe really we should be saving credentials in the client not just the userID
      await this.client.invokeJSONCommand('loginController', 'setUserId', userId);
    }
    Logger.info(`!!! Got user id ${this.userId} from device`);
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
    const {isReady, isConnected, error} = this.state;
    if (!isReady) {
      return <LoadingScreen text="Connecting"/>;
    } else if (!isConnected){
      return  <Container style={{flexDirection: 'column', flex: 1}}>
        <Text>{'Not connected - ERROR IS:' + JSON.stringify(error)}</Text>
        <Button fullWidth paddedBottom signOutButton onPress={signOut}><Text uppercase={false}>Sign out</Text></Button>
      </Container>;
    }
    const globalProps = {client: this.client, userId: this.userId, dispatch: this.dispatch};

    return <Provider store={store}>
      <Root>
        <NativeRouter>
          <AndroidBackButton>
            <StyleProvider style={getTheme(shotgun)}>
              <View style={{flex: 1, backgroundColor: '#ffffff'}}>
                <Switch>
                  <Route path="/Root" component={App}/>
                  <Route path="/RegistrationCommon" exact render={(props) => <RegistrationCommon {...globalProps} {...props}/>}/>
                  <Route path="/LandingCommon" exact render={(props) => <LandingCommon {...globalProps} {...props}/>}/>
                  <Route path="/Customer/Registration" render={(props) => <CustomerRegistration {...globalProps} {...props}/>}/>
                  <Route path="/Driver/Registration" render={(props) => <DriverRegistration {...globalProps} {...props}/>}/>
                  <Route path="/Customer" render={(props) => <CustomerLanding {...globalProps} {...props}/>}/>
                  <Route path="/Driver" render={(props) => <DriverLanding {...globalProps} {...props}/>}/>
                  <Route path="/TermsAndConditions" render={(props) => <TermsAndConditions {...globalProps} {...props}/>}/>
                  <Redirect to={App.INITIAL_ROOT_NAME}/>
                </Switch>
              </View>
            </StyleProvider>
          </AndroidBackButton>
        </NativeRouter>
      </Root>
    </Provider>;
  }
}
