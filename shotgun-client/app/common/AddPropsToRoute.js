import React, {Component} from 'react';
import Logger from 'common/Logger';
const AddPropsToRoute = (WrappedComponent, passedProps)=>{
  return class PropsForRouteWrapper extends Component{

    componentWillUnmount(){
      Logger.info("Throwing AddPropsToRoute away as for some reason we don't think we need it anymore ");
    }

    static oneOffInitialization(props){
      if (WrappedComponent.oneOffInitialization){
        WrappedComponent.oneOffInitialization(props);
      }
    }
  
    static oneOffDestruction(props){
      if (WrappedComponent.oneOffDestruction){
        WrappedComponent.oneOffDestruction(props);
      }
    }
  
    static beforeNavigateTo(props){
      if (WrappedComponent.beforeNavigateTo){
        WrappedComponent.beforeNavigateTo(props);
      }
    }

    oneOffInitialization(){
      if (this.ref){
        this.ref();
      }
    }
  
    oneOffDestruction(){
      if (this.ref){
        this.ref.oneOffDestruction();
      }
    }
  
    beforeNavigateTo(){
      if (this.ref){
        this.ref.beforeNavigateTo();
      }
    }
  

    render(){
      const props = {...(typeof passedProps === 'function' ? passedProps() : passedProps), ...this.props};
      return <WrappedComponent ref={ref => {this.ref = ref;}} {...props} />;
    }
  };
};

export default AddPropsToRoute;
