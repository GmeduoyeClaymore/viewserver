import config from 'electron-json-config';


// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper.js';
const reduxUtil = reduxHelper('OperatorGroupView');

// Include component
import component from './OperatorGroupView.js';
const SELECT_GROUP = reduxUtil.defineAction('SELECT_GROUP');
const TOGGLE_MODE = reduxUtil.defineAction('TOGGLE_MODE');
const SHOW_OPERATOR_GROUP_GRAPH = reduxUtil.defineAction('SHOW_OPERATOR_GROUP_GRAPH');
const SELECT_OPERATOR_NODE = reduxUtil.defineAction('SELECT_OPERATOR_NODE');
// Initial State
const initialState = {
  mode : 'graph',
  graphConfig: {
    links: [],
    nodes: []
  }
};




// Make Actions
const actions = {
  selectGroup: reduxUtil.createAction(SELECT_GROUP),
  toggleMode: reduxUtil.createAction(TOGGLE_MODE),
  selectOperatorNode: reduxUtil.createAction(SELECT_OPERATOR_NODE),
  showOperatorGroupGraph: reduxUtil.createAction(SHOW_OPERATOR_GROUP_GRAPH),
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
