import config from 'electron-json-config';

// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper';
import {parseReportFromJson} from './reportConfigUtils';
import component from './OperatorConfigurationView';
import determinePosition from 'common/utils/graphPositioningUtils';

const reduxUtil = reduxHelper('OperatorConfigurationView');

// Include component
const SHOW_REPORT_GRAPH = reduxUtil.defineAction('SHOW_REPORT_GRAPH');
const SELECT_REPORT_NODE = reduxUtil.defineAction('SELECT_REPORT_NODE');
// Initial State
const initialState = {
  graphConfig: {
    links: [],
    nodes: []
  }
};

// Make Actions
const actions = {
  showReportGraph: reduxUtil.createAction(SHOW_REPORT_GRAPH),
  selectReportNode: reduxUtil.createAction(SELECT_REPORT_NODE)
};
// Make reducer
const reducer = reduxUtil.createReducer({
  [SHOW_REPORT_GRAPH]: function(state, action) {
    const {links, nodes, parameters} = parseReportFromJson(JSON.parse(action.payload.json));
    
    const resultState = state.setIn(['graphConfig'], {links, nodes, parameters, report: action.payload});
    return resultState.setIn(['selectedNode'], undefined);
  },
  [SELECT_REPORT_NODE] : function(state, action) {
    return state.setIn(['selectedNode'], action.payload);
  }
}, initialState);

// Export
export {
  component,
  actions,
  reducer
};


