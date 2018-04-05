import Logger from 'common/Logger';
import PhoneCallService from 'common/services/PhoneCallService';

export default class DriverDao{
  constructor(client) {
    this.client = client;
    this.name = 'driverDao';
    this.updateDriver = this.updateDriver.bind(this);
    this.acceptOrderRequest = this.acceptOrderRequest.bind(this);
    this.startOrderRequest = this.startOrderRequest.bind(this);
    this.cancelOrderRequest = this.cancelOrderRequest.bind(this);
    this.completeOrderRequest = this.completeOrderRequest.bind(this);
    this.rateCustomer = this.rateCustomer.bind(this);
    this.callCustomer = this.callCustomer.bind(this);
  }

  async updateDriver({driver}){
    Logger.info(`Updating driver ${driver.email}`);
    const driverId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: driver});
    Logger.info(`Driver ${driverId} updated`);
    return driverId;
  }

  async acceptOrderRequest({orderId}) {
    await this.client.invokeJSONCommand('driverController', 'acceptOrder', {orderId});
  }

  async startOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'startOrder', {orderId});
  }

  async cancelOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'cancelOrder', {orderId});
  }

  async completeOrderRequest({orderId}){
    await this.client.invokeJSONCommand('driverController', 'completeOrder', {orderId});
  }

  async callCustomer({orderId}){
    const customerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getCustomerVirtualNumber', orderId);
    PhoneCallService.call(customerPhoneNumber);
  }

  async rateCustomer({orderId, rating}){
    await this.client.invokeJSONCommand('orderController', 'addCustomerRating', {orderId,  rating});
  }
}

DriverDao.prototype.toJSON = function () {
  return { name: this.name };
};

