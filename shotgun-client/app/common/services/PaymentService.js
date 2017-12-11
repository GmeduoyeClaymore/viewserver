import Logger from 'common/Logger';

//TODO - this should come from the manifest
const STRIPE_KEY = 'pk_test_BUWd5f8iUuxmbTT5MqsdOlmk';
const STRIPE_URL = 'https://api.stripe.com/v1';

export default class PaymentService {
  static async createCardToken(cardNumber, expMonth, expYear, cvc){
    try {
      const params = {
        'card[number]': cardNumber,
        'card[exp_month]': expMonth,
        'card[exp_year]': expYear,
        'card[cvc]': cvc
      }
      const response = await PaymentService.sendToStripe(params, 'tokens');
      return response.id;
    } catch (err) {
      Logger.warning(err);
    }
  }

  static async sendToStripe(params, method){
    let formBody = [];
    for (const property in params) {
      const encodedKey = encodeURIComponent(property);
      const encodedValue = encodeURIComponent(params[property]);
      formBody.push(`${encodedKey}=${encodedValue}`);
    }
    formBody = formBody.join('&');

    const response = await fetch(`${STRIPE_URL}/${method}`, {
      method: 'post',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${STRIPE_KEY}`
      },
      body: formBody
    });

    return JSON.parse(response._bodyText);
  }
}
