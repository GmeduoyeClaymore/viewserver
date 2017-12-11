import electron from 'electron';

import { createHashHistory } from 'history';
import { applyMiddleware, createStore, compose } from 'redux';
import { connectRouter, routerMiddleware } from 'connected-react-router';
import thunk from 'redux-thunk';
import DaoMiddleware from 'custom-redux/DaoMiddleware';
const Immutable = require("seamless-immutable");
import {combineReducers} from 'redux-seamless-immutable';
import {UPDATE_STATE, UPDATE_COMMAND_STATUS, UPDATE_OPTIONS, UPDATE_TOTAL_SIZE} from 'common/dao/ActionConstants';
import {setLocale} from 'yup/lib/customLocale';
import * as crx from 'common/rx';
this.crx = crx;//force this to load

setLocale({
  mixed: {
    required: 'is required',
    max: 'is too long',
    min: 'is too short',
    matches: 'fails to match the pattern'
  },
  string: {
    email: 'is not a valid email',
    url: 'is not a valid url'
  }
});

const dao = (state = {}, action) => {
  if (action.type && (action.type.startsWith(UPDATE_STATE('')) || action.type.startsWith(UPDATE_OPTIONS('')) || action.type.startsWith(UPDATE_TOTAL_SIZE(''))|| action.type.startsWith(UPDATE_COMMAND_STATUS('')))){
    return state.setIn(action.path || [], action.data);
  }
  return state;
};

// This grabs all 'component.js' files in subdirectories under /components/
const allComponents = require.context('./', true, /component\.js$/);

// Grab the redux reducer function from the components's 'component' file, as well as the component itself
let reducers = {};
let components = {};
let actions = {};
allComponents.keys().forEach( ( path ) => {
  let name = path.split('/')[1];
  let thisComponent = allComponents( path );
  if ( !thisComponent.component ) {
    console.warn(`Component "${name}" is in an invalid format, ignoring. Found at: "${path}"`);
  }
  components[ name ] = thisComponent.component;
  if(thisComponent.actions){
    actions[ name ] = thisComponent.actions;
  }
  if ( thisComponent.reducer ) {
    reducers[ name ] = thisComponent.reducer;
  }
});

// Start history
const history = createHashHistory({
  // Here we override prompt to use the native electron dialog module, this lets us override the message box title
  getUserConfirmation: (message, callback) => {
    electron.remote.dialog.showMessageBox(
      {
        title: 'Confirm Navigation',
        type: 'question',
        buttons: ['Yes', 'No'],
        message
      },
      (clickedIdx) => {
        if ( clickedIdx === 0 ) {
          callback( true );
        } else {
          callback( false );
        }
      }
    )
  }

  //callback(window.confirm(message))
});


// Compile reducers
reducers = combineReducers( {...reducers,dao} );

// Start history
// Merge middlewares
let middlewares = [
  thunk,
  routerMiddleware(history),
  DaoMiddleware
];

// Development adds logging, must be last
if ( process.env.NODE_ENV !== "production") {
  middlewares.push( require('redux-logger')({
    // Change this configuration to your liking
    duration: true, collapsed: true
  }));
}

const makeSeamless = (reducer) => (state = {}, action) => {
  const newState = reducer(Immutable.isImmutable(state) ? state : Immutable(state),action);
  return Immutable.isImmutable(newState) ? newState : Immutable(newState)
};

const finalReducer = makeSeamless(connectRouter(history)(reducers));
const store = undefined === window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ ?
createStore(finalReducer, compose(applyMiddleware(...middlewares))) :
createStore(finalReducer, window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__(applyMiddleware(...middlewares)));

// Export all the separate modules
export {
  components,
  history,
  store
};
