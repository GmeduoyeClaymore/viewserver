import {createStore, applyMiddleware, compose} from 'redux';
import rootReducer from './RootReducer';
import thunk from 'redux-thunk';

const middleware = [
  thunk
];

export default function configureStore(initialState) {
  const store = createStore(rootReducer, initialState, compose(applyMiddleware.apply(null, middleware)));

  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('./RootReducer', () => {
      const nextReducer = rootReducer;
      store.replaceReducer(nextReducer);
    });
  }
  return store;
}
