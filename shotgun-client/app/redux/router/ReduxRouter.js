import React, {Children, Component, isValidElement, cloneElement} from 'react';
import { createAnimatableComponent, View, Text } from 'react-native-animatable';
import {ScrollView, Dimensions} from 'react-native';
import { withExternalState } from '../withExternalState';
import TransitionFactory from './TransitionFactory';


const AnimatableContainer = createAnimatableComponent(ScrollView);

const DefaultNavigationStack = [{
  pathname: '/'
}];

const {width: deviceWidth, height: deviceHeight} = Dimensions.get('window');

const getKeyFromElement = (element, index) => {
  const { path} = element.props;
  return path ? path.replace('//', '_') : element.component ? `${element.component.name}_${index}` : `component_${index}`;
};

class ReduxRouterClass extends Component{
  constructor(props){
    super(props);
    this.transitionFactory = new TransitionFactory();
  }

  shouldComponentUpdate(newProps, newState){
    const {children, navigationContainer} = newProps;
    const {routes} = newState;
    if (children != this.props.children){
      return true;
    }
    if (navigationContainer != this.props.navigationContainer){
      return true;
    }
    if (routes != this.state.routes){
      return true;
    }
    return false;
  }

  componentWillMount(){
    if (this.props.children){
      this.registerChildrenAsReduxRoutes(this.props.children);
    }
  }

  componentWillReceiveProps(newProps){
    const {children} = newProps;
    if (children != this.props.children){
      this.registerChildrenAsReduxRoutes(children);
    }
  }

  registerChildrenAsReduxRoutes(children){
    const routes = [];
    let index = 0;
    Children.forEach(children, (element) => {
      if (!isValidElement(element)) return;
      const { path, exact, strict, sensitive} = element.props;
      routes.push({ path, exact, strict, sensitive, key: getKeyFromElement(element, index) });
      index++;
    });
    super.setState({routes});
  }

  componentDidUpdate(){
    const {navigationContainer} = this.props;
    this.transitionFactory.performTransition(navigationContainer);
  }

  createComponent(routeDelta, additionalProps = {}){
    const {children, ...rest} = this.props;
    const childForRoute = children.find((c, idx)=> getKeyFromElement(c, idx) == routeDelta.key);
    if (!childForRoute){
      throw new Error(`Unable to find child for route ${routeDelta.key}`);
    }
    return cloneElement(childForRoute, { ...routeDelta, ...rest, ...childForRoute.props, ...additionalProps});
  }
  

  handleRef(routeDelta, ref){
    this.transitionFactory.initialize(routeDelta, ref);
  }

  render() {
    const {width = deviceWidth, height  = deviceHeight, navigationContainer} = this.props;
    const  {routes} = this.state;
    const routeDeltas = this.transitionFactory.getOrderedRoutes(routes, navigationContainer );
    return <AnimatableContainer style={{flex: 1, height, width}}  contentContainerStyle={{flex: 1, height, width}}>
      {routeDeltas.map(
        (rt, idx) => {
          return this.createComponent(rt, {key: idx, style: {height, width}, ref: ref => {this.handleRef(rt, ref);}});
        }
      )}
    </AnimatableContainer>;
  }
}

const nextTranslation = (navigationContainer, navAction) => {
  const {navigationStack} = navigationContainer;
  const newNavigationStack = [...navigationStack, {...navAction}];
  const {navigationPointer = 0} = navigationContainer;
  return navigationContainer.setIn({navigationPointer: navigationPointer + 1, newNavigationStack});
};

const goBackTranslation = (navigationContainer) => {
  const {navigationStack} = navigationContainer;
  const newNavigationStack = [...navigationStack.slice(navigationStack.length - 1)];
  const {navigationPointer = 0} = navigationContainer;
  return navigationContainer.setIn({navigationPointer: navigationPointer - 1, newNavigationStack});
};

const replaceTranslation = (navigationContainer, navAction) => {
  const {navigationStack} = navigationContainer;
  const newNavigationStack = [...navigationStack.slice(navigationStack.length - 1), {...navAction}];
  const {navigationPointer = 0} = navigationContainer;
  return navigationContainer.setIn({navigationPointer: navigationPointer + 1, newNavigationStack});
};

const navigateFactory = (myStateGetter, navigationContainerTranslation, setState, DefaultNavigation) => (action) =>  async (dispatch, getState) => {
  const componentState = myStateGetter(getState()) || {};
  const {navigationContainer = DefaultNavigation} = componentState;
  const newnavigationContainer =  navigationContainerTranslation(navigationContainer, action);
  setState({navigationContainer: newnavigationContainer}, undefined, dispatch);
};

const createDefaultNavigation = (props) => {
  const navigationStack = props.defaultNavigation || DefaultNavigationStack;
  return {
    navigationStack,
    navigationPointer: 0
  };
};

const mapStateToProps = (state, props) => {
  const DEFAULT_NAVIGATION = createDefaultNavigation(props);
  const {myStateGetter, dispatch, navigationContainer = DEFAULT_NAVIGATION, setState} = props;
  const replace = navigateFactory(myStateGetter, replaceTranslation, setState, DEFAULT_NAVIGATION);
  const goBack = navigateFactory(myStateGetter, goBackTranslation, setState, DEFAULT_NAVIGATION);
  const next = navigateFactory(myStateGetter, nextTranslation, setState, DEFAULT_NAVIGATION);

  return {
    ...props,
    navigationContainer,
    history: {
      replace: action => dispatch(replace(action)),
      goBack: action => dispatch(goBack(action)),
      push: action => dispatch(next(action)),
    }
  };
};

export const ReduxRouter = withExternalState(mapStateToProps)(ReduxRouterClass);

export default ReduxRouter;
