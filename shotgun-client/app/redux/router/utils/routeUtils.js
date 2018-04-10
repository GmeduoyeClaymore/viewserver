import React, {Children, isValidElement} from 'react';
import matchPath from './matchPath';
import TransitionStrategies from './TransitionStrategies';
import invariant  from 'invariant';

export const ensureComponentIsNative = (component) =>  {
  invariant(
    component && typeof component.setNativeProps === 'function',
    'Touchable child must either be native or forward setNativeProps to a ' +
    'native component' + component.setNativeProps
  );
};

export const getInitialStyleForRoute = (route) => {
  return {
    zIndex: route.index
  };
};
  
export const getAnimationType = (route) => {
  const strategy = TransitionStrategies[route.transition] || TransitionStrategies.immediate;
  return strategy == undefined ? TransitionStrategies.immediate : strategy(route);
};
  
export const getDuration = () => {
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

export const combinePaths = (basePath, extension) => {
  return `${basePath.startsWith('/') ? '' : '/'}${basePath}${basePath.endsWith('/')  || extension.startsWith('/') ? '' : '/'}${extension}`;
};
  
export const parseElementIntoRoute = (element, routerPath) => {
  invariant(routerPath, 'Router path is required');
  invariant(element, 'element is required');
  const { path, exact, strict, sensitive, persistent, transition, ...componentProps} = element.props;
  return { path: combinePaths(routerPath, path), exact, strict, sensitive, persistent, transition, componentProps};
};
  
export const getRoutesForChildren = (children, routerPath) => {
  const result = [];
  Children.forEach(children, (element) => {
    if (!isValidElement(element)) return;
    const route = parseElementIntoRoute(element, routerPath);
    result.push(route);
  });
  checkForOverlap(result);
  return result;
};

export const parseAction = (action, state) => {
  if (!action){
    return;
  }
  if (typeof action === 'string'){
    return {
      pathname: action,
      state
    };
  }
  return {
    ...action,
    state: state == undefined ? action.state : state
  };
};

export const parseRoute = (routerPath, route, state) => {
  invariant(routerPath, 'Router path is required');
  invariant(route, 'route is required');
  if (typeof route === 'string'){
    return {
      pathname: combinePaths(routerPath, route),
      state,
      transition: 'immediate'
    };
  }
  invariant(route.pathname, 'route.pathname is required');
  return {
    ...route,
    transition: route.transition || 'immediate',
    pathname: combinePaths(routerPath, route.pathname),
    state: state == undefined ? route.state : state
  };
};
