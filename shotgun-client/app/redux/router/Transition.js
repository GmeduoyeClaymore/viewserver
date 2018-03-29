export default class Transition{
  constructor(){
  }
  
  async transition(componentRef, route){
    const animationType = this.getAnimationType(route);
    if (animationType){
      await componentRef.animate(animationType, this.getDuration(route));
    }
  }
  
  getInitialStyleForRoute(route){
    return {
      zIndex: 0//route.index
    };
  }
  
  getAnimationType(route){
    if (route.isAdd){
      return 'slideInRight';
    }
    if (route.isRemove){
      return 'slideOutRight';
    }
  }
  
  getDuration(route){
    return 300;
  }
}
  
