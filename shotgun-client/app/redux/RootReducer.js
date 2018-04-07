import {combineReducers} from 'redux-seamless-immutable';
import {UPDATE_STATE, UPDATE_COMMAND_STATUS, UPDATE_OPTIONS, RESET_ALL_COMPONENT_STATE, UPDATE_COMPONENT_STATE} from 'common/dao/ActionConstants';

const dao = (state = {}, action) => {
  if (action.type && (action.type.startsWith(UPDATE_STATE('')) || action.type.startsWith(UPDATE_OPTIONS('')) || action.type.startsWith(UPDATE_COMMAND_STATUS('')))){
    return state.setIn(action.path || [], action.data);
  }
  return state;
};

const component = (state = {}, action) => {
  if (action.type && (action.type.startsWith(UPDATE_COMPONENT_STATE('')))){
    const path = action.path || [];
    let stateToModify = state.getIn(path);
    stateToModify = stateToModify ? stateToModify.merge(action.data) : action.data;
    const result = state.setIn(path, stateToModify);
    if (action.continueWith){
      setTimeout(() => action.continueWith(state));
    }
    return result;
  } else if (action.type && (action.type === RESET_ALL_COMPONENT_STATE)){
    return {};
  }
 
  return state;
};

export default rootReducer = combineReducers({dao, component});
