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
        errors ? <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
            <View style={{height: 25}}>
              <Text style={{flex: 1, color: 'red', fontSize: 10}}>{errors}</Text>
            </View>
            {this.props.children}
        </View> : this.props.children
    );
  }
}

