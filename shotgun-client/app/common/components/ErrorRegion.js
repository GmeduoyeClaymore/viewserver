import React, {Component} from 'react';
import {View, Text} from 'react-native';
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
      errors ? <View>
        <View style={{height: 25}}>
          <Text style={{flex: 1, color: 'red', fontSize: 10}}>{errors}</Text>
        </View>
        {this.props.children || null}
      </View> : (this.props.children || null)
    );
  }
}

