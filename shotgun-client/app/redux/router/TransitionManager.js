import Logger from 'common/Logger';
import React from 'react';
import * as RouteUtils from './routeUtils';


const combine = async (promises, props) => {
  const result = [];
  promises.filter(c => c).forEach(
    pr => result.push(pr(props))
  );
  return result;
};

const unwrap = (component) => {
  let result = component;
  while (result.wrappedInstance){
    result = result.wrappedInstance;
  }
  return result;
};

export default class TransitionManager{
  constructor(name){
    this.name = name;
    this.initializedRouteElementReferences = {};
    this.performTransition = this.performTransition.bind(this);
    this.getRoutesNeedingTransition = this.getRoutesNeedingTransition.bind(this);
    this.initialize = this.initialize.bind(this);
    this.initializeActualComponent = this.initializeActualComponent.bind(this);
    this.completeTransition = this.completeTransition.bind(this);
  }

  log(message){
    Logger.info(`TransitionManager-${this.name} - ${message}`);
  }

  setDefaultRoute(defaultRoute){
    this.defaultRoute = RouteUtils.parseRoute(defaultRoute);
  }
  
  initialize(route, componentRef){
    this.log(`Initializing animation element ${route.key} ${componentRef}`);
    if (componentRef === null){
      return;
    }
    RouteUtils.ensureComponentIsNative(componentRef);
    const existingReference = this.initializedRouteElementReferences[route.key] || {};
    this.initializedRouteElementReferences[route.key] =  {...existingReference, componentRef, route: RouteUtils.removeDeltas(route)};
  }
  initializeActualComponent(route, actualComponentRef){
    this.log(`Initializing actual element ${route.key} ${actualComponentRef}`);
    if (actualComponentRef === null){
      return;
    }
    const existingReference = this.initializedRouteElementReferences[route.key] || {};
    const unwrappedActualComponentRef = unwrap(actualComponentRef);
    if (existingReference.callBeforeNavigateToOnActualComponent && unwrappedActualComponentRef.beforeNavigateTo){
      unwrappedActualComponentRef.beforeNavigateTo.bind(unwrappedActualComponentRef)();
    }
    this.initializedRouteElementReferences[route.key] =  {...existingReference, actualComponentRef: unwrappedActualComponentRef, callBeforeNavigateToOnActualComponent: undefined};
  }

  destroy(routeKey){
    const componentRouteRef = this.initializedRouteElementReferences[routeKey];
    if (componentRouteRef){
      const {oneOffDestruction} = componentRouteRef;
      if (oneOffDestruction){
        oneOffDestruction();
      }
      delete this.initializedRouteElementReferences[routeKey];
    }
  }
  
  getRoutesNeedingTransition(navigationContainer){
    const routes = Object.values(this.initializedRouteElementReferences).map(c => c.route);
    this.log(`Checking if routes need to transion routes are ${routes.join(',')} and default route is ${JSON.stringify(this.defaultRoute)}`);
    const result = RouteUtils.getOrderedRoutes(routes, navigationContainer, true, this.defaultRoute);
    this.log(`Found ${result.length} routes that need to transition`);
    return result;
  }

  async performTransition(routes, navigationContainer, {lifeCycles, props}){
    if (this.navigationContainer == navigationContainer){
      return;
    }
    this.navigationContainer = navigationContainer;
    this.isMidTransition = true;
    const transitionPromises = [];
    const keysToTransition = [];
    const _this = this;
    this.log('Attempting to perform transition');
    
    routes.filter(rt=> RouteUtils.getAnimationType(rt)).forEach(
      (route) => {
        const routeElement = _this.initializedRouteElementReferences[route.key];
        this.log(`Performing transition for ${route.key} ${Object.keys(routeElement)}`);
        const {actualComponentRef, componentRef} = routeElement;
        const {beforeNavigateTo} = lifeCycles[route.key];
        let beforeNavigateToFromActualComponent;
        if (!actualComponentRef){
          this.log(`Havent found the actual component ref so flagging initialization for  ${route.key}`);
          _this.initializedRouteElementReferences[route.key] = {...routeElement, callBeforeNavigateToOnActualComponent: true};
        } else {
          beforeNavigateToFromActualComponent = actualComponentRef.beforeNavigateTo ? actualComponentRef.beforeNavigateTo.bind(actualComponentRef) : undefined;
        }
        const transitionPromise = combine([
          beforeNavigateTo,
          beforeNavigateToFromActualComponent,
          () => _this.transition(componentRef, route)
        ], props);
        keysToTransition.push(route.key);
        transitionPromises.push(transitionPromise);
      }
    );
    this.log(`Performing transition for ${transitionPromises.length} elements. Keys are  "${keysToTransition.join(',')}" and routes are ${JSON.stringify(routes)}`);
    await Promise.all(transitionPromises);
    return transitionPromises.length;
  }

  async transition(componentRef, route){
    const animationType = RouteUtils.getAnimationType(route);
    if (animationType){
      componentRef.setNativeProps({
        style: {
          zIndex: 1,
        },
      });
      await componentRef.animate(animationType, RouteUtils.getDuration(route));
      componentRef.setNativeProps({
        style: {
          zIndex: 0,
        },
      });
    }
  }
 
  completeTransition(){
    this.isMidTransition = false;
  }

  getInitializedRouteReferences(){
    return Object.values(this.initializedRouteElementReferences).map(c => c.route);
  }

  getOrderedRoutesFromChildren(children, navigationContainer = {}, filterForTransitions){
    return RouteUtils.getOrderedRoutes(RouteUtils.getRoutesForChildren(children), navigationContainer, filterForTransitions, this.defaultRoute);
  }
}

