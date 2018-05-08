import React, {Component} from 'react';
import {Text} from 'native-base';

export class Currency extends Component{
  render(){
    //TODO - add internationalization to this
    const {value, currency = '£', suffix, decimals = 2, ...props} = this.props;
    const formattedValue = value ? currency + (value / 100).toFixed(decimals) : '';

    return <Text {...props}>{formattedValue + (suffix ? ' ' + suffix : '')}</Text>;
  }
}
