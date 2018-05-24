import React, {Component} from 'react';
import {Text} from 'native-base';

export class Currency extends Component{
  render(){
    //TODO - add internationalization to this
    const {value, currency, suffix, decimals, ...props} = this.props;
    return <Text {...props}>{formatPrice(value, currency, decimals) + (suffix ? ' ' + suffix : '')}</Text>;
  }
}

export const formatPrice = (value, currency = '£', decimals = 2) => {
  return value ? currency + (value / 100).toFixed(decimals) : '';
};


