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
  }

  get observable(){
    return this.subject;
  }

  updateState = (partialState) => {
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  updatePartner = async({partner}) => {
    Logger.info(`Updating partner ${partner.email}`);
    const partnerId = await this.client.invokeJSONCommand('userController', 'updateUser', {user: partner});
    Logger.info(`Partner ${partnerId} updated`);
    return partnerId;
  }

  callCustomer = async({orderId}) => {
    const customerPhoneNumber = await this.client.invokeJSONCommand('phoneCallController', 'getCustomerVirtualNumber', orderId);
    PhoneCallService.call(customerPhoneNumber);
  }

  getVehicleDetails = async({registrationNumber}) => {
    const vehicleDetails = await this.client.invokeJSONCommand('vehicleDetailsController', 'getDetails', registrationNumber);
    this.updateState({vehicleDetails});
  }
}

PartnerDao.prototype.toJSON = function () {
  return { name: this.name };
};

