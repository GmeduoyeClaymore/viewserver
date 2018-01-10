import Logger from 'common/Logger';
// import PrincipalService from 'common/services/PrincipalService';
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
    this.checkout = this.checkout.bind(this);
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
    //await PrincipalService.setUserIdOnDevice(driverId);
    return customerId;
  }

  async checkout({orderItem, payment, delivery}){
    const orderId = await this.orderDao.createOrder({paymentId: payment.paymentId, delivery, orderItems: [{quantity: 1, ...orderItem}]});
    return orderId;
  }
}

