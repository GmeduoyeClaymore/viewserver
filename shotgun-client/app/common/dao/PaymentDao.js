import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';

export default class PaymentDao{
  constructor(client) {
    this.client = client;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.name = 'paymentDao';
    this.state = {};
    this.subject.next();
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  updateState = (partialState) => {
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  deletePaymentCard = async({cardId}) => {
    const promise = this.client.invokeJSONCommand('userController', 'deletePaymentCard', {cardId});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error(`Could not detect deletion of payment card ${cardId} in 5 seconds`));
    Logger.debug(`Deleted card ${cardId}`);
    return paymentResponse;
  }

  addPaymentCard = async({paymentCard}) => {
    const promise = this.client.invokeJSONCommand('userController', 'addPaymentCard', {paymentCard});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error('Could not detect creation of payment card in 5 seconds'));
    Logger.debug('Added card for customer');
    return paymentResponse;
  }

  getBankAccount = async() => {
    const promise = this.client.invokeJSONCommand('paymentController', 'getBankAccount');
    const bankAccount =  await promise.timeoutWithError(5000, new Error('Could get bank account for stripe account for current user in 5 seconds'));
    Logger.debug(`Got bank account ${bankAccount}`);
    const result = {bankAccount};
    this.updateState(result);
    return result;
  }

  setBankAccount = async({paymentBankAccount, address}) => {
    const promise = this.client.invokeJSONCommand('partnerController', 'setBankAccount', {paymentBankAccount, address});
    const result =  await promise.timeoutWithError(5000, new Error('Could set bank account in 5 seconds'));
    Logger.debug('Set bank account');
    this.getBankAccount();
    return result;
  }

  updateSubscription = async() => {
    this.subject.next();
    return;
  }
}

PaymentDao.prototype.toJSON = function () {
  return { name: this.name };
};

