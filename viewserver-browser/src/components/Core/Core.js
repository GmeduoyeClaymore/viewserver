import React, { Component } from 'react';
import { Provider } from 'react-redux';
import { Route, Switch, Redirect } from 'react-router-dom';
import { ConnectedRouter } from 'connected-react-router';


import { components, history, store, store2 } from '../components.js';
import styles from './component.less';

const Core = () => {
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <div className="window">
          <div className="window-content">
            <div className="pane-group">
              <div className="pane-sm flex-col sidebar"><components.Menu graphStore={store2}/></div>
              <div className="pane flex-col padded"><AppRouter graphStore={store2}/></div>
            </div>
          </div>
          <components.Footer />
        </div>
      </ConnectedRouter>
    </Provider>
  );
}

 
const AppRouter = (props) => { 
  return (
    <Switch>
      <Route path="/login" exact component={components.Login} /> 
      <Route path="/operatorGroupView" render={() => <components.OperatorGroupView {...props}/>} /> 
    </Switch>
  );
} 



export default Core;
