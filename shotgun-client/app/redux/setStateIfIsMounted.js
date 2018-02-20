export const setStateIfIsMounted = (component) => {
  //if we're debugging then use the redux devtools extension
  let {componentDidMount, componentWillUnmount, setState} = component;
  componentDidMount = componentDidMount.bind(component);
  componentWillUnmount = componentWillUnmount.bind(component);
  setState = setState.bind(component);
  component.componentDidMount = () => {
    component.isMountedComponentMounted = true;
    componentDidMount();
  };


  component.componentWillUnmount = () => {
    component.isMountedComponentMounted = false;
    componentWillUnmount();
  };


  component.setState = (partialState, continuewWith) => {
    if (component.isMountedComponentMounted){
      setState(partialState, continuewWith);
    }
  };
};
