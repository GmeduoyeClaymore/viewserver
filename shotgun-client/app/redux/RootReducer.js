import {combineReducers} from 'redux-seamless-immutable';
import {UPDATE_STATE} from 'common/dao/ActionConstants';

const dao = (state = {}, action) => {
  if (action.type == UPDATE_STATE){
    return state.setIn(action.path || [], action.data);
  }
  return state;
};

export default rootReducer = combineReducers({dao});
