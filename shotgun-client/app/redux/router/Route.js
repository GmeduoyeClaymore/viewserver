'use strict';
import React, {Children, Component} from 'react';
import PropTypes from 'prop-types';

const isEmptyChildren = (children) => {
  return Children.count(children) === 0;
};
/**
 * The public API for matching a single path and rendering.
 */
export class Route extends Component{
    static propTypes = {
      computedMatch: PropTypes.object, // private, from <Switch>
      path: PropTypes.string,
      exact: PropTypes.bool,
      strict: PropTypes.bool,
      sensitive: PropTypes.bool,
      component: PropTypes.func,
      render: PropTypes.func,
      children: PropTypes.oneOfType([PropTypes.func, PropTypes.node]),
      location: PropTypes.object
    };

    oneOffInitialization(){
      const {props} = this;
      const {component} = props;
      component.oneOffInitialization(props);
    }

    oneOffDestruction(){
      const {props} = this;
      const {component} = props;
      component.oneOffDestruction(props);
    }

    beforeNavigateTo(){
      const {props} = this;
      const {component} = props;
      component.beforeNavigateTo(props);
    }

    constructor(props){
      super(props);
    }

    render() {
      const {children, component, render, ...rest} = this.props;
      return component ? // component prop gets first priority, only called if there's a match
      React.createElement(component, rest) : render ? // render prop is next, only called if there's a match
      render(rest)  : children ? // children come last, always called
      typeof children === 'function' ? children(props) : !isEmptyChildren(children) ? Children.only(children) : null : null;
    }
}

export default Route;
