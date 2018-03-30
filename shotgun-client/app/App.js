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
import AddPropsToRoute from 'common/AddPropsToRoute';
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
    this.client.connection.connectionObservable.subscribe(this.handleConnectionStatusChanged);
  }

  async componentDidMount() {
    await ProtoLoader.loadAll();
    await this.client.connect(true);
  }

  async handleConnectionStatusChanged(isReady){
    Logger.info(`Connection status changed to :\"${isReady}\"`);
    if (isReady){
      try {
        await this.setUserId();
        await this.initMessaging();
        this.setState({isReady, isConnected: isReady, hasBeenReady: true});
      } catch (error){
        this.setState({isReady: true, isConnected: false});
      }
    } else {
      this.setState({ isReady, isConnected: isReady});
    }
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
    Logger.info(`!!! Got user id ${this.userId} from device`);
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
    const RegistrationCommonPage = AddPropsToRoute(RegistrationCommon, completeProps);
    const LandingCommonPage = AddPropsToRoute(LandingCommon, completeProps);

    return <Container style={{flexDirection: 'column', flex: 1}}>
      <Container style={{flex: 1}}>
        <Provider store={store}>
          <Root>
            <StyleProvider style={getTheme(shotgun)}>
              <View style={{flex: 1, backgroundColor: '#ffffff'}}>
                <ReduxRouter defaultRoute="/Root">
                  <Route path="/Root" component={this.userId ? LandingCommonPage : RegistrationCommonPage }/>
                  <Route path="/RegistrationCommon" exact component={RegistrationCommonPage}/>
                  <Route path="/LandingCommon" exact component={LandingCommonPage}/>
                  <Route path="/Customer/Registration" component={AddPropsToRoute(CustomerRegistration, completeProps)}/>
                  <Route path="/Driver/Registration" component={AddPropsToRoute(DriverRegistration, completeProps)}/>
                  <Route path="/Customer/Landing" component={AddPropsToRoute(CustomerLanding, completeProps)}/>
                  <Route path="/Driver/Landing" component={AddPropsToRoute(DriverLanding, completeProps)}/>
                  <Route path="/TermsAndConditions" component={AddPropsToRoute(TermsAndConditions, completeProps)}/>
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
