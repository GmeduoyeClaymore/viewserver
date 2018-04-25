import React, {Component} from 'react';
import {Text} from 'native-base';

export class Currency extends Component{
  render(){
    //TODO - add internationalization to this
    const {value, currency = 'GBP', ...props} = this.props;
    const formattedValue = value ? (value / 100).toLocaleString(undefined, { style: 'currency', currency }) : '';

    return <Text {...props}>{formattedValue}</Text>;
  }
}
