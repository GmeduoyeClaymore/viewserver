import Logger from 'common/Logger';
import Rx from 'rxjs/Rx';

export default class PaymentDao{
  constructor(client) {
    this.client = client;
    this.subject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.name = 'paymentDao';
    this.createPaymentCustomer = this.createPaymentCustomer.bind(this);
    this.getCustomerPaymentCards = this.getCustomerPaymentCards.bind(this);
    this.updateSubscription = this.updateSubscription.bind(this);
    this.subject.next();
  }

  get observable(){
    return this.subject;
  }

  get optionsObservable(){
    return this.optionsSubject;
  }

  async createPaymentCustomer(paymentCustomer){
    const promise = this.client.invokeJSONCommand('paymentController', 'createPaymentCustomer', paymentCustomer);
    const paymentResponse =  await promise.timeoutWithError(5000, new Error(`Could not detect creation of payment customer ${paymentCustomer.email} in 5 seconds`));
    Logger.debug(`Got stripe payment details ${JSON.stringify(paymentResponse)}`)
    this.subject.next(paymentResponse);
    return paymentResponse;
  }

  async getCustomerPaymentCards(stripeCustomerToken){
    const promise = this.client.invokeJSONCommand('paymentController', 'getPaymentCards', stripeCustomerToken);
    const getCardsResponse =  await promise.timeoutWithError(2000, new Error(`Could not get payment cards for customer ${stripeCustomerToken} in 2 seconds`));
    Logger.debug(`Got stripe payment cards ${JSON.stringify(getCardsResponse)}`);
    this.subject.next(getCardsResponse);
    return getCardsResponse;
  }

  async updateSubscription(){
    this.subject.next();
    return;
  }
}

