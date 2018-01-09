import Logger from 'common/Logger';
//import PrincipalService from 'common/services/PrincipalService';
import Rx from 'rxjs/Rx';

export default class DriverDao{
  constructor(client) {
    this.client = client;
    this.name = 'driverDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.updateSubscription = this.updateSubscription.bind(this);
    this.registerDriver = this.registerDriver.bind(this);
    this.acceptOrderRequest = this.acceptOrderRequest.bind(this);
    this.startOrderRequest = this.startOrderRequest.bind(this);
    this.cancelOrderRequest = this.cancelOrderRequest.bind(this);
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

  async registerDriver({driver, vehicle}){
    Logger.info(`Registering driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('driverController', 'registerDriver', {user: driver, vehicle});
    Logger.info(`Driver ${driverId} registered`);
    //await PrincipalService.setUserIdOnDevice(driverId);
    return driverId;
  }

  async acceptOrderRequest({orderId}) {
    const {userId} = this.options;
    await this.client.invokeJSONCommand('driverController', 'acceptOrder', {driverId: userId, orderId});
  }

  async startOrderRequest({orderId}){
    const {userId} = this.options;
    await this.client.invokeJSONCommand('driverController', 'startOrder', {driverId: userId, orderId});
  }

  async cancelOrderRequest({orderId}){
    const {userId} = this.options;
    await this.client.invokeJSONCommand('driverController', 'cancelOrder', {driverId: userId, orderId});
  }
}

