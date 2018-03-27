
import React from 'react';
import PropTypes from 'prop-types';
import NonReactStatics from 'hoist-non-react-statics';
import {connect} from './connect';
import {UPDATE_COMPONENT_STATE} from 'common/dao/ActionConstants';
import memoize from './memoize';
import isEqual from './is-equal';

Function.prototype.wrap = function wrap(otherFunction) {
  const f = this;
  return function () {
    otherFunction(() => f.apply(this, arguments));
  };
};

const customAssign = (target) =>  {
  for (let i = 1; i < arguments.length; i++) {
    const source = arguments[i];
    for (const key in source) {
      if (Object.prototype.hasOwnProperty.call(source, key)) {
        target[key] = source[key];
      }
    }
  } return target;
};

const _extends = Object.assign || customAssign;


const mapComponentStateToProps = (stateKey, propsToMap = [], globalMapStateToProps) => (state, initialProps) => {
  let myState = state.getIn(['component', stateKey]  || {});
  if (propsToMap.length){
    const resultingState = {};
    propsToMap.forEach(
      pr => {
        if (myState[pr] != undefined){
          resultingState[pr] = myState[pr];
        }
      }
    );
    myState = resultingState;
  }

  const result =  {
    ...initialProps,
    ...myState
  };

  return globalMapStateToProps ? globalMapStateToProps(state, result) : result;
};

const setState = (stateKey, partialState, continueWith) => {
  return {type: UPDATE_COMPONENT_STATE(stateKey), path: [stateKey], data: partialState, continueWith};
};

const createSetState = (stateKey) => {
  return function(partialState, continueWith, dispatchArgument){
    const {dispatch = dispatchArgument} = (this.props || {});
    dispatch(setState(stateKey, partialState, continueWith));
  };
};

/**
 * A public higher-order component to access the imperative API
 */

const wrapperFactory = (Component, mapGlobalStateToProps) => class ComponentWrapper extends React.Component{
  constructor(props){
    super(props);
    const {stateKey = (Component.stateKey || Component.name), propsToMap} = props;
    this.stateKey = stateKey;
    this.newProps =  _extends({}, createNewProps(Component, {...props, stateKey}));
    const isStatelessComponent = !!Component.prototype.render;
    const componentAndGlobalMapStateToProps = mapComponentStateToProps(stateKey, propsToMap, mapGlobalStateToProps);
    this.Component =  connect(componentAndGlobalMapStateToProps, true, isStatelessComponent)(Component);
    const displayKey = (Component.displayName || Component.name);
    this.displayName = 'withExternalState(' + displayKey + ')';
    this.WrappedComponent = Component;
    this.createSetState = createSetState(this.stateKey).bind(this);
  }

  componentWillMount(){
    if (Component.InitialState){
      this.createSetState(Component.InitialState);
    }
  }

  get wrappedInstance(){
    if (this.inner){
      return this.inner.wrappedInstance;
    }
  }

  render(){
    const {Component, newProps, props} = this;
    return <Component ref={cmp => {this.inner = cmp;}} style={{flex: 1}} {...{...newProps, ...props}}/>;
  }
};

const createNewProps = memoize((Component, originalProps) => {
  const {stateKey} = originalProps;
  const newSetState = createSetState(stateKey);
  Component.prototype.setState =  newSetState;
  return {...originalProps, setState: newSetState, Component};
});

export const withExternalState = (mapGlobalStateToProps) => (Component) => {
  return wrapperFactory(Component, mapGlobalStateToProps);
};

export default withExternalState;
