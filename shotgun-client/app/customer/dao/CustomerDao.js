import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';
import PhoneCallService from 'common/services/PhoneCallService';

export default class CustomerDao{
  constructor(client) {
    this.client = client;
    this.name = 'customerDao';
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.updateCustomer = this.updateCustomer.bind(this);
    this.ratePartner = this.ratePartner.bind(this);
    this.callPartner = this.callPartner.bind(this);
  }

  async updateCustomer({customer}){
    Logger.info(`Updating customer ${customer.email}`);
    const customerId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: customer});
    Logger.info(`Customer ${customerId} updated`);
    return customerId;
  }

  async callPartner({orderId}){
    const partnerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getPartnerVirtualNumber', orderId);
    PhoneCallService.call(partnerPhoneNumber);
  }

  async ratePartner({orderId, rating}){
    await this.client.invokeJSONCommand('orderController', 'addPartnerRating', {orderId,  rating});
  }
}

CustomerDao.prototype.toJSON = function () {
  return { name: this.name };
};

