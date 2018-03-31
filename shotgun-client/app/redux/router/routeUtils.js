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
  },
  flip: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'flipInX' : 'flipInY';
    }
    if (route.isRemove){
      return route.isReverse ? 'flipOutY' : 'flipOutX';
    }
  },
  zoom: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'zoomInLeft' : 'zoomInRight';
    }
    if (route.isRemove){
      return route.isReverse ? 'zoomOutRight' : 'zoomOutLeft';
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
      if (!rt.path){
        overlappingRoutes.push(`Route ${JSON.stringify(rt)} must have a path specified`);
      } else {
        routes.forEach(
          rt2 => {
            if (rt != rt2 && matchPath(rt.path, rt2)){
              overlappingRoutes.push(`Routes ${JSON.stringify(rt)} and ${JSON.stringify(rt2)} appear to overlap and they must not`);
            }
          });
      }
    }
  );
  if (overlappingRoutes.length){
    throw new Error(`${overlappingRoutes.join('\n')}`);
  }
};
  
export const parseElementIntoRoute = (element) => {
  const { path, exact, strict, sensitive, persistent} = element.props;
  return { path, exact, strict, sensitive, persistent, key: getKeyFromElement(element)};
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

export const getLifeCycleForChildren = (children) => {
  const result = {};
  Children.forEach(children, (element) => {
    if (!isValidElement(element)) return;
    const {component, path} = element.props;
    if (!component){
      throw new Error(`Element "${element.props.path}" does not have a component`);
    }
    result[path] = {
      path,
      oneOffInitialization: component.oneOffInitialization,
      oneOffDestruction: component.oneOffDestruction,
      beforeNavigateTo: component.beforeNavigateTo
    };
  });
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
  let foundVisibleRoot = false;
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
            const {isAdd, isRemove, index, transition, isReverse} = match.isExact ? el : {};
            foundVisibleRoot = true;
            result.push({...removeDeltas(rt), match, index, isAdd, isReverse, isRemove, transition, persistent: false});
          } else if (rt.persistent){
            Logger.info(`Loading persistent route ${JSON.stringify(rt)} to path ${el.pathname}`);
            foundKeys.push(rt.key);
            result.unshift({...removeDeltas(rt), match, index: -1, persistent: true});
          }
        }
      );
    }
  );
  if (!foundVisibleRoot && defaultRoute){
    const navigationStackLocation = navigationStack[navigationStack.length - 1];
    return [{...defaultRoute, pathname: navigationStackLocation.pathname}]; // Change default routes path so it has the url of the referring page
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
  
