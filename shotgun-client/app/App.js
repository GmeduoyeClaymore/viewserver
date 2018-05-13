import React from 'react';
import {UIManager, View, TouchableWithoutFeedback, Keyboard, AppState} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {Text, StyleProvider, Root, Spinner} from 'native-base';
import {Provider} from 'react-redux';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import LandingCommon from './LandingCommon';
import RegistrationCommon from './RegistrationCommon';
import CustomerRegistration from './customer/registration/CustomerRegistration';
import PartnerRegistration from './partner/registration/PartnerRegistration';
import TermsAndConditions from 'common/registration/TermsAndConditions';
import CustomerLanding from './customer/CustomerLanding';
import PartnerLanding from './partner/PartnerLanding';
import getTheme from './native-base-theme/components';
import shotgun from 'native-base-theme/variables/shotgun';
import FCM from 'react-native-fcm';
import {registerTokenListener} from 'common/Listeners';
import {ReduxRouter, Route, connect} from 'custom-redux';
import {loginServicesRegistrationAction} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState, getLoadingError} from 'common/dao';
import {LoadingScreen} from 'common/components';

const store = configureStore();
if (UIManager.setLayoutAnimationEnabledExperimental){
  UIManager.setLayoutAnimationEnabledExperimental(true);
}


class App extends React.Component {
  constructor() {
    super();
    registerTokenListener();
    this.client = new Client('ws://shotgun.ltd:6060/');
    //this.client = new Client('ws://192.168.0.20:6060/');
    //this.client = new Client('ws://10.5.200.151:6060/');
    this.dispatch = store.dispatch;
    this.state = {
      appState: AppState.currentState
    };
  }

  async componentWillMount(){
    await ProtoLoader.loadAll();
    await FCM.requestPermissions({badge: false, sound: true, alert: true});
  }

  async componentDidMount() {
    const {client} = this;
    const {dispatch} = this.props;
    dispatch(loginServicesRegistrationAction(client));
    AppState.addEventListener('change', this._handleAppStateChange);
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }

  _handleAppStateChange = (nextAppState) => {
    if (nextAppState === 'inactive'){
      this.client.disconnect();
    }
    if (this.state.appState.match(/inactive|background/) && nextAppState === 'active') {
      this.client.connect();
    }
    this.setState({appState: nextAppState});
  }
  
  render() {
    const {loginState, busy} = this.props;
    const {isConnected, isLoggedIn} = loginState;
    const globalProps = {client: this.client, userId: this.userId,  dispatch: this.dispatch, isConnected, isLoggedIn};
    const completeProps = {...globalProps, ...this.props};
    if (busy || isLoggedIn == undefined){
      return !isConnected ? <LoadingScreen text={'Connecting'}/> : <LoadingScreen text={'Signing You In'}/>;
    }
    return [<ReactNativeModal key='connectingModal'
      isVisible={!isConnected}
      backdropOpacity={0.4}>
      <View style={styles.modalContainer}>
        <View style={styles.innerContainer}>
          <Spinner/>
          <Text>Reconnecting</Text>
        </View>
      </View>
    </ReactNativeModal>,
    <TouchableWithoutFeedback key='root' onPress={() => Keyboard.dismiss()}><Root>
      <StyleProvider style={{...getTheme(shotgun)}}>
        <ReduxRouter path="/" name="AppRouter" defaultRoute={isLoggedIn ? 'LandingCommon' : 'RegistrationCommon' } {...completeProps}>
          <Route path="RegistrationCommon" exact component={RegistrationCommon}/>
          <Route path="Root" exact component={LandingCommon}/>
          <Route path="LandingCommon" exact component={LandingCommon}/>
          <Route path="Customer/Registration" component={CustomerRegistration}/>
          <Route path="Partner/Registration" component={PartnerRegistration}/>
          <Route path="Customer/Landing" component={CustomerLanding}/>
          <Route path="Partner/Landing" component={PartnerLanding}/>
          <Route path="TermsAndConditions" component={TermsAndConditions}/>
        </ReduxRouter>
      </StyleProvider>
    </Root></TouchableWithoutFeedback>];
  }
}

const mapStateToProps = (state) => {
  return {
    busy: isAnyLoading(state, ['loginDao']),
    loginState: getDaoState(state, [], 'loginDao') || {},
    errors: getLoadingError(state, 'loginDao')
  };
};

const ConnectedApp = connect(mapStateToProps)(App);

class AppWrapper extends React.Component {
  constructor(props){
    super(props);
  }
  render(){
    return <Provider store={store}>
      <ConnectedApp {...this.props}/>
    </Provider>;
  }
}
export default AppWrapper;

const styles = {
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  innerContainer: {
    alignItems: 'center',
  },
};
