import Logger from 'common/Logger';
import PrincipalService from 'common/services/PrincipalService';
import Rx from 'rxjs/Rx';
import FCM from 'react-native-fcm';
import * as crx from 'common/rx';
export default class LoginDao{
  constructor(client) {
    this.client = client;
    this.name = 'loginDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.handleConnectionStatusChanged = this.handleConnectionStatusChanged.bind(this);
    this.client.connection.connectionObservable.subscribe(this.handleConnectionStatusChanged);
    this.subject.next();
    this.state = {
      isLoggedIn: false,
      isConnected: this.client.connected
    };
    this.crx = crx;// force rx extensions to load
    this.initMessaging = this.initMessaging.bind(this);
    this.loginByUserId = this.loginByUserId.bind(this);
    this.loginUserByUsernameAndPassword = this.loginUserByUsernameAndPassword.bind(this);
    this.registerAndLoginDriver = this.registerAndLoginDriver.bind(this);
    this.connectClientIfNotConnected = this.connectClientIfNotConnected.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.doLoginStuff = this.doLoginStuff.bind(this);
    this.logOut = this.logOut.bind(this);
    this.initMessaging = this.initMessaging.bind(this);
    this.onChangeToken = this.onChangeToken.bind(this);
    this.updateState = this.updateState.bind(this);
    this.onRegister = this.onRegister.bind(this);
  }

  async onRegister(){
    await this.connectClientIfNotConnected();
    const userid = await PrincipalService.getUserIdFromDevice();
    if (userid){
      this.loginByUserId(userid);
    }
  }

  async updateSubscription(){
    this.subject.next(this.state);
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  async loginByUserId(userId){
    this.assertNotLoggedIn();
    await this.client.invokeJSONCommand('loginController', 'setUserId', userId);
    await this.doLoginStuff(userId);
    this.updateState({isLoggedIn: true});
    this.optionsSubject.next({userId});
  }

  async loginUserByUsernameAndPassword({email, password}){
    this.assertNotLoggedIn();
    const userId = await this.client.invokeJSONCommand('loginController', 'login', {email, password});
    Logger.info(`User ${userId} logged in`);
    await this.doLoginStuff(userId);
    this.updateState({isLoggedIn: true});
    this.optionsSubject.next({email, password, userId});
    return userId;
  }

  async registerAndLoginDriver({driver, vehicle, address, bankAccount}){
    Logger.info(`Registering driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('driverController', 'registerDriver', {user: driver, vehicle, bankAccount, address});
    Logger.info(`Driver ${driverId} registered`);
    await loginByUserId(driverId);
    return driverId;
  }

  async connectClientIfNotConnected(){
    if (!this.client.connected){
      await this.client.connect(true);
    }
  }

  handleConnectionStatusChanged(isConnected){
    this.updateState({isConnected});
  }

  async doLoginStuff(userId){
    await PrincipalService.setUserIdOnDevice(userId);
    await this.initMessaging();
  }

  async logOut(){
    await PrincipalService.removeUserIdFromDevice();
    this.updateState({isLoggedIn: false});
  }

  async initMessaging(){
    const token = await FCM.getFCMToken();
    await this.onChangeToken(token);
    Logger.info('!!! Finished initializing firebase messaging');
  }

  async onChangeToken(token){
    await this.client.invokeJSONCommand('messagingController', 'updateUserToken', token);
  }

  updateState(partialState){
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  assertNotLoggedIn(){
    if (this.state.isLoggedIn){
      throw new Error('Cannot log in again already logged in ');
    }
  }
}

LoginDao.prototype.toJSON = function () {
  return { name: this.name };
};

