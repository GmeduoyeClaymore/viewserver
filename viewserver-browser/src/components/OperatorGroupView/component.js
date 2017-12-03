import config from 'electron-json-config';


// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper.js';
const reduxUtil = reduxHelper('OperatorGroupView');

// Include component
import component from './OperatorGroupView.js';
const SELECT_GROUP = reduxUtil.defineAction('SELECT_GROUP');
const TOGGLE_MODE = reduxUtil.defineAction('TOGGLE_MODE');
// Initial State
const initialState = {
  mode : 'table'
};

// Make Actions
const actions = {
  selectGroup: reduxUtil.createAction(SELECT_GROUP),
  toggleMode: reduxUtil.createAction(TOGGLE_MODE)
};
// Make reducer
const reducer = reduxUtil.createReducer({
  [SELECT_GROUP]: function(state, action) {
    return state.setIn(['context'], action.payload);
  },
  [TOGGLE_MODE]: function(state) {
    return state.setIn(['mode'], state.mode === 'table' ? 'graph' : 'table');
  }
}, initialState);

// Export
export {
  component,
  actions,
  reducer
};
