import React, {Component} from 'react';
import {View, Text} from 'react-native';
import {PropTypes} from 'prop-types';


const JAVA_EXCEPTION_STRING = 'java.lang.RuntimeException';

export class ErrorRegion extends Component {
  constructor(){
    super();
  }

  static propTypes = {
    errors: PropTypes.string
  };

  removeJavaStacktrace(errors){
    if (!errors){
      return errors;
    }
    return errors.split('\n').map(this.removeStacktrace).join('\n');
  }

  removeStacktrace(error){
    const index = error.lastIndexOf(JAVA_EXCEPTION_STRING);
    if (!!~index){
      return error.substring(index + JAVA_EXCEPTION_STRING.length);
    }
    return error;
  }

  hasErrors(errors){
    return errors && errors.trim().length > 0;
  }

  render() {
    const {errors} = this.props;
    return (
      this.hasErrors(errors) ? <View>
        <View style={{height: 25}}>
          <Text style={{flex: 1, color: 'red', fontSize: 10}}>{this.removeStacktrace(errors)}</Text>
        </View>
        {this.props.children || null}
      </View> : (this.props.children || null)
    );
  }
}

