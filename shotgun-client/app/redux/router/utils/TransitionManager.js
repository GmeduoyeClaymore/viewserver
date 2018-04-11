import Logger from 'common/Logger';
import React from 'react';
import * as RouteUtils from './routeUtils';
import matchPath from './matchPath';

const combine = async (promises) => {
  const result = [];
  for (let i = 0; i < promises.length; i++){
    if (promises[i]){
      result.push(await promises[i]());
    }
  }
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
  constructor(name, updateRouter){
    this.name = name;
    this.updateRouter = updateRouter;
    this.initializedRouteElementReferences = {};
    this.performTransition = this.performTransition.bind(this);
    this.initialize = this.initialize.bind(this);
    this.removeElement = this.removeElement.bind(this);
    this.initializeActualComponent = this.initializeActualComponent.bind(this);
  }

  log(message){
    Logger.info(`TransitionManager-${this.name} - ${message}`);
  }

  isRemoved(route){
    const element = this.initializedRouteElementReferences[route.path] || {};
    return element.flaggedForRemove;
  }
  
  initialize(route, componentRef){
    this.log(`Initializing animation element ${route.path} ${componentRef}`);
    if (componentRef === null){
      return;
    }
    RouteUtils.ensureComponentIsNative(componentRef);
    const existingReference = this.initializedRouteElementReferences[route.path] || {};
    this.initializedRouteElementReferences[route.path] =  {...existingReference, componentRef, route};
  }

  initializeActualComponent(route, actualComponentRef){
    this.log(`Initializing actual element ${route.path} ${actualComponentRef}`);
    if (actualComponentRef === null){
      return;
    }
    const existingReference = this.initializedRouteElementReferences[route.path] || {};
    const unwrappedActualComponentRef = unwrap(actualComponentRef);
    if (existingReference.callBeforeNavigateToOnActualComponent && unwrappedActualComponentRef.beforeNavigateTo){
      unwrappedActualComponentRef.beforeNavigateTo.bind(unwrappedActualComponentRef)();
    }
    this.initializedRouteElementReferences[route.path] =  {...existingReference, actualComponentRef: unwrappedActualComponentRef, callBeforeNavigateToOnActualComponent: undefined};
  }

  async performTransition(diffs, defaultPath, routerPath){
    const transitionPromises = [];
    const keysToTransition = [];
    const _this = this;
    this.log('Attempting to perform transition');
    diffs.forEach(
      (diff) => {
        const routeElement = _this.getBestMatchForPath(diff, defaultPath, routerPath);
        if (!routeElement){
          this.log(`Cannot find element ${diff.pathname} to transition`);
          return;
        }
        this.log(`Found element ${routeElement.route.path} to match ${diff.pathname}`);
        keysToTransition.push(diff.pathname);
        if (diff.isAdd){
          routeElement.flaggedForRemove = false;
          transitionPromises.push(_this.onBeforeNavigateTo(routeElement, diff));
        }
        if (diff.isRemove){
          transitionPromises.push(_this.onNavigateAway(routeElement, diff));
        }
      }
    );
    this.log(`Performing transition for ${transitionPromises.length} elements. Keys are  "${keysToTransition.join(',')}" and diff are ${JSON.stringify(diffs.map(c=> c.pathname))}`);
    await Promise.all(transitionPromises);
    return transitionPromises.length;
  }

  getBestMatchForPath(diff){
    const diffPath = diff.pathname;
    const result = this.initializedRouteElementReferences[diffPath];
    if (!result && diff.isAdd){
      const bestMatchKey = Object.keys(this.initializedRouteElementReferences).find( route => matchPath(diffPath, route));
      return this.initializedRouteElementReferences[bestMatchKey];
    }
    return result;
  }

  onBeforeNavigateTo(routeElement = {}, diff){
    const {actualComponentRef = {}, componentRef, route} = routeElement;
    if (!actualComponentRef.navigatedTo ){
      actualComponentRef.navigatedTo = true;
      const {beforeNavigateTo} = actualComponentRef;
      return combine([
        beforeNavigateTo ? beforeNavigateTo.bind(actualComponentRef) : undefined,
        () => this.transition(componentRef, {...diff, route})
      ]);
    }
    return  () => this.transition(componentRef, {...diff, route});
  }

  onNavigateAway(routeElement = {}, diff){
    const {actualComponentRef = {}, componentRef, route} = routeElement;
    if (actualComponentRef.navigatedTo ){
      actualComponentRef.navigatedTo = false;
      const {onNavigateAway} = actualComponentRef;
      return combine([
        onNavigateAway ? onNavigateAway.bind(actualComponentRef) : undefined,
        () => this.transition(componentRef, {...diff, route}, true),
        () => this.removeElement(route.path)
      ]);
    }
    return  () => this.transition(componentRef, {...diff, route}, true);
  }

  async removeElement(path){
    const element = this.initializedRouteElementReferences[path];
    if (!element){
      Logger.warning(`Attempting to remove an element for path ${path} that doesn't exist`);
      return;
    }
    element.flaggedForRemove = true;
    this.updateRouter();
  }

  async transition(componentRef, route){
    const animationType = RouteUtils.getAnimationType(route);
    if (animationType){
      this.log(`Performing animation type ${animationType} for route ${route.path}`);
      componentRef.setNativeProps({
        style: {
          zIndex: 1,
        },
      });
      componentRef.stopAnimation();
      await componentRef.animate(animationType, RouteUtils.getDuration(route));
      componentRef.setNativeProps({
        style: {
          zIndex: route.isRemove ? -1 : route.index
        },
      });
      this.log(`Finished performing animation type ${animationType} for route ${route.path}`);
    }
  }
}

