import React from 'react';
import {UIManager, View} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {Container, Text, StyleProvider, Root, Spinner} from 'native-base';
import {Provider} from 'react-redux';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import LandingCommon from './LandingCommon';
import RegistrationCommon from './RegistrationCommon';
import CustomerRegistration from './customer/registration/CustomerRegistration';
import DriverRegistration from './driver/registration/DriverRegistration';
import TermsAndConditions from 'common/registration/TermsAndConditions';
import CustomerLanding from './customer/CustomerLanding';
import DriverLanding from './driver/DriverLanding';
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
  static INITIAL_ROOT_NAME = 'LandingCommon';

  constructor() {
    super();
    registerTokenListener();
    this.client = new Client('ws://shotgun.ltd:6060/');
    //this.client = new Client('ws://192.168.0.20:6060/');
    //this.client = new Client('ws://10.5.200.151:6060/');
    this.dispatch = store.dispatch;
  }

  async componentWillMount(){
    await ProtoLoader.loadAll();
    await FCM.requestPermissions({badge: false, sound: true, alert: true});
  }

  async componentDidMount() {
    const {client} = this;
    const {dispatch} = this.props;
    dispatch(loginServicesRegistrationAction(client));
  }
  
  render() {
    const {loginState, busy, user = {}} = this.props;
    const {isConnected, isLoggedIn} = (loginState || {});
    const globalProps = {client: this.client, userId: this.userId,  dispatch: this.dispatch, isConnected, isLoggedIn};
    const completeProps = {...globalProps, ...this.props};
    if (isLoggedIn === undefined || busy){
      if (!isConnected){
        return <LoadingScreen text={'Awaiting connection ..'}/>;
      }
      return <LoadingScreen text={'Awaiting login ..'}/>;
    }
    return   <Container style={{flexDirection: 'column', flex: 1}}>
      <ReactNativeModal
        isVisible={!isConnected}
        backdropOpacity={0.4}>
        <View style={styles.modalContainer}>
          <View style={styles.innerContainer}>
            <Spinner/>
            <Text>Awaiting Connection ....</Text>
          </View>
        </View>
      </ReactNativeModal>
      <Container style={{flex: 1}}>
        <Root>
          <StyleProvider style={getTheme(shotgun)}>
            <View style={{flex: 1, backgroundColor: '#ffffff'}}>
              <ReduxRouter path="/" name="AppRouter" defaultRoute={isLoggedIn ? 'LandingCommon' : 'RegistrationCommon' } {...completeProps}>
                <Route path="RegistrationCommon" exact component={RegistrationCommon}/>
                <Route path="Root" exact component={LandingCommon}/>
                <Route path="LandingCommon" exact component={LandingCommon}/>
                <Route path="Customer/Registration" component={CustomerRegistration}/>
                <Route path="Driver/Registration" component={DriverRegistration}/>
                <Route path="Landing" component={user.type == 'driver' ? DriverLanding : CustomerLanding}/>
                <Route path="Customer/Landing" component={CustomerLanding}/>
                <Route path="Driver/Landing" component={DriverLanding}/>
                <Route path="TermsAndConditions" component={TermsAndConditions}/>
              </ReduxRouter>
            </View>
          </StyleProvider>
        </Root>
      </Container>
    </Container>;
  }
}

const mapStateToProps = (state) => {
  return {
    user: getDaoState(state, ['user'], 'userDao'),
    busy: isAnyLoading(state, ['loginDao']),
    loginState: getDaoState(state, [], 'loginDao'),
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
