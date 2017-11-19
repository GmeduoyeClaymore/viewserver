import {combineReducers} from 'redux-seamless-immutable';
import {UPDATE_STATE, UPDATE_COMMAND_STATUS} from 'common/dao/ActionConstants';

const dao = (state = {}, action) => {
  if (action.type && (action.type.startsWith(UPDATE_STATE('')) || action.type.startsWith(UPDATE_COMMAND_STATUS('')))){
    return state.setIn(action.path || [], action.data);
  }
  return state;
};

export default rootReducer = combineReducers({dao});
