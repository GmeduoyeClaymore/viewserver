import React, {Component} from 'react';
import { View } from 'react-native-animatable';
import {Dimensions} from 'react-native';
import { withExternalState } from '../withExternalState';
import TransitionManager from './TransitionManager';
import Immutable from 'seamless-immutable';
import matchPath from './matchPath';
import * as RouteUtils from './routeUtils';
import Logger from 'common/Logger';
import {ErrorRegion} from 'common/components';
import { memoize } from '../memoize';
import { isEqual } from '../is-equal';

const MaxStackLength = 20;

const DefaultNavigationStack = [{
  pathname: '/',
  isAdd: true
}];

const {width: deviceWidth, height: deviceHeight} = Dimensions.get('window');


class ReduxRouterClass extends Component{
  constructor(props){
    super(props);
    this.transitionManager = new TransitionManager(props.name);
    this.performTransition = this.performTransition.bind(this);
    this.handleRef = this.handleRef.bind(this);
    this.processChildren = this.processChildren.bind(this);
    this.lifeCycles = {};
    this.state = {
      initializationErrors: []
    };
    this.transitionManager.setDefaultRoute(this.props.defaultRoute);
  }

  componentDidMount(){
    if (!this.childrenProcessed){
      this.transitionManager.log('Processing children');
      this.processChildren(this.props.children);
    }
    this.transitionManager.log('Performing transition');
    this.performTransition();
    this.transitionManager.log('Finished Performing transition');
  }

  componentWillUnmount(){
    Logger.info("Throwing redux router away as for some reason we don't think we need it anymore ");
  }


  componentWillReceiveProps(newProps){
    this.transitionManager.setDefaultRoute(newProps.defaultRoute);
    if (this.props.children != newProps.children){
      this.processChildren(newProps.children);
    }
  }

  processChildren(children){
    const newLifeCycle = RouteUtils.getLifeCycleForChildren(children);
    Object.keys(newLifeCycle).forEach(
      childPath => {
        if (!this.lifeCycles[childPath]){
          this.doInit(newLifeCycle[childPath]);
          this.lifeCycles[childPath] = newLifeCycle[childPath];
        }
      }
    );
    const lifeCyclesToForget = [];
    Object.keys(this.lifeCycles).forEach(
      childPath => {
        if (!newLifeCycle[childPath]){
          lifeCyclesToForget.push(childPath);
        }
      }
    );
    lifeCyclesToForget.forEach(
      childPath => {
        this.transitionManager.destroy(childPath);
        delete this.lifeCycles[childPath];
      }
    );
    this.childrenProcessed = true;
  }

  async doInit(lifeCycle){
    if (!lifeCycle.oneOffInitialization){
      return;
    }
    try {
      await lifeCycle.oneOffInitialization(this.props);
    } catch (error){
      let initializationErrors = this.state.initializationErrors;
      initializationErrors = [...initializationErrors, error];
      super.setState({initializationErrors});
    }
  }


  async componentDidUpdate(oldProps){
    this.transitionManager.log('Component did update');
    const {children = [], navigationContainer} = oldProps;
    let mayNeedToTransition = false;
    if (children != this.props.children){
      this.transitionManager.log('Children have changed');
      mayNeedToTransition =  true;
    }
    if (navigationContainer != this.props.navigationContainer){
      this.transitionManager.log('Navigation has changed');
      mayNeedToTransition =  true;
    }
    if (mayNeedToTransition){
      if (!this.childrenProcessed){
        this.transitionManager.log('Processing children');
        this.processChildren(this.props.children);
      }
      this.transitionManager.log('Performing transition');
      this.performTransition();
    } else {
      this.transitionManager.log('Not Performing transition');
    }
  }

  async performTransition(){
    this.props.performTransitions(this);
  }

  handleRef(route, ref){
    this.transitionManager.initialize(route, ref);
  }

  handleActualComponentRef(route, actualComponentRef){
    this.transitionManager.initializeActualComponent(route, actualComponentRef);
  }

