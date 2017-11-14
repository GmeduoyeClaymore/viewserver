import * as constants from '../../redux/ActionConstants';
import {INITIAL_STATE} from './CheckoutInitialState';

export default function CheckoutReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case constants.UPDATE_STATUS:
      return state.merge(
        {
          status: {
            ...action.status
          }
        }, {deep: true});
    case constants.UPDATE_CART:
     return state.merge(
        {
          cart: {
            ...action.cart
          }
        }, {deep: true});
    case constants.UPDATE_ORDER:
      return state.merge(
        {
          order: {
            ...action.order
          }
        }, {deep: true});
    case constants.UPDATE_DELIVERY:
      return state.merge(
        {
          delivery: {
            ...action.delivery
          }
        }, {deep: true});
    default:
      return state;
  }
}
