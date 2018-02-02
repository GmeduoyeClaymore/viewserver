import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';

export default class PaymentDao{
  constructor(client) {
    this.client = client;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.name = 'paymentDao';
    this.getCustomerPaymentCards = this.getCustomerPaymentCards.bind(this);
    this.deletePaymentCard = this.deletePaymentCard.bind(this);
    this.addPaymentCard = this.addPaymentCard.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.getBankAccount = this.getBankAccount.bind(this);
    this.setBankAccount = this.setBankAccount.bind(this);
    this.subject.next();
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  async deletePaymentCard({cardId}){
    const promise = this.client.invokeJSONCommand('paymentController', 'deletePaymentCard', {cardId});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error(`Could not detect deletion of payment card ${cardId} in 5 seconds`));
    Logger.debug(`Deleted card ${cardId}`);
    this.getCustomerPaymentCards(customerToken);
    return paymentResponse;
  }

  async addPaymentCard({customerToken, paymentCard}){
    const promise = this.client.invokeJSONCommand('paymentController', 'addPaymentCard', {paymentCard});
    const paymentResponse =  await promise.timeoutWithError(5000, new Error(`Could not detect creation of payment card for customer ${customerToken} in 5 seconds`));
    Logger.debug(`Added card for customer ${customerToken}`);
    this.getCustomerPaymentCards(customerToken);
    return paymentResponse;
  }

  async getCustomerPaymentCards(stripeCustomerToken){
    const promise = this.client.invokeJSONCommand('paymentController', 'getPaymentCards');
    const paymentCards =  await promise.timeoutWithError(5000, new Error(`Could not get payment cards for customer ${stripeCustomerToken} in 5 seconds`));
    Logger.fine(`Got stripe payment cards ${JSON.stringify(paymentCards)}`);
    const result = {paymentCards};
    this.subject.next(result);
    return result;
  }

  async getBankAccount(){
    const promise = this.client.invokeJSONCommand('paymentController', 'getBankAccount');
    const bankAccount =  await promise.timeoutWithError(5000, new Error('Could get bank account for stripe account for current user in 5 seconds'));
    Logger.fine(`Got bank account ${stripeAccountId}`);
    const result = {bankAccount};
    this.subject.next(result);
    return result;
  }

  async setBankAccount({stripeAccountId, paymentBankAccount}){
    const promise = this.client.invokeJSONCommand('paymentController', 'setBankAccount', {paymentBankAccount});
    const result =  await promise.timeoutWithError(5000, new Error('Could get bank account for stripe account for current user in 5 seconds'));
    Logger.fine(`Set bank account for ${stripeAccountId}`);
    this.getBankAccount({stripeAccountId});
    return result;
  }

  async updateSubscription(){
    this.subject.next();
    return;
  }
}
