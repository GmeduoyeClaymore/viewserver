
import React from 'react';
import NonReactStatics from 'hoist-non-react-statics';
import {connect} from './connect';
import {UPDATE_COMPONENT_STATE} from 'common/dao/ActionConstants';
import Logger from 'common/Logger';
import removeProperties from './removeProperties';
import invariant  from 'invariant';


Function.prototype.wrap = function wrap(otherFunction) {
  const f = this;
  return function () {
    otherFunction(() => f.apply(this, arguments));
  };
};

const getPath = (stateKey) => {
  return stateKey.split('.');
};

const mapComponentStateToProps = (stateKey, propsFromStateToPassIntoComponent = [], globalMapStateToProps) => (state, initialProps) => {
  const myStateGetter = (stateObj) => stateObj.getIn(['component', ...getPath(stateKey)]  || {});
  let myState = myStateGetter(state);
  if (propsFromStateToPassIntoComponent.length){
    const resultingState = {};
    propsFromStateToPassIntoComponent.forEach(
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
    setState: createSetState(stateKey),
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
    invariant((this.props || dispatchArgument), "You're probably trying to use withExternalState on a stateless component. This means we cannot get dispatch from the props so either pass it in as the third argument to set state or make the component stateful");
    const {dispatch = dispatchArgument} = (this.props || {});
    dispatch(setState(stateKey, partialState, continueWith));
  };
};

const createSetStateWithPath = (stateKey) => {
  return function(partialState, path, continueWith, dispatchArgument){
    invariant((this.props || dispatchArgument), "You're probably trying to use withExternalState on a stateless component. This means we cannot get dispatch from the props so either pass it in as the third argument to set state or make the component stateful");
    const {dispatch = dispatchArgument} = (this.props || {});
    dispatch(setStateWithPath(stateKey, partialState, path, continueWith));
  };
};

/**
 * A public higher-order component to access the imperative API
 */


const removeUnwantedProperties = (props) => (
  removeProperties(props, ['stateKey', 'setState', 'setStateWithPath', 'propsFromStateToPassIntoComponent'])
);

const createEither = ({
  props,
  mapGlobalStateToProps,
  mapGlobalStateToPropsFactory
}) => {
  if (mapGlobalStateToProps){
    return mapGlobalStateToProps;
  }
  if (mapGlobalStateToPropsFactory){
    return mapGlobalStateToPropsFactory(props);
  }
};

const wrapperFactory = (Component, {
  superStateKeyOverride,
  mapGlobalStateToProps,
  mapGlobalStateToPropsFactory
}) => {
  let hasInitialized = false;

  const changeSetStateImplementationAndReturnNewProps = props => {
    let {stateKey = (Component.stateKey || Component.name)} = props;
    stateKey = superStateKeyOverride || stateKey;
    const newSetState = createSetState(stateKey);
    const setStateWithPath = createSetStateWithPath(stateKey);
    Component.prototype.setState =  newSetState;
    return {setState: newSetState, setStateWithPath, Component, stateKey};
  };

  const result = class ComponentWrapper extends React.Component{
    constructor(props){
      super(props);
      this.stateSpecificProps = changeSetStateImplementationAndReturnNewProps(this.props);
      const {propsFromStateToPassIntoComponent} = this.props;
      const {stateKey} = this.stateSpecificProps;

      const isStatelessComponent = !!Component.prototype.render;

      const componentAndGlobalMapStateToProps = mapComponentStateToProps(stateKey, propsFromStateToPassIntoComponent, createEither({
        mapGlobalStateToProps, mapGlobalStateToPropsFactory, props
      }));
      this.Component =  connect(componentAndGlobalMapStateToProps, true, isStatelessComponent)(Component);


      const displayKey = (Component.displayName || Component.name);
      this.displayName = `withExternalState(${displayKey}_${stateKey}')`;
      this.createSetState = createSetState(stateKey).bind(this);
      this.resetComponentState = this.resetComponentState.bind(this);
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
      const {Component, props, resetComponentState, stateSpecificProps} = this;
      const  propsForChild = {...removeUnwantedProperties(props), ...stateSpecificProps, resetComponentState};
      return <Component key={stateSpecificProps.stateKey} ref={cmp => {this.inner = cmp;}} style={{flex: 1}} {...propsForChild}/>;
    }
  };
  return NonReactStatics(result, Component);
};


export const withExternalState = (mapGlobalStateToProps, superStateKeyOverride) => (Component) => {
  return wrapperFactory(Component, {
    superStateKeyOverride,
    mapGlobalStateToProps});
};

export const withExternalStateFactory = (mapGlobalStateToPropsFactory, superStateKeyOverride) => (Component) => {
  return wrapperFactory(Component, {
    superStateKeyOverride,
    mapGlobalStateToPropsFactory});
};

export default withExternalState;
