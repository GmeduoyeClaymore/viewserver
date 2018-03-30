import Logger from 'common/Logger';

import * as RouteUtils from './routeUtils';

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
  
  doAnyOfMyRoutesNeedToTransition(navigationContainer){
    return !this.isMidTransition && !!RouteUtils.getOrderedRoutes(Object.values(this.initializedRouteElementReferences).map(c => c.route), navigationContainer, true).length;
  }

  async performTransition(navigationContainer, defaultRoute){
    if (this.isMidTransition){
      Logger.info('Not transitioning mid transition');
    }
    const routes = RouteUtils.getOrderedRoutes(this.getInitializedRouteReferences(), navigationContainer, true, defaultRoute);
    this.isMidTransition = true;
    const transitionPromises = [];
    const keysToTransition = [];
    routes.filter(rt=> RouteUtils.getAnimationType(rt)).forEach(
      (route) => {
        const {componentRef} = this.initializedRouteElementReferences[route.key];
        keysToTransition.push(route.key);
        transitionPromises.push(this.transition(componentRef, route));
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

