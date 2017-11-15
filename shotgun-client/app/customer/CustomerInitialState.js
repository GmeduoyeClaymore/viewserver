import Immutable from 'seamless-immutable';

export const INITIAL_STATE = Immutable({
  customer: {
    customerId: undefined,
    title: undefined,
    firstName: undefined,
    lastName: undefined,
    emailAddress: undefined,
    contactNo: undefined,
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
