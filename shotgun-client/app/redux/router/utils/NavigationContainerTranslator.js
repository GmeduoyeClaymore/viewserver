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

  get defaultPathTranslation(){
    if (this.location.pathname == this.routerPath){
      return {...this.location, ...removeProperties(this.defaultRoute, ['state'])};
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

  getRoutesToRender(){
    if (this.routesToRender){
      return this.routesToRender;
    }
    const result = [];
    const {navigationStack = [], navPointer} = this.navContainer;
    const {routesInScope} = this;
    const foundKeys = [];
    let croppedStack = navigationStack.slice(0, navPointer + 1);
    croppedStack = croppedStack.slice(-MaxStackLength);
    croppedStack.forEach(
      el => {
        routesInScope.forEach(
          rt => {
            if (!!~foundKeys.indexOf(rt.path)){
              return;
            }
            const match = matchPath(el.pathname, rt);
            if (match){
              foundKeys.push(rt.path);
              result.push(rt);
            } else if (rt.persistent){
              Logger.info(`Loading persistent route ${JSON.stringify(rt)} to path ${el.pathname}`);
              foundKeys.push(rt.path);
              result.unshift({...removeDeltas(rt), match, index: -1, persistent: true});
            }
          }
        );
      }
    );
    this.routesToRender = result;
    return result;
  }

  static fromProps(props){
    if (!props){
      return;
    }
    const {navigationContainer, path, defaultRoute, children, name} = props;
    return NavigationContainerTranslator.memoizedFactory(navigationContainer, path, defaultRoute, children, name);
  }

  static memoizedFactory = memoize((navigationContainer, path, defaultRoute, children, name) => {
    const routesInScope = RouteUtils.getRoutesForChildren(children, path);
    try {
      return new NavigationContainerTranslator(navigationContainer || NavigationContainerTranslator.createDefaultNavigation(path, defaultRoute), path, defaultRoute, routesInScope);
    } catch (error){
      throw new Error('Issue initing nav container translator for ' + name + ' - ' + error);
    }
  })


  static createDefaultNavigation(routerPath, defaultRoute){
    const navigationStack = defaultRoute ? [RouteUtils.parseRoute(routerPath, defaultRoute)] : DefaultNavigationStack;
    return Immutable({
      navPointer: 0,
      navigationStack
    });
  }
}
