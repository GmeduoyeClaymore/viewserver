import React, {Component} from 'react';
import {PropTypes} from 'prop-types';

export default class Item extends Component {
  constructor(){
    super();
  }

  static propTypes = {
    error: PropTypes.bool,
    success: PropTypes.bool
  };

  render() {
    const {error,success} = this.props;
    const border  = (!success && !error) ? '' : error ? '1px solid red' : '1px solid green';
    return (
     <div style={{display : 'flex', flexDirection: 'row', flex: 1, padding: 0, border}}>
        {this.props.children}
        {error ? <span style={{flex: 1, color: 'red', fontSize: 10}}>{error}</span>: null}
      </div>
    );
  }
}

