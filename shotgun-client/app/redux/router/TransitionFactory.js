import Transition from './Transition';
import matchPath from './matchPath';


export default class TransitionFactory{
  constructor(){
    this.deltaRefs = {};
    this.transition = new Transition();
    this.oldHistory = undefined;
  }
  
  initialize(routeDelta, componentRef){
    this.deltaRefs[routeDelta.key] =  {routeDelta, componentRef};
  }
  
  async performTransition(navigationContainer){
    const routes = this.getTransitionDeltas(navigationContainer);
    const transitionPromises = [];
    routes.forEach(
      (route, idx) => {
        const deltaRef = this.deltaRefs[route.key].componentRef;
        //deltaRef.setNativeProps(this.transition.getInitialStyleForRoute(transitionDelta.index));
        transitionPromises.push(this.transition.transition(deltaRef, route));
      }
    );
    const result =  await Promise.all(transitionPromises);
    this.oldnavigationContainer = navigationContainer;
    return result;
  }

  getTransitionDeltas(navigationContainer){
    return this._calculateTransitionDeltas(navigationContainer, this.oldnavigationContainer);
  }

  _calculateTransitionDeltas(navigationContainer, oldnavigationContainer){
    const routes = Object.values(this.deltaRefs).map(rt => rt.routeDelta);
    const orderedRoutes = this.getOrderedRoutes(routes, navigationContainer);
    const previousOrderedRoutes = this.getOrderedRoutes(routes, oldnavigationContainer);
    
    const pl = previousOrderedRoutes[previousOrderedRoutes.length - 1] || {};
    const previousLastElement = orderedRoutes.find(c=> c.key == pl.key) || {};
    const currentLastElement = orderedRoutes[orderedRoutes.length - 1] || {};
    if (currentLastElement.key !== previousLastElement.key){
      previousLastElement.isRemove = true;
      currentLastElement.isAdd = true;
    }
    
    return orderedRoutes;
  }
  
  getOrderedRoutes(routes, navigationContainer = {}){
    const result = [];
    const {navigationStack = []} = navigationContainer;
    navigationStack.forEach(
      el => {
        routes.forEach(
          rt => {
            const match = matchPath(el.pathname, rt);
            if (match){
              result.push({...rt, index: result.length, match});
            }
          }
        );
      }
    );
    return result;
  }
}
