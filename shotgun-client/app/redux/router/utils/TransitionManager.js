import Logger from 'common/Logger';
import React from 'react';
import * as RouteUtils from './routeUtils';

const combine = async (promises) => {
  const result = [];
  promises.filter(c => c).forEach(
    pr => result.push(pr())
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
    this.initialize = this.initialize.bind(this);
    this.initializeActualComponent = this.initializeActualComponent.bind(this);
  }

  log(message){
    Logger.fine(`TransitionManager-${this.name} - ${message}`);
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

  async performTransition(diffs){
    const transitionPromises = [];
    const keysToTransition = [];
    const _this = this;
    this.log('Attempting to perform transition');
    diffs.forEach(
      (diff) => {
        const routeElement = _this.initializedRouteElementReferences[diff.pathname];
        if (!routeElement){
          this.log(`Cannot find element ${diff.pathname} to transition`);
          return;
        }
        keysToTransition.push(diff.pathname);
        if (diff.isAdd){
          transitionPromises.push(this.onBeforeNavigateTo(routeElement, diff));
        }
        if (diff.isRemove){
          transitionPromises.push(this.onNavigateAway(routeElement, diff));
        }
      }
    );
    this.log(`Performing transition for ${transitionPromises.length} elements. Keys are  "${keysToTransition.join(',')}" and diff are ${JSON.stringify(diffs)}`);
    await Promise.all(transitionPromises);
    return transitionPromises.length;
  }

  onBeforeNavigateTo(routeElement = {}, diff){
    const {actualComponentRef = {}, componentRef, route} = routeElement;
    const {beforeNavigateTo} = actualComponentRef;
    return combine([
      beforeNavigateTo ? beforeNavigateTo.bind(actualComponentRef) : undefined,
      () => this.transition(componentRef, {...diff, route})
    ]);
  }

  onNavigateAway(routeElement = {}){
    const {actualComponentRef = {}, componentRef, route} = routeElement;
    const {onNavigateAway} = actualComponentRef;
    return combine([
      onNavigateAway ? onNavigateAway.bind(actualComponentRef) : undefined,
      () => this.transition(componentRef, route)
    ]);
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
}

