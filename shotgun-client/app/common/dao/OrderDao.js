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
    invariant(resources.Controller, 'Unable to find controller within resource dictionary');
    return  resources.Controller;
  }

  rateUserOrder = async({orderId, rating, comments, ratingType}) => {
    await this.client.invokeJSONCommand('userController', 'addOrUpdateRating', {orderId,  rating, comments, ratingType});
  };

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
    const controller = this.getControllerForOrder(orderContentTypeId);
    const paymentStageId = await this.client.invokeJSONCommand(controller, 'addPaymentStage', {orderId, amount, name, description, paymentStageType});
    Logger.info(`Order payment stage ${paymentStageId} created`);
    return paymentStageId;
  }

  removePaymentStage = async ({orderId, paymentStageId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'removePaymentStage', {orderId, paymentStageId});
    Logger.info(`Order payment stage ${paymentStageId} removed`);
    return paymentStageId;
  }


  payForPaymentStage = async ({orderId, paymentStageId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'payForPaymentStage', {orderId, paymentStageId});
    Logger.info(`Order payment stage ${paymentStageId} removed`);
    return paymentStageId;
  }

  completePaymentStage = async ({orderId, paymentStageId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'completePaymentStage', {orderId, paymentStageId});
    Logger.info(`Order payment stage ${paymentStageId} complete`);
    return paymentStageId;
  }

  startPaymentStage = async ({orderId, paymentStageId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'startPaymentStage', {orderId, paymentStageId});
    Logger.info(`Order payment stage ${paymentStageId} complete`);
    return paymentStageId;
  }

  startJourney = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const stage = await this.client.invokeJSONCommand(controller, 'startJourney', {orderId});
    Logger.info(`Order ${orderId} journey started`);
    return stage;
  }

  completeJourney = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const stage = await this.client.invokeJSONCommand(controller, 'completeJourney', {orderId});
    Logger.info(`Order ${orderId} journey completed`);
    return stage;
  }

  logDayStart = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const paymentStageId = this.client.invokeJSONCommand(controller, 'logDayStarted', {orderId});
    Logger.info(`Order ${orderId} day started`);
    return paymentStageId;
  }

  logDayComplete = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const paymentStageId = this.client.invokeJSONCommand(controller, 'logDayComplete', {orderId});
    Logger.info(`Order ${orderId} day completed`);
    return paymentStageId;
  }

  customerCompleteAndPay = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const paymentId = this.client.invokeJSONCommand(controller, 'customerCompleteAndPay', {orderId});
    Logger.info(`Order ${orderId} day completed`);
    return paymentId;
  }

  cancelResponsePartner = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const result = this.client.invokeJSONCommand(controller, 'cancelResponsePartner', {orderId});
    Logger.info(`Response to  ${orderId} from partner cancelled`);
    return result;
  }

  cancelResponseCustomer = async ({orderId, partnerId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    const result = this.client.invokeJSONCommand(controller, 'cancelResponseCustomer', {orderId, partnerId});
    Logger.info(`Response to  ${orderId} from partner cancelled`);
    return result;
  }

  offHireItem = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'offHireItem', {orderId});
    Logger.info(`Order ${orderId} item off hired`);
    return paymentStageId;
  }

  markItemReady = async ({orderId, orderContentTypeId}) =>  {
    const controller = this.getControllerForOrder(orderContentTypeId);
    this.client.invokeJSONCommand(controller, 'markItemReady', {orderId});
    Logger.info(`Order ${orderId} item marked ready`);
    return paymentStageId;
  }

  calculatePriceEstimate = async ({order}) =>  {
    const controller = this.getControllerForOrder(order.orderContentTypeId);
    const calculatedPrice = this.client.invokeJSONCommand(controller, 'calculatePriceEstimate', {order});
    Logger.info(`Orderprice ${calculatedPrice} has been calculated item marked ready`);
    return calculatedPrice;
  }
}

OrdersDao.prototype.toJSON = function () {
  return { name: this.name };
};

