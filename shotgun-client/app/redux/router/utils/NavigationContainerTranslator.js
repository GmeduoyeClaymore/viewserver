import Immutable from 'seamless-immutable';
import * as RouteUtils from './routeUtils';
import matchPath from './matchPath';
import invariant from 'invariant';
import { memoize } from '../../memoize';
import removeProperties from '../../removeProperties';
import NavigationTransformation from './NavigationTransformation';
const DefaultNavigationStack = [{
  pathname: '/'
}];

const MaxStackLength = 2;


export default class NavigationContainerTranslator{
  constructor(navContainer, routerPath, defaultRoute, routesInScope){
    invariant(navContainer, 'Nav container is required');
    invariant(routerPath, 'routerPath is required');
    invariant(defaultRoute, 'defaultRoute is required');
    invariant(routesInScope, 'routes in scope is required');
    this.navContainer = navContainer;
    this.routerPath = routerPath;
    this.defaultRoute = RouteUtils.parseRoute(routerPath, defaultRoute);
    this.routesInScope = routesInScope;
    this.tranformation = new NavigationTransformation(this.navContainer);
  }

  get location(){
    return  this.tranformation.location;
  }
  
  get navigationStack(){
    return  this.tranformation.navigationStack;
  }

  get navPointer(){
    return  this.tranformation.navPointer;
  }

  get version(){
    return  this.tranformation.version;
  }

  get printAllRoutes(){
    return JSON.stringify(this.routesInScope);
  }

  get pendingDefaultRouteTransition(){
    const match = matchPath(this.location.pathname, this.routerPath);
    if (match && match.isExact){
      const newStack = [...this.navigationStack];
      const newHead = {...this.location, ...removeProperties(this.defaultRoute, ['state'])};
      newStack[this.navContainer.navPointer] = newHead;
      return {...newHead, navContainerOverride: this.navContainer.setIn(['navigationStack'], newStack)};
    }
  }

  get routeThatShouldBeRendered(){
    let result = this.location;
    result =  result.pathname == this.routerPath ? this.defaultRoute : result;
    const index = this.routesInScope.findIndex(c=> matchPath(result.pathname, c.path));
    if (!!~index){
      const route = this.routesInScope[index];
      const {isReverse, transition = route.transition} = result;
      return {...route, pathname: route.path, transition, isReverse};
    }
  }
  
  static diff = memoize((prevContainer = {}, nextContainer = {}) => {
    if (!prevContainer && !nextContainer){
      return;
    }
    const isReverse = prevContainer.navPointer > nextContainer.navPointer;
    let result;
    if (!prevContainer.routeThatShouldBeRendered || !nextContainer.routeThatShouldBeRendered){
      if (prevContainer.routeThatShouldBeRendered){
        result = [{...prevContainer.routeThatShouldBeRendered, isRemove: true}];
      } else if (nextContainer.routeThatShouldBeRendered ){
        result = [{...nextContainer.routeThatShouldBeRendered, isAdd: true}];
      }
    } else {
      if (prevContainer.routeThatShouldBeRendered && nextContainer.routeThatShouldBeRendered){
        if (nextContainer.routeThatShouldBeRendered.pathname !== prevContainer.routeThatShouldBeRendered.pathname){
          if (matchPath(nextContainer.routeThatShouldBeRendered.pathname, prevContainer.routeThatShouldBeRendered.pathname)){
            result = [{...nextContainer.routeThatShouldBeRendered, isAdd: true}];
          } else {
            const {isReverse, transition} = nextContainer.routeThatShouldBeRendered;
            result = [{ ...nextContainer.routeThatShouldBeRendered, isAdd: true}, {...prevContainer.routeThatShouldBeRendered, isRemove: true, isReverse, transition}];
          }
        }
      }
    }
    if (isReverse && result){
      result.forEach(c => {
        c.isReverse = isReverse;
      });
    }
    return result;
  })

  getRouteFromScope(el, isCurrentLocation){
    const {routesInScope} = this;
    return routesInScope.map(rt => {
      const match = matchPath(el.pathname, rt);
      return {...rt, match, navContainerOverride: el.navContainerOverride, index: isCurrentLocation ? 1  : 0};
    }).filter(rt => rt.match)[0];
  }

  getRoutesToRender(){
    if (this.routesToRender){
      return this.routesToRender;
    }
    let result = [];
    const {navigationStack = []} = this.navContainer;
    const foundKeys = [];
    const croppedStack = navigationStack.slice(-MaxStackLength);
    croppedStack.forEach((el) => {
      const routeFromScope = this.getRouteFromScope(el, el == this.location);
      if (routeFromScope && !~foundKeys.indexOf(routeFromScope.path)){
        foundKeys.push(routeFromScope.path);
        result.push(routeFromScope);
      }
    });
    const pendingRouteTransition = this.pendingDefaultRouteTransition;
    if (pendingRouteTransition){
      const transitionElementToRender = this.getRouteFromScope(pendingRouteTransition);
      if (transitionElementToRender && !~result.findIndex(rt => rt.path === transitionElementToRender.path)){
        result = [...result,  transitionElementToRender];
      }
    }
    this.routesToRender = result;
    return result;
  }


  static createDefaultNavigation(){
    const navigationStack = DefaultNavigationStack;
    return Immutable({
      navPointer: 0,
      navigationStack,
      version: 0
    });
  }
}
