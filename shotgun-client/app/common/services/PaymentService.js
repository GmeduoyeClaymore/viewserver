import Logger from 'common/Logger';

//TODO - this should come from the manifest
const STRIPE_PUBLIC_KEY = 'pk_test_BUWd5f8iUuxmbTT5MqsdOlmk';
//TODO - we have to find a way to store this on the viewserver rather than on the device.
const STRIPE_PRIVATE_KEY = 'sk_test_a36Vq8WXGWEf0Jb55tUUdXD4';
const STRIPE_URL = 'https://api.stripe.com/v1';

export default class PaymentService {
  static async createCardToken(cardNumber, expMonth, expYear, cvc){
    try {
      const params = {
        'card[number]': cardNumber,
        'card[exp_month]': expMonth,
        'card[exp_year]': expYear,
        'card[cvc]': cvc
      };

      const response = await PaymentService.sendToStripe(params, `${STRIPE_URL}/tokens`, STRIPE_PUBLIC_KEY);
      Logger.info(`Created stripe token ${response.id}`);
      return response.id;
    } catch (err) {
      //TODO - better handling here
      Logger.error(err);
    }
  }

  static async createCustomer(cardToken, email){
    try {
      const params = {
        source: cardToken,
        email
      };

      const response = await PaymentService.sendToStripe(params, `${STRIPE_URL}/customers`, STRIPE_PRIVATE_KEY);
      Logger.info(`Created stripe customer with ${response.id}`);
      return response.id;
    } catch (err) {
      //TODO - better handling here
      Logger.error(err);
    }
  }

  static async getCustomerCards(customerPaymentId){
    try {
      const response = await PaymentService.sendToStripe({}, `${STRIPE_URL}/customers/${customerPaymentId}/sources?object=card`, STRIPE_PRIVATE_KEY, 'get');
      Logger.info(`Got ${response.data.length} cards for customer ${customerPaymentId}`);
      return response.data.map(c => {return {paymentId: c.id, number: `************${c.last4}`, brand: c.brand, expiry: `${c.exp_month}/${c.exp_year}`};});
    } catch (err) {
      //TODO - better handling here
      Logger.error(err);
    }
  }

  static async sendToStripe(params, uri, key, method = 'post'){
    const request = {
      method,
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${key}`
      }
    };

    if (method == 'post'){
      let formBody = [];
      for (const property in params) {
        if (params.hasOwnProperty(property)) {
          const encodedKey = encodeURIComponent(property);
          const encodedValue = encodeURIComponent(params[property]);
          formBody.push(`${encodedKey}=${encodedValue}`);
        }
      }
      formBody = formBody.join('&');
      request.body = formBody;
    }

    const response = await fetch(uri, request);
    return JSON.parse(response._bodyText);
  }
}
