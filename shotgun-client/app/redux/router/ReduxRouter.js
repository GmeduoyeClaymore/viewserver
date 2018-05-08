import React, { Component } from 'react';
import { View } from 'react-native-animatable';
import { BackHandler, Keyboard } from 'react-native';
import { withExternalStateFactory } from '../withExternalState';
import Logger from 'common/Logger';
import { LoadingScreen } from 'common/components';
import { memoize } from '../memoize';
import removeProperties from '../removeProperties';
import NavigationContainerTranslator from './utils/NavigationContainerTranslator';
import NavigationTransformation from './utils/NavigationTransformation';
import TransitionManager from './utils/TransitionManager';
import matchPath from './utils/matchPath';
import * as RouteUtils from './utils/routeUtils';
import invariant from 'invariant';
import shotgun from 'native-base-theme/variables/shotgun';
import { Container } from 'native-base';
import { setStateIfIsMounted } from 'custom-redux';

class ReduxRouterClass extends Component {
  constructor(props) {
    super(props);
    invariant(props.path, 'All routers must have a path');
    this.transitionManager = new TransitionManager(props.name, this.forceUpdate.bind(this));
    this.performTransition = this.performTransition.bind(this);
    this.handleRef = this.handleRef.bind(this);
    this.handleBack = this.handleBack.bind(this);
    this.getHeight = this.getHeight.bind(this);
    this.handleActualComponentRef = this.handleActualComponentRef.bind(this);
    setStateIfIsMounted(this);
  }

  componentWillMount() {
    const { resizeForKeyboard = false } = this.props;
    super.setState({ keyboardOffset: 0 });

    if (resizeForKeyboard) {
      Keyboard.addListener('keyboardWillShow', this.keyboardWillShow);
      Keyboard.addListener('keyboardDidShow', this.keyboardWillShow);
    }

    Keyboard.addListener('keyboardDidHide', this.keyboardWillHide);
    Keyboard.addListener('keyboardWillHide', this.keyboardWillHide);
  }

  componentDidMount() {
    if (this.props.name === 'AppRouter') {
      BackHandler.addEventListener('hardwareBackPress', this.handleBack);
    }
    this.performTransition(undefined, this.props);
  }

  componentWillUnmount() {
    if (this.props.name === 'AppRouter') {
      BackHandler.removeEventListener('hardwareBackPress', this.handleBack);
    }
    Logger.info("Throwing redux router away as for some reason we don't think we need it anymore ");
  }

  handleBack() {
    const { historyOverrideFactory } = this.props;
    const history = historyOverrideFactory();
    history.goBack();
    return true;
  }

  async componentDidUpdate(oldProps) {
    this.transitionManager.log('Component did update');
    this.performTransition(oldProps, this.props);
  }

  async performTransition(oldProps = {}, newProps) {
    const newNavigationContainerTranslator = newProps.navigationContainerTranslator;
    const oldNavigationContainerTranslator = oldProps.navigationContainerTranslator;
    const diff = NavigationContainerTranslator.diff(oldNavigationContainerTranslator, newNavigationContainerTranslator, newNavigationContainerTranslator.defaultRoute, newNavigationContainerTranslator.routerPath);
    if (diff) {
      this.transitionManager.performTransition(diff, newNavigationContainerTranslator.defaultRoute.pathname, newNavigationContainerTranslator.routerPath);
    }
  }

  keyboardWillShow = (event) => {
    if (this.isMountedComponentMounted) {
      super.setState({ keyboardOffset: event.endCoordinates.height });
    }
  };

  keyboardWillHide = () => {
    if (this.isMountedComponentMounted) {
      super.setState({ keyboardOffset: 0 });
    }
  };

  handleRef(route, ref) {
    this.transitionManager.initialize(route, ref);
  }

  isRemoved(route) {
    this.transitionManager.isRemoved(route);
  }

  handleActualComponentRef(route, actualComponentRef) {
    this.transitionManager.initializeActualComponent(route, actualComponentRef);
  }

  getHeight(){
    const {height: initialHeight = shotgun.deviceHeight, hasFooter = false} = this.props;
    const {keyboardOffset} = this.state;
    return initialHeight - keyboardOffset + (hasFooter && keyboardOffset > 0 ? shotgun.footerHeight : 0);
  }

