import Logger from 'common/Logger';
import PrincipalService from 'common/services/PrincipalService';
import Rx from 'rxjs/Rx';
import PhoneCallService from 'common/services/PhoneCallService';

export default class CustomerDao{
  constructor(client, orderDao) {
    this.client = client;
    this.orderDao = orderDao;
    this.name = 'customerDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.updateSubscription = this.updateSubscription.bind(this);
    this.registerCustomer = this.registerCustomer.bind(this);
    this.rejectDriver = this.rejectDriver.bind(this);
    this.updateCustomer = this.updateCustomer.bind(this);
    this.cancelOrder = this.cancelOrder.bind(this);
    this.checkout = this.checkout.bind(this);
    this.rateDriver = this.rateDriver.bind(this);
    this.callDriver = this.callDriver.bind(this);
    this.updateOrderPrice = this.updateOrderPrice.bind(this);
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
    this.client.loginUserById(customerId);
    return customerId;
  }

  async updateCustomer({customer}){
    Logger.info(`Updating customer ${customer.email}`);
    const customerId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: customer});
    Logger.info(`Customer ${customerId} updated`);
    return customerId;
  }


  async updateOrderPrice({orderId, price}) {
    await this.client.invokeJSONCommand('customerController', 'updateOrderPrice', {orderId, price});
  }

  async cancelOrder({orderId}){
    await this.client.invokeJSONCommand('customerController', 'cancelOrder', orderId);
  }

  async rejectDriver({orderId}){
    await this.client.invokeJSONCommand('customerController', 'rejectDriver', orderId);
  }

  async callDriver({orderId}){
    const driverPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getDriverVirtualNumber', orderId);
    PhoneCallService.call(driverPhoneNumber);
  }
 
  async checkout({orderItem, payment, product, delivery}){
    const orderId = await this.orderDao.createOrder({paymentId: payment.paymentId, product, delivery, orderItems: [{quantity: 1, ...orderItem}]});
    return orderId;
  }

  async rateDriver({orderId, rating}){
    await this.client.invokeJSONCommand('orderController', 'addDriverRating', {orderId,  rating});
  }
}

CustomerDao.prototype.toJSON = function () {
  return { name: this.name };
};

