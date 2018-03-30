import React, {Component} from 'react';
import { View } from 'react-native-animatable';
import {Dimensions} from 'react-native';
import { withExternalState } from '../withExternalState';
import TransitionManager from './TransitionManager';
import Immutable from 'seamless-immutable';
import matchPath from './matchPath';
import * as RouteUtils from './routeUtils';
import Logger from 'common/Logger';


const MaxStackLength = 2;

const DefaultNavigationStack = [{
  pathname: '/',
  isAdd: true
}];

const {width: deviceWidth, height: deviceHeight} = Dimensions.get('window');

class ReduxRouterClass extends Component{
  constructor(props){
    super(props);
    this.transitionManager = new TransitionManager();
    this.performTransition = this.performTransition.bind(this);
    this.handleRef = this.handleRef.bind(this);
  }

  shouldComponentUpdate(newProps){
    const {children, navigationContainer} = newProps;
    if (children != this.props.children){
      return true;
    }

    if (navigationContainer != this.props.navigationContainer){
      return true;
    }

    if (!this.pendingRouteTransition && this.transitionManager.doAnyOfMyRoutesNeedToTransition(navigationContainer)){
      this.pendingRouteTransition = true;
      return true;
    }
    return false;
  }

  async componentDidUpdate(){
    this.performTransition();
  }

  async performTransition(){
    const {navigationContainer, defaultRoute, history, performTransitions} = this.props;
    if ( this.pendingRouteTransition ){
      return;
    }

    performTransitions(this);
  }

  createComponent(routeDelta, additionalProps = {}){
    const {children, navigationContainer, ...rest} = this.props;
    const childForRoute = children.find((c, idx)=> RouteUtils.getKeyFromElement(c, idx) == routeDelta.key);
    if (!childForRoute){
      throw new Error(`Unable to find child for route for ${JSON.stringify(routeDelta)}`);
    }
    const {match} = routeDelta;
    const completeProps = { route: routeDelta, match, ...rest, ...childForRoute.props, ...additionalProps};
    const { component: ComponentForRoute} = childForRoute.props;
    return <ComponentForRoute {...completeProps} />;
  }
  

  handleRef(route, ref){
    this.transitionManager.initialize(route, ref);
  }

  render() {
    const {width = deviceWidth, height  = deviceHeight, navigationContainer, defaultRoute, children} = this.props;
    const routesToRender = this.transitionManager.getOrderedRoutesFromChildren(children, navigationContainer, false, RouteUtils.parseRoute(defaultRoute));
    return <View style={{flex: 1, height, width}}>
      {routesToRender.map(
        (rt, idx) => {
          return <View
            useNativeDriver={true}
            style={{ position: 'absolute',
              height, width,
              backgroundColor: 'white',
              left: 0,
              zIndex: rt.index,
              top: 0, minHeight: height, minWidth: width, maxHeight: height, maxWidth: width}} key={rt.key} ref={ref =>  {this.handleRef(rt, ref);}}>
            {this.createComponent(rt, {style: {height, width}})}
          </View>;
        }
      )}
    </View>;
  }
}


const moveNavigation  = (isReplace, isReverse) => (navigationContainer, navAction) => {
  Logger.info(`Navigating to ${JSON.stringify(navAction)}`);
  const {navigationStack} = navigationContainer;
  const previousNavItems = navigationStack.map(RouteUtils.removeDeltas);
  const previousFinalNavItem = previousNavItems[previousNavItems.length - 1];
  const itemsNotNeedingTransition = [...previousNavItems.slice(0, previousNavItems.length - 1)];
  const addition = {isReverse, ...navAction, isAdd: true};
  let newNavigationStack;
  if (isReplace){
    newNavigationStack = [...itemsNotNeedingTransition, {...addition, index: itemsNotNeedingTransition.length + 1} ];
  } else {
    if (!navAction.pathname.includes(previousFinalNavItem.pathname)){
      const remove = {isReverse, ...previousFinalNavItem, isRemove: true};
      newNavigationStack = [...itemsNotNeedingTransition, remove, addition ];
    } else {
      newNavigationStack = [...itemsNotNeedingTransition, previousFinalNavItem, {addition, index: itemsNotNeedingTransition.length + 2}  ];
    }
  }
  const croppedStack = newNavigationStack.slice(-MaxStackLength);
  return navigationContainer.setIn(['navigationStack'], croppedStack);
};

