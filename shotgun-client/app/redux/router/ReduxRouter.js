import React, {Component} from 'react';
import { View } from 'react-native-animatable';
import {Dimensions, BackHandler} from 'react-native';
import { withExternalStateFactory } from '../withExternalState';
import Logger from 'common/Logger';
import {ErrorRegion, LoadingScreen} from 'common/components';
import { memoize } from '../memoize';
import removeProperties from '../removeProperties';
import NavigationContainerTranslator from './utils/NavigationContainerTranslator';
import TransitionManager from './utils/TransitionManager';
import matchPath from './utils/matchPath';
import * as RouteUtils from './utils/routeUtils';
import invariant  from 'invariant';
import {Text} from 'native-base';

const {width: deviceWidth, height: deviceHeight} = Dimensions.get('window');

class ReduxRouterClass extends Component{
  constructor(props){
    super(props);
    invariant(props.path, 'All routers must have a path');
    this.transitionManager = new TransitionManager(props.name);
    this.performTransition = this.performTransition.bind(this);
    this.handleRef = this.handleRef.bind(this);
    this.handleActualComponentRef = this.handleActualComponentRef.bind(this);
    this.doesMatch = memoize((loc, path) => matchPath(loc, path));
  }


  componentDidMount(){
    if (this.props.name === 'AppRouter'){
      BackHandler.addEventListener('hardwareBackPress', this.handleBack);
    }
    this.performTransition(undefined, this.props);
  }

  componentWillUnmount(){
    if (this.props.name === 'AppRouter'){
      BackHandler.removeEventListener('hardwareBackPress', this.handleBack);
    }
    Logger.info("Throwing redux router away as for some reason we don't think we need it anymore ");
  }


  handleBack(){
    const { history } = this.props;
    history.goBack();
    return true;
  }

  async componentDidUpdate(oldProps){
    this.transitionManager.log('Component did update');
    this.performTransition(oldProps, this.props);
  }

  async performTransition(oldProps = {}, newProps){
    const newNavigationContainerTranslator = newProps.navigationContainerTranslator;
    const oldNavigationContainerTranslator = oldProps.navigationContainerTranslator;
    const diff = NavigationContainerTranslator.diff(oldNavigationContainerTranslator, newNavigationContainerTranslator);
    if (diff){
      this.transitionManager.performTransition(diff);
    }
  }

  handleRef(route, ref){
    this.transitionManager.initialize(route, ref);
  }

  handleActualComponentRef(route, actualComponentRef){
    this.transitionManager.initializeActualComponent(route, actualComponentRef);
  }

  render() {
    Logger.info(`${this.props.name} - rendering`);
    const {width = deviceWidth, height  = deviceHeight, history, customLoadingText, navigationContainerTranslator, path: parentPath, style = {}} = this.props;
    const reduxRouterPropertiesToPassToEachRoute = removeProperties(this.props, ['stateKey', 'children', 'defaultRoute', 'setStateWithPath', 'setState', 'name', 'navigationContainerTranslator'] );
    const routesToRender = navigationContainerTranslator.getRoutesToRender();

    if (navigationContainerTranslator.pendingDefaultRouteTransition){
      setTimeout(() => history.replace(navigationContainerTranslator.pendingDefaultRouteTransition));
      return <LoadingScreen text={customLoadingText || `Forwarding to ${navigationContainerTranslator.pendingDefaultRouteTransition.pathname}`}/>;
    }

    return <View style={{flex: 1, ...style, height, width}}>
      <ErrorRegion/>
      {routesToRender.length ? routesToRender.map(
        (rt) => {
          const {componentProps} = rt;
          const { component: ComponentForRoute, ...otherPropsDeclaredOnRouteElement} = componentProps;
          const match = this.doesMatch(history.location.pathname, rt.path);
          let  completeProps = { ...reduxRouterPropertiesToPassToEachRoute, parentPath};
          completeProps = { ...completeProps, route: rt, path: rt.path};
          completeProps = { ...completeProps, ...otherPropsDeclaredOnRouteElement};
          completeProps = { ...completeProps, height, width};
          completeProps = { ...completeProps, match};
          return <View
            useNativeDriver={true}
            style={{ position: 'absolute',
              ...style,
              height, width,
              backgroundColor: 'white',
              left: 0,
              top: 0, minHeight: height, minWidth: width, maxHeight: height, maxWidth: width}} key={rt.path} ref={ref => {this.handleRef(rt, ref);}}>
            <ComponentForRoute ref={ComponentForRoute.prototype.render ? ref => {this.handleActualComponentRef(rt, ref);} : undefined} key={rt.path} {...completeProps}/>
          </View>;
        }
      ) : <Text>{`No routes found to match the path ${history.location.pathname} routes are ${navigationContainerTranslator.printAllRoutes}`}</Text>}
    </View>;
  }
}

const navigateFactory = (getNewContainer, setState, dispatch) => (action, state) => {
  const newnavigationContainer =  getNewContainer(RouteUtils.parseAction(action, state));
  if (newnavigationContainer){
    setState({navigationContainer: newnavigationContainer}, undefined, dispatch);
  }
};


const CompareArgruments = (arg1, arg2, idx) => {
  if (idx === 3){
    if (arg1 && arg2){
      return arg1.length == arg2.length;
    }
    return arg1 === arg2;
  }
  return arg1 === arg2;
};


const mapStateToProps = () => {
  const memoizedFactory = memoize((navigationContainer, path, defaultRoute, children, name) => {
    const routesInScope = RouteUtils.getRoutesForChildren(children, path);
    try {
      return new NavigationContainerTranslator(navigationContainer || NavigationContainerTranslator.createDefaultNavigation(path, defaultRoute), path, defaultRoute, routesInScope);
    } catch (error){
      throw new Error('Issue initing nav container translator for ' + name + ' - ' + error);
    }
  }, CompareArgruments);
  
  const fromProps = (props)=> {
    if (!props){
      return;
    }
    const {navigationContainer, path, defaultRoute, children, name} = props;
    return memoizedFactory(navigationContainer, path, defaultRoute, children, name);
  };

  const historyFactory = memoize(
    (navigationContainer, setState, dispatch) => {
      invariant(setState, 'Set state must be defined');
  
      const replace = navigateFactory((action) => navigationContainer.replace(action), setState, dispatch);
      const goBack = navigateFactory((action) => navigationContainer.goBack(action), setState, dispatch);
      const just = navigateFactory((action) => navigationContainer.just(action), setState, dispatch);
      const push = navigateFactory((action) => navigationContainer.next(action), setState, dispatch);
      const location = navigationContainer.location;
      return {
        location,
        replace,
        goBack,
        just,
        push
      };
    }
  );

  return (state, props) => {
    const {setState, dispatch} = props;
    const navigationContainerTranslator = fromProps(props);
    const history =  historyFactory(navigationContainerTranslator, setState, dispatch);
    const {location} = history;
    return {
      navigationContainerTranslator,
      ...props,
      location,
      history
    };
  };
};

export const ReduxRouter = withExternalStateFactory(mapStateToProps, 'ReduxNavigation')(ReduxRouterClass);

export default ReduxRouter;
