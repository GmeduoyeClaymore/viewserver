import config from 'electron-json-config';


// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper.js';
const reduxUtil = reduxHelper('ControllerView');

// Include component
import component from './ControllerView.js';
// Initial State
const initialState = {
};




// Make Actions
const actions = {
};
// Make reducer
const reducer = reduxUtil.createReducer({
  INVOKE_COMMAND_CONTROLLERSDAO_INVOKEJSONCOMMAND: function(state, action) {
    const {daoName, method, payload} = action;
    if("controllersDao" === daoName && method === "invokeJSONCommand"){
      const { controller, command, payload: commandPayload} = payload;
      return state.setIn([controller,command], commandPayload)
    }
    return state;
  }
}, initialState);

// Export
export {
  component,
  actions,
  reducer
};