const goBackTranslation = (navigationContainer) => {
  const {navigationStack} = navigationContainer;
  const previousNavItems = navigationStack.map(RouteUtils.removeDeltas);
  const previousFinalNavItem = previousNavItems[previousNavItems.length - 2] || previousNavItems[previousNavItems.length - 1];
  return moveNavigation(true, true)(navigationContainer, previousFinalNavItem);
};

const resetTranslation = (navigationContainer) => {
  const {navigationStack} = navigationContainer;
  const newNavigationStack = navigationStack.map(RouteUtils.removeDeltas);
  return navigationContainer.setIn(['navigationStack'], newNavigationStack);
};

const navigateFactory = (myStateGetter, navigationContainerTranslation, setState, DefaultNavigation, continueWith) => (action, state) =>  async (dispatch, getState) => {
  const componentState = myStateGetter(getState()) || {};
  const {navigationContainer = DefaultNavigation} = componentState;
  const newnavigationContainer =  navigationContainerTranslation(navigationContainer, RouteUtils.parseAction(action, state));
  setState({navigationContainer: newnavigationContainer}, continueWith, dispatch);
};

const createDefaultNavigation = (props) => {
  const navigationStack = props.defaultNavigation || DefaultNavigationStack;
  return Immutable({
    navigationStack,
    navigationPointer: 1
  });
};


const performTransitionsThunkFactory = (context, myStateGetter, setState, clearTransitions) => async (dispatch, getState) => {
  const componentState = myStateGetter(getState()) || {};
  const {navigationContainer} = componentState;
  if (!navigationContainer || ! context.transitionManager.doAnyOfMyRoutesNeedToTransition(navigationContainer)){
    return;
  }
  setState({isTransitioning: true}, undefined, dispatch);

  const hasTransitioned = await context.transitionManager.performTransition(navigationContainer);
  if (hasTransitioned && !context.pendingClear){
    context.pendingClear = true;
    clearTransitions(() => {
      context.pendingClear = false;
      context.pendingRouteTransition = false;
      context.transitionManager.completeTransition();
      setState({isTransitioning: false}, undefined, dispatch);
    });
  }
  dispatch({ type: 'TRANSITIONS_PERFORMED'});
};

const mapStateToProps = (state, props) => {
  const DEFAULT_NAVIGATION = createDefaultNavigation(props);
  const {myStateGetter, dispatch, navigationContainer = DEFAULT_NAVIGATION, setState} = props;
  const replaceThunk = navigateFactory(myStateGetter, moveNavigation(true), setState, DEFAULT_NAVIGATION);
  const goBackThunk = navigateFactory(myStateGetter, goBackTranslation, setState, DEFAULT_NAVIGATION);
  const nextThunk = navigateFactory(myStateGetter, moveNavigation(false), setState, DEFAULT_NAVIGATION);
  const clearTransitionsThunk = (continueWith) => navigateFactory(myStateGetter, resetTranslation, setState, undefined, continueWith);
  const clearTransitions =  (continueWith) => dispatch(clearTransitionsThunk(continueWith)());
  return {
    ...props,
    navigationContainer,
    performTransitions: context => dispatch(performTransitionsThunkFactory(context, myStateGetter, setState,  clearTransitions)),
    history: {
      location: navigationContainer.navigationStack[navigationContainer.navigationStack.length - 1],
      replace: (path, navState) => dispatch(replaceThunk(path, navState)),
      goBack: action => dispatch(goBackThunk(action)),
      push: (path, navState) => dispatch(nextThunk(path, navState))
    }
  };
};

export const ReduxRouter = withExternalState(mapStateToProps)(ReduxRouterClass);

export default ReduxRouter;
