import Immutable from 'seamless-immutable';
import {OrderStatuses} from '../../common/constants/OrderStatuses';

export const INITIAL_STATE = Immutable({
  cart: {
    totalQuantity: undefined,
    totalPrice: 0,
    items: []
  },
  order: {
    orderId: undefined,
    paymentId: undefined,
    deliveryId: undefined,
    status: OrderStatuses.PLACED
  },
  delivery: {
    deliveryId: undefined,
    type: 'ROADSIDE',
    eta: 72,
    deliveryAddressId: undefined
  },
  status: {
    error: '',
    busy: false
  }
});
