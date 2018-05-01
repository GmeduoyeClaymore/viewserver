import Logger from 'common/Logger';
import PhoneCallService from 'common/services/PhoneCallService';
import Rx from 'rxjs/Rx';

export default class PartnerDao{
  constructor(client) {
    this.client = client;
    this.name = 'partnerDao';
    this.subject = new Rx.Subject();
    this.state = {};
    this.subject.next();
    this.updatePartner = this.updatePartner.bind(this);
    this.acceptOrderRequest = this.acceptOrderRequest.bind(this);
    this.startOrderRequest = this.startOrderRequest.bind(this);
    this.cancelOrderRequest = this.cancelOrderRequest.bind(this);
    this.completeOrderRequest = this.completeOrderRequest.bind(this);
    this.rateCustomer = this.rateCustomer.bind(this);
    this.callCustomer = this.callCustomer.bind(this);
    this.getVehicleDetails = this.getVehicleDetails.bind(this);
    this.updateState = this.updateState.bind(this);
  }

  get observable(){
    return this.subject;
  }

  updateState(partialState){
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  async updatePartner({partner}){
    Logger.info(`Updating partner ${partner.email}`);
    const partnerId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: partner});
    Logger.info(`Partner ${partnerId} updated`);
    return partnerId;
  }

  async acceptOrderRequest({orderId}) {
    await this.client.invokeJSONCommand('partnerController', 'acceptOrder', {orderId});
  }

  async startOrderRequest({orderId}){
    await this.client.invokeJSONCommand('partnerController', 'startOrder', {orderId});
  }

  async cancelOrderRequest({orderId}){
    await this.client.invokeJSONCommand('partnerController', 'cancelOrder', {orderId});
  }

  async completeOrderRequest({orderId}){
    await this.client.invokeJSONCommand('partnerController', 'completeOrder', {orderId});
  }

  async callCustomer({orderId}){
    const customerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getCustomerVirtualNumber', orderId);
    PhoneCallService.call(customerPhoneNumber);
  }

  async rateCustomer({orderId, rating}){
    await this.client.invokeJSONCommand('orderController', 'addCustomerRating', {orderId,  rating});
  }

  async getVehicleDetails({registrationNumber}){
    const vehicleDetails = await this.client.invokeJSONCommand('vehicleDetailsController', 'getDetails', registrationNumber);
    this.updateState({vehicleDetails});
  }
}

PartnerDao.prototype.toJSON = function () {
  return { name: this.name };
};

