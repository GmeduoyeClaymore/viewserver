import Logger from 'common/Logger';
import PrincipalService from 'common/services/PrincipalService';
import Rx from 'rxjs/Rx';
import RNImmediatePhoneCall from 'react-native-immediate-phone-call';

export default class DriverDao{
  constructor(client) {
    this.client = client;
    this.name = 'driverDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.updateSubscription = this.updateSubscription.bind(this);
    this.registerDriver = this.registerDriver.bind(this);
    this.updateDriver = this.updateDriver.bind(this);
    this.loginDriver = this.loginDriver.bind(this);
    this.acceptOrderRequest = this.acceptOrderRequest.bind(this);
    this.startOrderRequest = this.startOrderRequest.bind(this);
    this.cancelOrderRequest = this.cancelOrderRequest.bind(this);
    this.completeOrderRequest = this.completeOrderRequest.bind(this);
    this.rateCustomer = this.rateCustomer.bind(this);
    this.callCustomer = this.callCustomer.bind(this);
    this.subject.next();
    this.options = {};
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  async updateSubscription(options){
    this.options = {...this.options, ...options};
    this.subject.next();
    return;
  }

  async registerDriver({driver, vehicle, address, bankAccount}){
    Logger.info(`Registering driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('driverController', 'registerDriver', {user: driver, vehicle, bankAccount, address});
    Logger.info(`Driver ${driverId} registered`);
    await PrincipalService.setUserIdOnDevice(driverId);
    return driverId;
  }

  async updateDriver({driver}){
    Logger.info(`Updating driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: driver});
    Logger.info(`Driver ${driverId} updated`);
    return driverId;
  }

  async loginDriver({email, password}){
    const driverId = await this.client.invokeJSONCommand('loginController', 'login', {email, password});
    Logger.info(`Driver ${driverId} logged in`);
    await PrincipalService.setUserIdOnDevice(driverId);
    return driverId;
  }

  async acceptOrderRequest({orderId}) {
    await this.client.invokeJSONCommand('driverController', 'acceptOrder', {orderId});
  }

  async startOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'startOrder', {orderId});
  }

  async cancelOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'cancelOrder', {orderId});
  }

  async completeOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'completeOrder', {orderId});
  }

  async callCustomer({orderId}){
    const customerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getCustomerVirtualNumber', orderId);
    RNImmediatePhoneCall.immediatePhoneCall(`+${customerPhoneNumber}`);
  }

  async rateCustomer({orderId, rating}){
    await this.client.invokeJSONCommand('orderController', 'addCustomerRating', {orderId,  rating});
  }
}

