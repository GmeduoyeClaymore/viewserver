import config from 'electron-json-config';


// Init reduxHelper
import reduxHelper from '../../utils/reduxHelper.js';
const reduxUtil = reduxHelper('FullOperatorView');

// Include component
import component from './FullOperatorView.js';
// Initial State
const initialState = {
};




// Make Actions
const actions = {
};
// Make reducer
const reducer = reduxUtil.createReducer({
}, initialState);

// Export
export {
  component,
  actions,
  reducer
};
