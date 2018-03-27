import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';

export default class OrdersDao{
  constructor(client) {
    this.client = client;
    this.name = 'orderDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.createOrder = this.createOrder.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
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

  async createOrder({delivery, paymentId, product, orderItems}) {
    Logger.info('Creating order');
    const orderId = await this.client.invokeJSONCommand('orderController', 'createOrder', {paymentId, product, delivery, orderItems});
    Logger.info(`Order ${orderId} created`);
    return orderId;
  }
}

OrdersDao.prototype.toJSON = function () {
  return { name: this.name };
};

