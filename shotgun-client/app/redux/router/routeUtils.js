import React, {Children, isValidElement} from 'react';
import matchPath from './matchPath';
import Logger from 'common/Logger';

import invariant  from 'invariant';

export const ensureComponentIsNative = (component) =>  {
  invariant(
    component && typeof component.setNativeProps === 'function',
    'Touchable child must either be native or forward setNativeProps to a ' +
    'native component' + component.setNativeProps
  );
};


const TransitionStrategies = {
  left: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'slideInLeft' : 'slideInRight';
    }
    if (route.isRemove){
      return route.isReverse ? 'slideOutRight' : 'slideOutLeft';
    }
  },
  bottom: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'slideInDown' : 'slideInUp';
    }
    if (route.isRemove){
      return route.isReverse ? 'slideOutUp' : 'slideOutDown';
    }
  }
};

export const getInitialStyleForRoute = (route) => {
  return {
    zIndex: route.index
  };
};
  
export const getAnimationType = (route) => {
  const strategy = TransitionStrategies[route.transition] || TransitionStrategies.left;
  return strategy(route);
};
  
export const getDuration = (route) => {
  return 500;
};

export const checkForOverlap = (routes) => {
  const overlappingRoutes = [];
  routes.forEach(
    rt => {
      routes.forEach(
        rt2 => {
          if (rt != rt2 && matchPath(rt.pathname, rt2)){
            overlappingRoutes.push(`Routes ${rt} and ${rt2} appear to overlap and they must not`);
          }
        });
    }
  );
  if (overlappingRoutes.length){
    throw new Error(`${overlappingRoutes.join('\n')}`);
  }
};
  
export const parseElementIntoRoute = (element) => {
  const { path, exact, strict, sensitive} = element.props;
  return { path, exact, strict, sensitive, key: getKeyFromElement(element)};
};

export const removeDeltas = (navItem) => {
  const {isAdd, isRemove, isReverse, ...rest} = navItem;
  return rest;
};
  
export const getRoutesForChildren = (children) => {
  const result = [];
  Children.forEach(children, (element) => {
    if (!isValidElement(element)) return;
    const route = parseElementIntoRoute(element);
    result.push(route);
  });
  checkForOverlap(result);
  return result;
};
  
export const getKeyFromElement = (element) => {
  const { path} = element.props;
  if (!path){
    throw new Error(`All elements must have a path ${JSON.stringify(Object.keys(element.props))}`);
  }
  return processPath(path);
};
  
export const processPath = path => {
  return path.replace('//', '_');
};
  
export const getOrderedRoutes = (routes, navigationContainer = {}, filterForTransitions, defaultRoute) => {
  const result = [];
  const {navigationStack = []} = navigationContainer;
  const foundKeys = [];
  navigationStack.filter(rt => !filterForTransitions || rt.isAdd || rt.isRemove).forEach(
    el => {
      routes.forEach(
        rt => {
          if (!!~foundKeys.indexOf(rt.key)){
            return;
          }
          const match = matchPath(el.pathname, rt);
          if (match){
            Logger.info(`Matched ${JSON.stringify(rt)} to path ${el.pathname}`);
            foundKeys.push(rt.key);
            const {isAdd, isRemove, index, transition} = match.isExact ? el : {};
            result.push({...removeDeltas(rt), match, index, isAdd, isRemove, transition});
          }
        }
      );
    }
  );
  if (!result.length && defaultRoute){
    return [defaultRoute];
  }
  return result;
};
  

export const parseAction = (action, state) => {
  if (typeof action === 'string'){
    return {
      pathname: action,
      state
    };
  }
  return {
    ...action,
    state
  };
};
  
export const parseRoute = route => {
  if (typeof route === 'string'){
    return {
      path: route,
      key: processPath(route)
    };
  }
  return route;
};
  