  render() {
    const {width = deviceWidth, height  = deviceHeight, navigationContainer, history, path: parentPath} = this.props;
    const {initializationErrors} = this.state;
    const {stateKey: _, children, defaultRoute, setStateWithPath, setState, name, ...reduxRouterPropertiesToPassToEachRoute} = this.props;
    const routesToRender = this.transitionManager.getOrderedRoutesFromChildren(children, navigationContainer, false);
    return <View style={{flex: 1, height, width}}>
      <ErrorRegion errors={initializationErrors.join('\n')}/>
      {routesToRender.map(
        (rt) => {
          const childForRoute = children.find((c)=> RouteUtils.getKeyFromElement(c) == rt.key);
          if (!childForRoute){
            Logger.warning(`Unable to find child for route for ${JSON.stringify(rt)}`);
            return null;
          }
          const { component: ComponentForRoute, ...rest} = childForRoute.props;
          const match = matchPath(history.location.pathname, childForRoute.props.path);
          const completeProps = { ...reduxRouterPropertiesToPassToEachRoute, parentPath, route: rt, ...rest, height, width, style: {height, width}, match: {...match, path: childForRoute.props.path}, isInBackground: !match || !match.isExact && childForRoute.props.path != defaultRootObj.path };
          return <View
            useNativeDriver={true}
            style={{ position: 'absolute',
              height, width,
              backgroundColor: 'white',
              left: 0,
              top: 0, minHeight: height, minWidth: width, maxHeight: height, maxWidth: width}} key={rt.key} ref={ref => {this.handleRef(rt, ref);}}>
            <ComponentForRoute ref={ref => {this.handleActualComponentRef(rt, ref);}} key={rt.path} {...completeProps}/>
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
  if (isEqual(RouteUtils.removeDeltas(previousFinalNavItem), RouteUtils.removeDeltas(navAction), true)){
    Logger.info(`Looks like you attempted to navigate to a route you are already on ignoring ${JSON.stringify(navAction)}`);
    return;
  }
  const itemsNotNeedingTransition = [...previousNavItems.slice(0, previousNavItems.length - 1)];
  const addition = {isReverse, ...navAction, isAdd: true};
  let newNavigationStack;
  if (isReplace){
    newNavigationStack = [...itemsNotNeedingTransition, {...addition} ];
  } else {
    if (!navAction.pathname.includes(previousFinalNavItem.pathname)){
      const remove = {isReverse, ...previousFinalNavItem, isRemove: true};
      newNavigationStack = [...itemsNotNeedingTransition, remove, addition ];
    } else {
      newNavigationStack = [...itemsNotNeedingTransition, previousFinalNavItem, {...addition}  ];
    }
  }
  const croppedStack = newNavigationStack.slice(-MaxStackLength);
  return navigationContainer.setIn(['navigationStack'], croppedStack);
};

const goBackTranslation = (navigationContainer) => {
  const {navigationStack} = navigationContainer;
  if (navigationStack.length == 1){
    return;
  }
  const previousNavItems = navigationStack.map(RouteUtils.removeDeltas);
  const oldHeadOfStack = previousNavItems[previousNavItems.length - 1] || {};
  //Remove the current end of stack
  const remainingItems = [...previousNavItems.slice(0, previousNavItems.length - 1)];
  //Get new head of stack
  const newHeadOfStack = remainingItems[remainingItems.length - 1];
  const itemsNotNeedingTransition =  [...remainingItems.slice(0, remainingItems.length - 1)];
  //Set transition params on new head of stack
  const addition = {isReverse: true, ...newHeadOfStack, isAdd: true, transition: oldHeadOfStack.transition};
  const newNavigationStack = [...itemsNotNeedingTransition, {...addition}  ];
  const croppedStack = newNavigationStack.slice(-MaxStackLength);
  return navigationContainer.setIn(['navigationStack'], croppedStack);
};

const justTranslation = (navigationContainer, navAction) => {
  const addition = {isAdd: true, ...navAction};
  const newNavigationStack = [addition];
  return navigationContainer.setIn(['navigationStack'], newNavigationStack);
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
  if (newnavigationContainer){
    setState({navigationContainer: newnavigationContainer}, continueWith, dispatch);
  }
};


const createDefaultNavigation = (props) => {
  const navigationStack = props.defaultNavigation || DefaultNavigationStack;
  return Immutable({
    navigationStack
  });
};

const performTransitionsThunkFactory = (context, myStateGetter, setState, clearTransitions) => async (dispatch, getState) => {
  const componentState = myStateGetter(getState()) || {};
  const {navigationContainer} = componentState;

  const routes = context.transitionManager.getRoutesNeedingTransition(navigationContainer);
  const nonPersistentRoutes = routes.filter(rt => !rt.persistent);
  if (!navigationContainer || !nonPersistentRoutes || !nonPersistentRoutes.length ){
    return;
  }

  const hasTransitioned = await context.transitionManager.performTransition(routes, navigationContainer, context);
  if (hasTransitioned){
    clearTransitions(() => {
      Logger.info('Now clearing transitions');
      context.transitionManager.completeTransition();
    });
    dispatch({ type: 'TRANSITIONS_PERFORMED'});
  }
};

const historyFactory = memoize(
  ({myStateGetter, dispatch, navigationContainer, setState, DefaultNavigation}) => {
    const replaceThunk = navigateFactory(myStateGetter, moveNavigation(true), setState, DefaultNavigation);
    const goBackThunk = navigateFactory(myStateGetter, goBackTranslation, setState, DefaultNavigation);
    const justThunk = navigateFactory(myStateGetter, justTranslation, setState, DefaultNavigation);
    const nextThunk = navigateFactory(myStateGetter, moveNavigation(false), setState, DefaultNavigation);
    const location = navigationContainer.navigationStack[navigationContainer.navigationStack.length - 1];
    return {
      location,
      replace: (path, navState) => dispatch(replaceThunk(path, navState)),
      goBack: action => dispatch(goBackThunk(action)),
      just: action => dispatch(justThunk(action)),
      push: (path, navState) => dispatch(nextThunk(path, navState))
    };
  }, (arg1, arg2) => isEqual(arg1, arg2, true, true)
);

const mapStateToProps = (state, props) => {
  const DEFAULT_NAVIGATION = createDefaultNavigation(props);
  const {myStateGetter, dispatch, navigationContainer = DEFAULT_NAVIGATION, setState} = props;
  const clearTransitionsThunk = (continueWith) => navigateFactory(myStateGetter, resetTranslation, setState, undefined, continueWith);
  const clearTransitions =  (continueWith) => dispatch(clearTransitionsThunk(continueWith)());
  const history =  historyFactory({myStateGetter, dispatch, navigationContainer, setState, DefaultNavigation: DEFAULT_NAVIGATION});
  const {location} = history;
  return {
    clearTransitions,
    ...props,
    location,
    navigationContainer,
    performTransitions: context => dispatch(performTransitionsThunkFactory(context, myStateGetter, setState,  clearTransitions)),
    history
  };
};

export const ReduxRouter = withExternalState(mapStateToProps, 'ReduxNavigation')(ReduxRouterClass);

export default ReduxRouter;