  render() {
    const { width = shotgun.deviceWidth, historyOverrideFactory, navigationContainerTranslator, path: parentPath, style = {}, isInBackground } = this.props;
    const height = this.getHeight();

    const reduxRouterPropertiesToPassToEachRoute = removeProperties(this.props, ['hasFooter', 'resizeForKeyboard', 'stateKey', 'history', 'historyOverrideFactory', 'children', 'defaultRoute', 'setStateWithPath', 'setState', 'name', 'navigationContainerTranslator']);
    const routesToRender = navigationContainerTranslator.getRoutesToRender();
    const result = <Container>
      {routesToRender.length ? routesToRender.map(
        (rt) => {
          const { componentProps, match, navContainerOverride } = rt;
          const { component: ComponentForRoute, ...otherPropsDeclaredOnRouteElement } = componentProps;
          const history = historyOverrideFactory(navContainerOverride);
          const { location } = history;
          const isInBackground = !matchPath(location.pathname, rt.path);
          let completeProps = { ...reduxRouterPropertiesToPassToEachRoute, parentPath };
          completeProps = { ...completeProps, route: rt, path: rt.path, isInBackground };
          completeProps = { ...completeProps, ...otherPropsDeclaredOnRouteElement };
          completeProps = { ...completeProps, height, width, isInBackground };
          completeProps = { ...completeProps, match, navContainerOverride, history, location };
          return this.isRemoved(rt) ? null : <View
            useNativeDriver={true}
            style={{
              position: 'absolute',
              ...style,
              height, width,
              left: 0,
              backgroundColor: shotgun.brandPrimary,
              zIndex: rt.index,
              top: 0, minHeight: height, minWidth: width, maxHeight: height, maxWidth: width
            }} key={rt.path} ref={ref => { this.handleRef(rt, ref); }}>
            <ComponentForRoute ref={ComponentForRoute.prototype.render ? ref => { this.handleActualComponentRef(rt, ref); } : undefined}  {...completeProps} />
          </View>;
        }
      ) : <LoadingScreen text="Navigating..." />}
    </Container>;

    if (!routesToRender.length) {
      Logger.info(`${isInBackground} - No routes found to match the path ${navigationContainerTranslator.location.pathname} routes are ${navigationContainerTranslator.printAllRoutes}`);
    }

    return result;
  }
}

const navigateFactory = (getNewContainer, setState, dispatch) => (action, state, continueWith) => {
  const parsedAction = RouteUtils.parseAction(action, state);

  const newnavigationContainer = getNewContainer(parsedAction);
  if (newnavigationContainer) {
    if (!parsedAction || parsedAction.dismissKeyboard != false) {
      Keyboard.dismiss();
    }

    setState({ navigationContainer: newnavigationContainer }, continueWith, dispatch);
  }
};

const mapStateToProps = () => {
  const memoizedFactory = memoize((navigationContainer, path, defaultRoute, children, name) => {
    const routesInScope = RouteUtils.getRoutesForChildren(children, path);
    try {
      return new NavigationContainerTranslator(navigationContainer || NavigationContainerTranslator.createDefaultNavigation(path), path, defaultRoute, routesInScope);
    } catch (error) {
      throw new Error('Issue initing nav container translator for ' + name + ' - ' + error);
    }
  });

  const fromProps = (props) => {
    if (!props) {
      return;
    }
    const { navigationContainer, path, defaultRoute, children, name, navContainerOverride } = props;
    return memoizedFactory(navContainerOverride || navigationContainer, path, defaultRoute, children, name);
  };

  const memoizedHistoryFactory = memoize(
    (navigationContextOverride, navigationContainer, setState, dispatch) => {
      invariant(setState, 'Set state must be defined');
      const navContainerOverride = navigationContextOverride && navigationContextOverride.version >= navigationContainer.version ? navigationContextOverride : navigationContainer;
      const tranformation = new NavigationTransformation(navContainerOverride);
      const replace = navigateFactory((action) => { invariant(action, 'Action must be defined'); return tranformation.replace(action); }, setState, dispatch);
      const goBack = navigateFactory((action) => tranformation.goBack(action), setState, dispatch);
      const just = navigateFactory((action) => { invariant(action, 'Action must be defined'); return tranformation.just(action); }, setState, dispatch);
      const push = navigateFactory((action) => { invariant(action, 'Action must be defined'); return tranformation.next(action); }, setState, dispatch);
      const location = tranformation.location;
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
    const { setState, dispatch } = props;
    const navigationContainerTranslator = fromProps(props);
    const historyOverrideFactory = navigationContextOverride => memoizedHistoryFactory(navigationContextOverride, navigationContainerTranslator.navContainer, setState, dispatch);
    const { location } = navigationContainerTranslator;

    return {
      navigationContainerTranslator,
      ...props,
      historyOverrideFactory,
      location,
    };
  };
};

export const ReduxRouter = withExternalStateFactory(mapStateToProps, 'ReduxNavigation')(ReduxRouterClass);

export default ReduxRouter;
