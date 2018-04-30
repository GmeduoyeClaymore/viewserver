import Logger from 'common/Logger';
import PhoneCallService from 'common/services/PhoneCallService';
import Rx from 'rxjs/Rx';

export default class DriverDao{
  constructor(client) {
    this.client = client;
    this.name = 'driverDao';
    this.subject = new Rx.Subject();
    this.state = {};
    this.subject.next();
  }

  get observable(){
    return this.subject;
  }

  updateState = (partialState) => {
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  updateDriver = async({driver}) => {
    Logger.info(`Updating driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('userController', 'addOrUpdateUser', {user: driver});
    Logger.info(`Driver ${driverId} updated`);
    return driverId;
  }

  acceptOrderRequest = async({orderId}) => {
    await this.client.invokeJSONCommand('driverController', 'acceptOrder', {orderId});
  }

  startOrderRequest = async({orderId}) => {
    await this.client.invokeJSONCommand('driverController', 'startOrder', {orderId});
  }

  cancelOrderRequest = async({orderId}) => {
    await this.client.invokeJSONCommand('driverController', 'cancelOrder', {orderId});
  }

  completeOrderRequest = async({orderId}) => {
    await this.client.invokeJSONCommand('driverController', 'completeOrder', {orderId});
  }

  callCustomer = async({orderId}) => {
    const customerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getCustomerVirtualNumber', orderId);
    PhoneCallService.call(customerPhoneNumber);
  }

  rateCustomer = async({orderId, rating}) => {
    await this.client.invokeJSONCommand('orderController', 'addCustomerRating', {orderId,  rating});
  }

  getVehicleDetails = async({registrationNumber}) => {
    const vehicleDetails = await this.client.invokeJSONCommand('vehicleDetailsController', 'getDetails', registrationNumber);
    this.updateState({vehicleDetails});
  }
}

DriverDao.prototype.toJSON = function () {
  return { name: this.name };
};

