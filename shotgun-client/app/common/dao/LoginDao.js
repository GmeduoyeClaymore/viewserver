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
    this.client.connection.connectionObservable.subscribe(this.handleConnectionStatusChanged);
  
    this.subject.next();
    this.state = {
      isLoggedIn: undefined,
      isConnected: this.client.connected
    };
    this.crx = crx;// force rx extensions to load
  }

   onRegister = async() => {
     await this.connectClientIfNotConnected();
     const userid = await PrincipalService.getUserIdFromDevice();
     if (userid){
       await this.loginByUserId(userid);
     } else {
       this.updateState({isLoggedIn: false});
     }
   }

   get observable(){
     return this.subject;
   }

   get optionsObservable(){
     return this.optionsSubject;
   }

  loginByUserId = async(userId) => {
    this.assertNotLoggedIn();
    await this.client.invokeJSONCommand('loginController', 'setUserId', userId);
    await this.doLoginStuff(userId);
    this.updateState({isLoggedIn: true});
    this.optionsSubject.next({userId});
  }

  loginUserByUsernameAndPassword = async({email, password}) => {
    this.assertNotLoggedIn();
    const userId = await this.client.invokeJSONCommand('loginController', 'login', {email, password});
    Logger.info(`User ${userId} logged in`);
    await this.doLoginStuff(userId);
    this.updateState({isLoggedIn: true});
    this.optionsSubject.next({email, password, userId});
    return userId;
  }

  registerAndLoginPartner = async({partner, vehicle, address, bankAccount}) => {
    Logger.info(`Registering partner ${partner.email}`);
    const partnerId = await this.client.invokeJSONCommand('partnerController', 'registerPartner', {user: {...partner, bankAccount, deliveryAddress: address}, vehicle});
    Logger.info(`Partner ${partnerId} registered`);
    await this.loginByUserId(partnerId);
    return partnerId;
  }

  registerAndLoginCustomer = async({customer, deliveryAddress, paymentCard}) => {
    Logger.info(`Registering customer ${customer.email}`);
    const customerId = await this.client.invokeJSONCommand('customerController', 'registerCustomer', {user: customer,  deliveryAddress, paymentCard});
    Logger.info(`Customer ${customerId} registered`);
    await this.loginByUserId(customerId);
    return customerId;
  }

  connectClientIfNotConnected = async() => {
    if (!this.client.connected){
      await this.client.connect(true);
    }
    this.client.connected;
  }

  handleConnectionStatusChanged = async(isConnected) => {
    if (isConnected){
      const userId = await PrincipalService.getUserIdFromDevice();
      if (userId){
        try {
          await this.client.invokeJSONCommand('loginController', 'setUserId', userId);
          await this.initMessaging();
          this.updateState({isConnected: true, isLoggedIn: true});
        } catch (error){
          Logger.error('Problem re logging you in ' + error );
          this.updateState({isConnected: true, isLoggedIn: false});
        }
      } else {
        this.updateState({isConnected: true, isLoggedIn: false});
      }
    } else {
      this.updateState({isConnected: false, isLoggedIn: false});
    }
  }

  doLoginStuff = async(userId) => {
    await PrincipalService.setUserIdOnDevice(userId);
    await this.initMessaging();
  }

  logOut = async() => {
    await PrincipalService.removeUserIdFromDevice();
    await this.client.invokeJSONCommand('loginController', 'logOut', {});
    this.updateState({isLoggedIn: false});
  }

  initMessaging = async() => {
    const token = await FCM.getFCMToken();
    const apnsToken = await FCM.getAPNSToken();
    await this.onChangeToken(token);
    Logger.info(`!!! Finished initializing firebase messaging fcm token is ${token} apns token is ${apnsToken}`);
  }

  onChangeToken = async(token) => {
    await this.client.invokeJSONCommand('messagingController', 'updateUserToken', token);
  }

  updateState = (partialState) => {
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  assertNotLoggedIn = () => {
    if (this.state.isLoggedIn){
      throw new Error('Cannot log in again already logged in ');
    }
  }
}

LoginDao.prototype.toJSON = function () {
  return { name: this.name };
};

