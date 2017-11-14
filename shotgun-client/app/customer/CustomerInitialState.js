import Immutable from 'seamless-immutable';

export const INITIAL_STATE = Immutable({
  customer: {
    customerId: undefined,
    paymentCards: [],
    deliveryAddresses: [],
    orders: {
      complete: [],
      incomplete: []
    },
    orderDetail: {
      items: []
    },
    status: {
      error: '',
      busy: false
    }
  }
});
