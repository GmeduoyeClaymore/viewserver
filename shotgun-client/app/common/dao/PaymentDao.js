import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';

export default class PaymentDao{
  constructor(client) {
    this.client = client;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.name = 'paymentDao';
    this.state = {};
    this.getCustomerPaymentCards = this.getCustomerPaymentCards.bind(this);
    this.deletePaymentCard = this.deletePaymentCard.bind(this);
    this.addPaymentCard = this.addPaymentCard.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.getBankAccount = this.getBankAccount.bind(this);
    this.setBankAccount = this.setBankAccount.bind(this);
    this.updateState = this.updateState.bind(this);
    this.subject.next();
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  updateState(partialState){
    const newState = {...this.state, ...partialState};
    this.state = newState;
    this.subject.next(newState);
  }

  async deletePaymentCard({cardId}){
    const promise = this.client.invokeJSONCommand('paymentController', 'deletePaymentCard', {cardId});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error(`Could not detect deletion of payment card ${cardId} in 5 seconds`));
    Logger.debug(`Deleted card ${cardId}`);
    this.getCustomerPaymentCards();
    return paymentResponse;
  }

  async addPaymentCard({paymentCard}){
    const promise = this.client.invokeJSONCommand('paymentController', 'addPaymentCard', {paymentCard});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error('Could not detect creation of payment card in 5 seconds'));
    Logger.debug('Added card for customer');
    this.getCustomerPaymentCards();
    return paymentResponse;
  }

  async getCustomerPaymentCards(){
    const promise = this.client.invokeJSONCommand('paymentController', 'getPaymentCards');
    const paymentCards =  await promise.timeoutWithError(5000, new Error('Could not get payment cards for customer in 5 seconds'));
    Logger.debug(`Got stripe payment cards ${JSON.stringify(paymentCards)}`);
    const result = {paymentCards};
    this.updateState(result);
    return result;
  }

  async getBankAccount(){
    const promise = this.client.invokeJSONCommand('paymentController', 'getBankAccount');
    const bankAccount =  await promise.timeoutWithError(5000, new Error('Could get bank account for stripe account for current user in 5 seconds'));
    Logger.debug(`Got bank account ${bankAccount}`);
    const result = {bankAccount};
    this.updateState(result);
    return result;
  }

  async setBankAccount({paymentBankAccount}){
    const promise = this.client.invokeJSONCommand('paymentController', 'setBankAccount', {paymentBankAccount});
    const result =  await promise.timeoutWithError(5000, new Error('Could get bank account for stripe account for current user in 5 seconds'));
    Logger.debug('Set bank account');
    this.getBankAccount();
    return result;
  }

  async updateSubscription(){
    this.subject.next();
    return;
  }
}

PaymentDao.prototype.toJSON = function () {
  return { name: this.name };
};

