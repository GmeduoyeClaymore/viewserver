export const setStateIfIsMounted = (component) => {
  //if we're debugging then use the redux devtools extension
  let {componentDidMount, componentWillUnmount, setState} = component;
  if (componentDidMount){
    componentDidMount = componentDidMount.bind(component);
  }
  if (componentWillUnmount){
    componentWillUnmount = componentWillUnmount.bind(component);
  }
  if (setState){
    setState = setState.bind(component);
  }
  component.componentDidMount = () => {
    component.isMountedComponentMounted = true;
    if (componentDidMount){
      componentDidMount();
    }
  };


  component.componentWillUnmount = () => {
    component.isMountedComponentMounted = false;
    if (componentWillUnmount){
      componentWillUnmount();
    }
  };


  component.setState = (partialState, continueWith) => {
    if (component.isMountedComponentMounted){
      if (setState){
        setState(partialState, continueWith);
      }
    }
  };
};
