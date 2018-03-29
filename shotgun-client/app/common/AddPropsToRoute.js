import React, {Component} from 'react';
const AddPropsToRoute = (WrappedComponent, passedProps)=>{
  return (
    class Route extends Component{
      shouldComponentUpdate(){
        return false;
      }
      render(){
        const props = Object.assign({}, this.props, passedProps);
        return <WrappedComponent {...props} />;
      }
    }
  );
};

export default AddPropsToRoute;
