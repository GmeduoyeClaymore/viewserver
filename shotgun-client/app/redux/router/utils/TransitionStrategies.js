const TransitionStrategies = {
  left: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'slideInLeft' : 'slideInRight';
    }
    if (route.isRemove){
      return route.isReverse ? 'slideOutRight' : 'slideOutLeft';
    }
  },
  right: (route) => {
    if (route.isAdd){
      return route.isReverse ? 'slideInRight' : 'slideInLeft';
    }
    if (route.isRemove){
      return route.isReverse ? 'slideOutLeft' : 'slideOutRight';
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
  },
  immediate: () => {
  }
};

export default TransitionStrategies;