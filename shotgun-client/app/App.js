import React from 'react';
import {UIManager, View} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {Container, Text, StyleProvider, Root, Spinner} from 'native-base';
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
import {AndroidBackButton} from 'react-router-native';
import {LoadingScreen} from 'common/components';
import getTheme from './native-base-theme/components';
import shotgun from 'native-base-theme/variables/shotgun';
import FCM from 'react-native-fcm';
import {registerTokenListener} from 'common/Listeners';
import {ReduxRouter, Route} from 'custom-redux';

const store = configureStore();
if (UIManager.setLayoutAnimationEnabledExperimental){
  UIManager.setLayoutAnimationEnabledExperimental(true);
}


class TestComponent extends React.Component{
  constructor(props){
    super(props);
  }

  componentWillMount(){
    this.log('Component mounting');
  }

  render(){
    this.log('Component rendering');
    return <Text>Test Component</Text>;
  }

  componentWillUnmount(){
    this.log('Component unmounting');
  }

  log(message){
    Logger.info('TestComponent-' + message);
  }
}

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'LandingCommon';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false,
    };
    registerTokenListener();
    //this.client = new Client('ws://shotgun.ltd:6060/');
    this.client = new Client('ws://192.168.0.20:6060/');
    //this.client = new Client('ws://10.5.200.151:6060/');
    this.dispatch = store.dispatch;
    this.onChangeToken = this.onChangeToken.bind(this);
    this.setUserId = this.setUserId.bind(this);
    this.initMessaging = this.initMessaging.bind(this);
    this.handleConnectionStatusChanged = this.handleConnectionStatusChanged.bind(this);
    this.handleUserLoggedIn = this.handleUserLoggedIn.bind(this);
    this.client.connection.connectionObservable.subscribe(this.handleConnectionStatusChanged);
    this.client.loggedInObservable.subscribe(this.handleUserLoggedIn);
  }

  async componentDidMount() {
    await ProtoLoader.loadAll();
    await this.client.connect(true);
  }

  async handleConnectionStatusChanged(isReady, userId){
    Logger.info(`Connection status changed to :\"${isReady}\"`);
    if (isReady && !this.userId){
      try {
        await this.setUserId(userId);
        await this.initMessaging();
        this.setState({isReady, isConnected: isReady, hasBeenReady: true});
      } catch (error){
        this.setState({isReady: true, isConnected: false});
      }
    } else {
      this.userId = undefined;
      this.setState({ isReady, isConnected: isReady});
    }
  }

  handleUserLoggedIn(userId){
    this.userId = userId;
    this.setState({userId});
  }

  async initMessaging(){
    Logger.info('......Initializing firebase messaging');
    if (!this.userId){
      Logger.warning('No userid has been specified not initializing messaging');
      return;
    }

    await FCM.requestPermissions({badge: false, sound: true, alert: true});
    const token = await FCM.getFCMToken();
    await this.onChangeToken(token);
    this.setState({token: token || ''});
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
      await this.client.loginUserById(userId);
    }
    Logger.info(`!!! Logged in as ${this.userId} from device`);
  }

  async signOut(){
    await PrincipalService.removeUserIdFromDevice();
  }


  render() {
    const {isReady, hasBeenReady} = this.state;
    if (!isReady && !hasBeenReady){
      return <LoadingScreen text="Connecting"/>;
    }

    const globalProps = {client: this.client, userId: this.userId, dispatch: this.dispatch, isReady};
    const completeProps = {...globalProps, ...this.props};

    return <Container style={{flexDirection: 'column', flex: 1}}>
      <Container style={{flex: 1}}>
        <Provider store={store}>
          <Root>
            <StyleProvider style={getTheme(shotgun)}>
              <View style={{flex: 1, backgroundColor: '#ffffff'}}>
                <ReduxRouter name="AppRouter" defaultRoute={this.userId  ? '/LandingCommon' : '/RegistrationCommon' } {...completeProps}>
                  <Route path="/RegistrationCommon" exact component={RegistrationCommon}/>
                  <Route path="/Root" exact component={LandingCommon}/>
                  <Route path="/LandingCommon" exact component={LandingCommon}/>
                  <Route path="/Customer/Registration" component={CustomerRegistration}/>
                  <Route path="/Driver/Registration" component={DriverRegistration}/>
                  <Route path="/Customer/Landing" component={CustomerLanding}/>
                  <Route path="/Driver/Landing" component={DriverLanding}/>
                  <Route path="/TermsAndConditions" component={TermsAndConditions}/>
                </ReduxRouter>
              </View>
            </StyleProvider>
          </Root>
        </Provider>
      </Container>
    </Container>;
  }
}

const styles = {
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  innerContainer: {
    alignItems: 'center',
  },
};
