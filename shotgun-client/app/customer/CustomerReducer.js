import * as constants from '../redux/ActionConstants';
import {INITIAL_STATE} from './CustomerInitialState';

export default function customerReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case constants.UPDATE_CUSTOMER:
     return state.merge(
        {
          customer: {
            ...action.customer
          }
        }, {deep: true});
    default:
      return state;
  }
}
