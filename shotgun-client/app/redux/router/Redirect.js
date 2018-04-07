import React, {Component} from 'react';
import warning from 'warning';
import { createLocation, locationsAreEqual } from 'history';

/**
 * The public API for updating the location programmatically
 * with a component.
 */


export class Redirect extends Component{
  constructor(props){
    super(props);
  }


  componentDidMount(){
    this.perform();
  }

  componentDidUpdate(prevProps) {
    const prevTo = createLocation(prevProps.to);
    const nextTo = createLocation(this.props.to);
    if (locationsAreEqual(prevTo, nextTo)) {
      warning(false, 'You tried to redirect to the same route you\'re currently on: ' + ('"' + nextTo.pathname + nextTo.search + '"'));
      return;
    }
    this.perform();
  }

  perform() {
    const {history, push, just, to} = this.props;
    if (push) {
      history.push(to);
    } else if (just) {
      history.just(to);
    } else {
      history.replace(to);
    }
  }
  render() {
    return null;
  }
}


export default Redirect;
