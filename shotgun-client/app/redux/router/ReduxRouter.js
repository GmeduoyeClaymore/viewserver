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
    this.processChildren = this.processChildren.bind(this);
    this.lifeCycles = {};
    this.state = {
      initializationErrors: []
    };
  }

  componentDidMount(){
    this.processChildren(this.props.children);
  }

  componentWillUnmount(){
    Logger.info("Throwing redux router away as for some reason we don't think we need it anymore ");
  }


  componentWillReceiveProps(newProps){
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

  shouldComponentUpdate(newProps){
    const {children, navigationContainer} = newProps;
    if (children != this.props.children){
      return true;
    }

    if (navigationContainer != this.props.navigationContainer){
      return true;
    }

    return false;
  }

  async componentDidUpdate(){
    if (!this.childrenProcessed){
      this.processChildren(this.props.children);
    }
    this.performTransition();
  }

  async performTransition(){
    this.props.performTransitions(this);
  }

  handleRef(route, ref){
    this.transitionManager.initialize(route, ref);
  }

  render() {
    const {width = deviceWidth, height  = deviceHeight, navigationContainer, history, path: parentPath} = this.props;
    const {initializationErrors} = this.state;
    const {stateKey: _, children, defaultRoute, setStateWithPath, setState, ...reduxRouterPropertiesToPassToEachRoute} = this.props;
    const defaultRootObj = RouteUtils.parseRoute(defaultRoute);
    const routesToRender = this.transitionManager.getOrderedRoutesFromChildren(children, navigationContainer, false, defaultRootObj);
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
            <ComponentForRoute key={rt.path} {...completeProps}/>
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
    newNavigationStack = [...itemsNotNeedingTransition, {...addition, index: itemsNotNeedingTransition.length + 1} ];
  } else {
    if (!navAction.pathname.includes(previousFinalNavItem.pathname)){
      const remove = {isReverse, ...previousFinalNavItem, isRemove: true};
      newNavigationStack = [...itemsNotNeedingTransition, remove, addition ];
    } else {
      newNavigationStack = [...itemsNotNeedingTransition, previousFinalNavItem, {...addition, index: itemsNotNeedingTransition.length + 2}  ];
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
  

  const routes = context.transitionManager.doAnyOfMyRoutesNeedToTransition(navigationContainer);
  const nonPersistentRoutes = routes.filter(rt => !rt.persistent);
  if (!navigationContainer || !nonPersistentRoutes || !nonPersistentRoutes.length ){
    return;
  }
  //setState({isTransitioning: true}, undefined, dispatch);

  const hasTransitioned = await context.transitionManager.performTransition(routes, navigationContainer, context);
  if (hasTransitioned){
    clearTransitions(() => {
      Logger.info('Now clearing transitions');
      context.transitionManager.completeTransition();
    //setState({isTransitioning: false}, undefined, dispatch);
    });
    dispatch({ type: 'TRANSITIONS_PERFORMED'});
  }
};

const historyFactory = memoize(
  ({myStateGetter, dispatch, navigationContainer, setState, DefaultNavigation}) => {
    const replaceThunk = navigateFactory(myStateGetter, moveNavigation(true), setState, DefaultNavigation);
    const goBackThunk = navigateFactory(myStateGetter, goBackTranslation, setState, DefaultNavigation);
    const nextThunk = navigateFactory(myStateGetter, moveNavigation(false), setState, DefaultNavigation);
    const location = navigationContainer.navigationStack[navigationContainer.navigationStack.length - 1];
    return {
      location,
      replace: (path, navState) => dispatch(replaceThunk(path, navState)),
      goBack: action => dispatch(goBackThunk(action)),
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
