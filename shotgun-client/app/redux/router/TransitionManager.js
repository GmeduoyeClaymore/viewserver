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

export default class TransitionManager{
  constructor(){
    this.initializedRouteElementReferences = {};
    this.performTransition = this.performTransition.bind(this);
    this.doAnyOfMyRoutesNeedToTransition = this.doAnyOfMyRoutesNeedToTransition.bind(this);
    this.initialize = this.initialize.bind(this);
    this.completeTransition = this.completeTransition.bind(this);
  }
  
  initialize(route, componentRef){
    if (componentRef === null){
      return;
    }
    RouteUtils.ensureComponentIsNative(componentRef);
    this.initializedRouteElementReferences[route.key] =  {componentRef, route: RouteUtils.removeDeltas(route)};
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
  
  doAnyOfMyRoutesNeedToTransition(navigationContainer){
    return RouteUtils.getOrderedRoutes(Object.values(this.initializedRouteElementReferences).map(c => c.route), navigationContainer, true);
  }

  async performTransition(routes, navigationContainer, {lifeCycles, props}){
    this.isMidTransition = true;
    const transitionPromises = [];
    const keysToTransition = [];
    
    routes.filter(rt=> RouteUtils.getAnimationType(rt)).forEach(
      (route) => {
        const {componentRef} = this.initializedRouteElementReferences[route.key];
        const {beforeNavigateTo} = lifeCycles[route.key];
        const transitionPromise = combine([
          beforeNavigateTo,
          () => this.transition(componentRef, route)
        ], props);
        keysToTransition.push(route.key);
        transitionPromises.push(transitionPromise);
      }
    );
    Logger.info(`Performing transition for ${transitionPromises.length} elements. Keys are  "${keysToTransition.join(',')}" and routes are ${JSON.stringify(routes)}`);
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

  getOrderedRoutesFromChildren(children, navigationContainer = {}, filterForTransitions, defaultRoute){
    return RouteUtils.getOrderedRoutes(RouteUtils.getRoutesForChildren(children), navigationContainer, filterForTransitions, defaultRoute);
  }
}

