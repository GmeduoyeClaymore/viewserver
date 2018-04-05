import Immutable from 'seamless-immutable';
import * as RouteUtils from './routeUtils';
import matchPath from './matchPath';
import Logger from 'common/Logger';
import invariant from 'invariant';
import { memoize } from '../../memoize';
import removeProperties from '../../removeProperties';
import { isEqual } from '../../is-equal';
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

    this.goBack = this.goBack.bind(this);
    this.next = this.next.bind(this);
    this.replace = this.replace.bind(this);
  }

  get printAllRoutes(){
    return JSON.stringify(this.routesInScope);
  }
    
  get location(){
    const navPointer = this.navContainer.navPointer;
    return  this.navContainer.navigationStack[navPointer];
  }

  get pendingDefaultRouteTransition(){
    const match = matchPath(this.location.pathname, this.routerPath);
    if (match && match.isExact){
      return {...this.location, ...removeProperties(this.defaultRoute, ['state']), navContainerOverride: this.tempNavContainerPendingDefaultRouteTransition};
    }
  }

  get tempNavContainerPendingDefaultRouteTransition(){
    const match = matchPath(this.location.pathname, this.routerPath);
    if (match && match.isExact){
      const existingStack = this.navContainer.navigationStack;
      const previousStackHead = this.navContainer[this.navContainer.navPointer];
      const newStack = [...existingStack.slice(0, existingStack.length - 1), {...previousStackHead, ...removeProperties(this.defaultRoute, ['state'])}];
      return this.navContainer.setIn(['navigationStack'], newStack);
    }
  }

  get head(){
    let result = this.location;
    result =  result.pathname == this.routerPath ? this.defaultRoute : result;
    if (!!~this.routesInScope.findIndex(c=> matchPath(result.pathname, c.path))){
      return result;
    }
  }

  get version(){
    return this.navContainer.version;
  }
  
  goBack(){
    const previousStackHead = this.navContainer[this.navContainer.navPointer];
    let newNavPointer = this.navContainer.navPointer - 1;
    newNavPointer = !!~newNavPointer ? newNavPointer : 0;
    const existingStack = this.navContainer.navigationStack;
    const newStack = [...existingStack.slice(0, newNavPointer + 1)];
    const newStackHead = newStack.pop();
    return this.incrementVersionCounter().setIn(['navPointer'], newNavPointer ).setIn(['navigationStack'], [...newStack, {...newStackHead, isReverse: true, transition: previousStackHead ?  previousStackHead.transition : undefined}]);
  }
  
  next(action){
    const newNavPointer = this.navContainer.navPointer + 1;
    const previousStackHead = this.navContainer[this.navContainer.navPointer];
    if (isEqual(action, previousStackHead, true)){
      Logger.info(`Action has already been added to the stack ${JSON.stringify(action)} ignoring next`);
      return this.navContainer;
    }
    const existingStack = this.navContainer.navigationStack;
    const newStack = [...existingStack, action];
    return this.incrementVersionCounter().setIn(['navPointer'], newNavPointer).setIn(['navigationStack'], newStack);
  }

  just(action){
    const newStack = [action];
    return this.incrementVersionCounter().setIn(['navPointer'], 0).setIn(['navigationStack'], newStack);
  }
  
  replace(action){
    const existingStack = this.navContainer.navigationStack;
    const previousStackHead = this.navContainer[this.navContainer.navPointer];
    if (isEqual(action, previousStackHead, true)){
      Logger.info(`Action has already been added to the stack ${JSON.stringify(action)} ignoring replace`);
      return this.navContainer;
    }
    const newStack = [...existingStack.slice(0, existingStack.length - 1), action];
    return this.incrementVersionCounter().setIn(['navigationStack'], newStack);
  }

  incrementVersionCounter(){
    const existingVersion = this.navContainer.version || 0;
    return this.navContainer.setIn(['version'], existingVersion + 1);
  }
  
  static diff = memoize((prevContainer, nextContainer) => {
    if (!prevContainer && !nextContainer){
      return;
    }
    if (!prevContainer || !nextContainer){
      if (prevContainer && prevContainer.head){
        return [{...prevContainer.head, isRemove: true}];
      } else if (nextContainer  && nextContainer.head ){
        return [{...nextContainer.head, isAdd: true}];
      }
    } else {
      if (prevContainer.head && nextContainer.head){
        if (nextContainer.head.pathname !== prevContainer.head.pathname){
          if (matchPath(nextContainer.head.pathname, prevContainer.head.pathname)){
            return [{...nextContainer.head, isAdd: true}];
          }
          return [{...nextContainer.head, isAdd: true}, {...prevContainer.head, isRemove: true}];
        }
      }
    }
  })

  getRouteFromScope(el){
    const {routesInScope} = this;
    return routesInScope.map(rt => {
      const match = matchPath(el.pathname, rt);
      return {...rt, match, navContainerOverride: el.navContainerOverride};
    }).filter(rt => rt.match)[0];
  }

  getRoutesToRender(){
    if (this.routesToRender){
      return this.routesToRender;
    }
    const result = [];
    const {navigationStack = [], navPointer} = this.navContainer;
    const foundKeys = [];
    let croppedStack = navigationStack.slice(0, navPointer + 1);
    croppedStack = croppedStack.slice(-MaxStackLength);
    croppedStack.forEach(el => {
      const routeFromScope = this.getRouteFromScope(el);
      if (routeFromScope && !~foundKeys.indexOf(routeFromScope.path)){
        foundKeys.push(routeFromScope.path);
        result.push(routeFromScope);
      }
    });
    this.routesToRender = result;
    return result;
  }


  static createDefaultNavigation(){
    const navigationStack = DefaultNavigationStack;
    return Immutable({
      navPointer: 0,
      navigationStack
    });
  }
}
