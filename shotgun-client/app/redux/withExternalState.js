
import React from 'react';
import NonReactStatics from 'hoist-non-react-statics';
import {connect} from './connect';
import {UPDATE_COMPONENT_STATE} from 'common/dao/ActionConstants';
import memoize from './memoize';
import Logger from 'common/Logger';


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

const getPath = (stateKey) => {
  return stateKey.split('.');
};

const mapComponentStateToProps = (stateKey, propsToMap = [], globalMapStateToProps) => (state, initialProps) => {
  const myStateGetter = (stateObj) => stateObj.getIn(['component', ...getPath(stateKey)]  || {});
  let myState = myStateGetter(state);
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
    myStateGetter,
    ...initialProps,
    ...myState
  };

  return globalMapStateToProps ? globalMapStateToProps(state, result, myState) : result;
};

const setState = (stateKey, partialState, continueWith) => {
  return setStateWithPath(stateKey, partialState, [], continueWith);
};

const setStateWithPath = (stateKey, partialState, path, continueWith) => {
  return {type: UPDATE_COMPONENT_STATE(stateKey), path: [...getPath(stateKey), ...path], data: partialState, continueWith};
};

const createSetState = (stateKey) => {
  return function(partialState, continueWith, dispatchArgument){
    const {dispatch = dispatchArgument} = (this.props || {});
    dispatch(setState(stateKey, partialState, continueWith));
  };
};

const createSetStateWithPath = (stateKey) => {
  return function(partialState, path, continueWith, dispatchArgument){
    const {dispatch = dispatchArgument} = (this.props || {});
    dispatch(setStateWithPath(stateKey, partialState, path, continueWith));
  };
};

/**
 * A public higher-order component to access the imperative API
 */

const wrapperFactory = (Component, mapGlobalStateToProps, superStateKeyOverride) => {
  Logger.info('WEXT - Creating ' + Component.name);
  let hasInitialized = false;
  const result = class ComponentWrapper extends React.Component{
    constructor(props){
      super(props);
      Logger.info('WEXT - Instantiating ' + Component.name);
      const {stateKey = (Component.stateKey || Component.name), propsToMap} = props;
      this.stateKey = superStateKeyOverride || stateKey;
      this.newProps =  _extends({}, createNewProps(Component, this.stateKey, {...props}));
      const isStatelessComponent = !!Component.prototype.render;
      const componentAndGlobalMapStateToProps = mapComponentStateToProps(this.stateKey, propsToMap, mapGlobalStateToProps);
      this.Component =  connect(componentAndGlobalMapStateToProps, true, isStatelessComponent)(Component);
      const displayKey = (Component.displayName || Component.name);
      this.displayName = 'withExternalState(' + displayKey + ')';
      this.WrappedComponent = Component;
      this.createSetState = createSetState(this.stateKey).bind(this);
      this.resetComponentState = this.resetComponentState.bind(this);
    }

    shouldComponentUpdate(){
      const {path, history = {}} = this.props;
      const {location} = history;
      return !location || !path || location.pathname.includes(path);
    }

    componentWillMount(){
      if (!hasInitialized){
        this.resetComponentState();
        hasInitialized = true;
      }
    }

    resetComponentState(continueWith){
      if (Component.InitialState){
        this.createSetState(Component.InitialState, continueWith);
      }
    }

    get wrappedInstance(){
      if (this.inner){
        return this.inner.wrappedInstance;
      }
    }

    render(){
      const {Component, newProps, props, resetComponentState} = this;
      const  propsForChild = {...{...newProps, ...props, resetComponentState}};
      return <Component key={newProps.stateKey} ref={cmp => {this.inner = cmp;}} style={{flex: 1}} {...propsForChild}/>;
    }
  };
  return NonReactStatics(result, Component);
};

const createNewProps = (Component, stateKey, originalProps) => {
  const {setState: _1, setStateWithPath: _2, ...rest} = originalProps;
  const newSetState = createSetState(stateKey);
  const setStateWithPath = createSetStateWithPath(stateKey);
  Component.prototype.setState =  newSetState;
  return {...rest, setState: newSetState, setStateWithPath, Component};
};

export const withExternalState = (mapGlobalStateToProps, superStateKeyOverride) => (Component) => {
  return wrapperFactory(Component, mapGlobalStateToProps, superStateKeyOverride);
};

export default withExternalState;
