import Logger from 'common/Logger';
import PrincipalService from 'common/services/PrincipalService';
import Rx from 'rxjs/Rx';

export default class CustomerDao{
  constructor(client, orderDao) {
    this.client = client;
    this.orderDao = orderDao;
    this.name = 'customerDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.updateSubscription = this.updateSubscription.bind(this);
    this.registerCustomer = this.registerCustomer.bind(this);
    this.updateCustomer = this.updateCustomer.bind(this);
    this.loginCustomer = this.loginCustomer.bind(this);
    this.checkout = this.checkout.bind(this);
    this.rateDriver = this.rateDriver.bind(this);
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

  async registerCustomer({customer, deliveryAddress, paymentCard}){
    Logger.info(`Registering customer ${customer.email}`);
    const customerId = await this.client.invokeJSONCommand('customerController', 'registerCustomer', {user: customer,  deliveryAddress, paymentCard});
    Logger.info(`Customer ${customerId} registered`);
    await PrincipalService.setUserIdOnDevice(customerId);
    return customerId;
  }

  async updateCustomer({customer}){
    const {userId} = this.options;
    Logger.info(`Updating customer ${customer.email}`);
    const customerId = await this.client.invokeJSONCommand('userController', 'updateUser', {userId, user: customer});
    Logger.info(`Customer ${customerId} updated`);
    return customerId;
  }

  async loginCustomer({email, password}){
    const customerId = await this.client.invokeJSONCommand('loginController', 'login', {email, password});
    Logger.info(`Customer ${customerId} logged in`);
    await PrincipalService.setUserIdOnDevice(customerId);
    return customerId;
  }

  async checkout({orderItem, payment, product, delivery}){
    const orderId = await this.orderDao.createOrder({paymentId: payment.paymentId, product, delivery, orderItems: [{quantity: 1, ...orderItem}]});
    return orderId;
  }

  async rateDriver({deliveryId, rating}){
    await this.client.invokeJSONCommand('deliveryController', 'addDriverRating', {deliveryId, rating});
  }
}

