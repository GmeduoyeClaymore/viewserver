import React, {Component} from 'react';
import {PropTypes} from 'prop-types';

export default class ErrorRegion extends Component {
  constructor(){
    super();
  }

  static propTypes = {
    errors: PropTypes.string
  };

  render() {
    const {errors} = this.props;
    return (
      errors ? <div style={{flexDirection: 'column', flex: 1, padding: 0}}>
        <div style={{height: 25}}>
          <span style={{flex: 1, color: 'red', fontSize: 10}}>{errors}</span>
        </div>
        {this.props.children}
      </div> : this.props.children
    );
  }
}

