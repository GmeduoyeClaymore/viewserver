'use strict';
import React, {Children, PureComponent} from 'react';
import PropTypes from 'prop-types';

const isEmptyChildren = (children) => {
  return Children.count(children) === 0;
};
/**
 * The public API for matching a single path and rendering.
 */
export class Route extends PureComponent{
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

    constructor(props){
      super(props);
    }

    render() {
      const {children, component, render} = this.props;
      const {history} = this.props;

      const location = this.props.location;
      const props = { location, history };

      return component ? // component prop gets first priority, only called if there's a match
      React.createElement(component, props) : render ? // render prop is next, only called if there's a match
      render(props)  : children ? // children come last, always called
      typeof children === 'function' ? children(props) : !isEmptyChildren(children) ? Children.only(children) : null : null;
    }
}

export default Route;
