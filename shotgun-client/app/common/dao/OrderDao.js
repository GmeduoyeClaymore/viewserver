import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';
import * as ContentTypes from 'common/constants/ContentTypes';
import invariant from 'invariant';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('Controller').
    personell('personellOrderController').
    rubbish('rubbishOrderController').
    skip('hireOrderController').
    delivery('deliveryOrderController').
    hire('hireOrderController');
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

  getControllerForOrder(orderContentTypeId){
    Logger.info('Creating order');
    invariant(orderContentTypeId, 'Order content type should be defined');
    const resources = resourceDictionary.resolve(orderContentTypeId);
    invariant(resources, 'Unable to find resource dictionary for content type ' + orderContentTypeId);
    return  resources.Controller;
  }

  createOrder = async ({order, paymentId}) => {
    const controller = this.getControllerForOrder(order.orderContentTypeId);
    const orderId = await this.client.invokeJSONCommand(controller, 'createOrder', {paymentId, order});
    Logger.info(`Order ${orderId} created`);
    return orderId;
  }

  cancelOrder = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    await this.client.invokeJSONCommand(controller, 'cancelOrder', {orderId});
    Logger.info(`Order ${orderId} cancelled`);
    return orderId;
  }

  rejectResponse = async ({orderId, partnerId, orderContentTypeId}) => {
    const controller = this.getControllerForOrder(orderContentTypeId);
    await this.client.invokeJSONCommand(controller, 'rejectResponse', {orderId, partnerId});
    Logger.info(`Order ${orderId} partner ${partnerId} rejected`);
    return orderId;
  }

  acceptResponse = async ({orderId, partnerId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    await this.client.invokeJSONCommand(controller, 'acceptResponse', {orderId, partnerId});
    Logger.info(`Order ${orderId} partner ${partnerId} accepted`);
    return orderId;
  }

  respondToOrder = async({orderId, orderContentTypeId, requiredDate, amount}) => {
    const controller = this.getControllerForOrder(orderContentTypeId);
    await this.client.invokeJSONCommand(controller, 'respondToOrder', {orderId, requiredDate, amount});
  };

  updateOrderAmount = async ({orderId, amount, orderContentTypeId}) => {
    const controller = this.getControllerForOrder(orderContentTypeId);
    await this.client.invokeJSONCommand(controller, 'updateOrderAmount', {orderId, amount});
    Logger.info(`Order ${orderId} amount ${amount} updated`);
    return orderId;
  };

  addPaymentStage = async ({orderId, orderContentTypeId, amount, name, description, paymentStageType}) =>  {
    const {controller} = this.getControllerForOrder(orderContentTypeId);
    const paymentStageId = await this.client.invokeJSONCommand(controller, 'addPaymentStage', {orderId, amount, name, description, paymentStageType});
    Logger.info(`Order payment stage ${paymentStageId} created`);
    return paymentStageId;
  }
}

OrdersDao.prototype.toJSON = function () {
  return { name: this.name };
};

