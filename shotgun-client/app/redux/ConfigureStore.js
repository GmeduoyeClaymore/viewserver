import {createStore, applyMiddleware, compose} from 'redux';
import rootReducer from './RootReducer';
import thunk from 'redux-thunk';

export default function configureStore(initialState) {
  //if we're debugging then use the redux devtools extension
  const store = undefined === window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ ?
    createStore(rootReducer, initialState, compose(applyMiddleware(thunk))) :
    createStore(rootReducer, initialState, window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__(applyMiddleware(thunk)));


  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('./RootReducer', () => {
      const nextReducer = rootReducer;
      store.replaceReducer(nextReducer);
    });
  }
  return store;
}
