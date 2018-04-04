import React, {Component} from 'react';
import { View } from 'react-native-animatable';
import {Dimensions} from 'react-native';
import { withExternalState } from '../withExternalState';

import Logger from 'common/Logger';
import {ErrorRegion} from 'common/components';
import { memoize } from '../memoize';
import removeProperties from '../removeProperties';
import NavigationContainerTranslator from './utils/NavigationContainerTranslator';
import TransitionManager from './utils/TransitionManager';
import matchPath from './utils/matchPath';
import * as RouteUtils from './utils/routeUtils';
import invariant  from 'invariant';

const {width: deviceWidth, height: deviceHeight} = Dimensions.get('window');

class ReduxRouterClass extends Component{
  constructor(props){
    super(props);
    invariant(props.path, 'All routers must have a path');
    this.transitionManager = new TransitionManager(props.name);
    this.performTransition = this.performTransition.bind(this);
    this.handleRef = this.handleRef.bind(this);
    this.handleActualComponentRef = this.handleActualComponentRef.bind(this);
  }

  componentDidMount(){
    this.performTransition(undefined, this.props);
  }

  componentWillUnmount(){
    Logger.info("Throwing redux router away as for some reason we don't think we need it anymore ");
  }

  async componentDidUpdate(oldProps){
    this.transitionManager.log('Component did update');
    this.performTransition(oldProps, this.props);
  }

  shouldComponentUpdate(newProps){
    const newNavigationContainerTranslator = NavigationContainerTranslator.fromProps(newProps);
    if (newNavigationContainerTranslator.defaultPathTranslation){
      newProps.history.replace(newNavigationContainerTranslator.defaultPathTranslation);
      return false;
    }
    return true;
  }

  async performTransition(oldProps, newProps){
    const newNavigationContainerTranslator = NavigationContainerTranslator.fromProps(newProps);
    const oldNavigationContainerTranslator = NavigationContainerTranslator.fromProps(oldProps);
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
    const {width = deviceWidth, height  = deviceHeight, history, path: parentPath, style = {}} = this.props;
    const navContainerTranslator  = NavigationContainerTranslator.fromProps(this.props);
    const reduxRouterPropertiesToPassToEachRoute = removeProperties(this.props, ['stateKey', 'children', 'defaultRoute', 'setStateWithPath', 'setState', 'name'] );
    const routesToRender = navContainerTranslator.getRoutesToRender(this.getPathFromElement);

    return <View style={{flex: 1, ...style, height, width}}>
      <ErrorRegion/>
      {routesToRender.map(
        (rt) => {
          const {componentProps} = rt;
          const { component: ComponentForRoute, ...otherPropsDeclaredOnRouteElement} = componentProps;
          const match = matchPath(history.location.pathname, rt.path);
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
            <ComponentForRoute ref={ref => {this.handleActualComponentRef(rt, ref);}} key={rt.path} {...completeProps}/>
          </View>;
        }
      )}
    </View>;
  }
}

const navigateFactory = (getNewContainer, setState, dispatch) => (action, state) => {
  const newnavigationContainer =  getNewContainer(RouteUtils.parseAction(action, state));
  if (newnavigationContainer){
    setState({navigationContainer: newnavigationContainer}, undefined, dispatch);
  }
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

const mapStateToProps = (state, props) => {
  const {setState, dispatch} = props;
  const navigationContainer = NavigationContainerTranslator.fromProps(props);
  const history =  historyFactory(navigationContainer, setState, dispatch);
  const {location} = history;
  return {
    ...props,
    location,
    history
  };
};

export const ReduxRouter = withExternalState(mapStateToProps, 'ReduxNavigation')(ReduxRouterClass);

export default ReduxRouter;
