import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import {Text} from 'react-native';

export class Currency extends Component{
  constructor(){
    super();
  }

  render(){
    //TODO - add internationalization to this
    const {value, currency = 'GBP'} = this.props;
    const formattedValue = (value / 100).toLocaleString(undefined, { style: 'currency', currency });

    return <Text>{formattedValue}</Text>;
  }
}

Currency.PropTypes = {
  value: PropTypes.number.isRequired,
  currency: PropTypes.string
};
