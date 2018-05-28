import config from 'electron-json-config';


// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper.js';
const reduxUtil = reduxHelper('Login');

// Include component
import component from './Login.js';

// Action Definitions
const SAVE_SETTINGS = reduxUtil.defineAction('SAVE_SETTINGS');

// Initial State
const initialState = {
  // get this from config file (second parameter is the default value if not found)
  username: config.get('username', 'bem'),
  password: config.get('password', 'foo'),
  url: config.get('url', 'shotgun.ltd:6060')
};

// Make Actions
const actions = {
  saveSettings: reduxUtil.createAction(SAVE_SETTINGS)
};

// Make reducer
const reducer = reduxUtil.createReducer({
  [SAVE_SETTINGS]: function(state, action) {
    let newState = { ...state, ...action.payload };
    return newState;
  }
}, initialState);

// Export
export {
  component,
  actions,
  reducer
};
