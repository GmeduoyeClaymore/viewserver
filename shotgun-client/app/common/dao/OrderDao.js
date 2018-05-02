import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';
import * as ContentTypes from 'common/constants/ContentTypes';
import invariant from 'invariant';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('getControllerName').
    personell(() => 'personellOrderController').
    rubbish(() => 'rubbishOrderController').
    skip(() => 'hireOrderController').
    delivery(order => order.sourceOrderContentType ? getController(order.sourceOrderContentType) :  'deliveryOrderController').
    hire(() => 'hireOrderController').
  property('getControllerAction', order => 'createOrder').
    delivery(order => order.sourceOrderContentType ? 'createDeliveryOrder' :  'createOrder');
/*eslint-enable */

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

  async createOrder({order, paymentId}) {
    Logger.info('Creating order');
    invariant(order.orderContentTypeId, 'Order content type should be defined');
    const resources = resourceDictionary.resolve(order.orderContentTypeId);
    invariant(resources, 'Unable to find resource dictionary for content type ' + order.orderContentTypeId);
    const controller = resources.getControllerName(order);
    const action = resources.getControllerAction(order);
    invariant(controller, 'Unable to get controller name for order');
    invariant(action, 'Unable to get action for order');
    const orderId = await this.client.invokeJSONCommand(controller, action, {paymentId, order});
    Logger.info(`Order ${orderId} created`);
    return orderId;
  }
}

OrdersDao.prototype.toJSON = function () {
  return { name: this.name };
};

