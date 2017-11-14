import * as constants from '../../redux/ActionConstants';
import {INITIAL_STATE} from './ProductInitialState';

export default function productReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case constants.UPDATE_PRODUCT:
     return state.merge(
        {
          product: {
            ...action.product
          }
        }, {deep: true});
    default:
      return state;
  }
}
