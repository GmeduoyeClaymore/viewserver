import React, {Component} from 'react';
import {Input} from 'native-base';

export const formatPrice = (price) => {
  if (!price || price === 'undefined'){
    return undefined;
  }
  return `£${(parseFloat(price + '')).toFixed(2)}`;
};
export class CurrencyInput extends Component{
  constructor(props){
    super(props);
    this.state = {
      formattedPrice: undefined
    };
  }

  componentDidMount(){
    this.setFormattedPriceValueFromProps(this.props);
  }

  componentWillReceiveProps(newProps){
    this.setFormattedPriceValueFromProps(newProps);
  }

  setFormattedPriceValueFromProps = (newProps) => {
    const {initialPrice} = newProps;
    if (this.props.initialPrice != newProps.initialPrice){
      const formattedPrice = formatPrice(initialPrice / 100);
      this.setState({formattedPrice});
    }
  }

  clear = () => {
    this.clearFormattedPriceValue();
    this.setState({price: undefined});
  }

  setFormattedPriceValue = () => {
    const {disabled} = this.props;
    if (disabled == true){
      return;
    }
    const { onValueChanged } = this.props;
    const {price } = this.state;
    const formattedPrice = formatPrice(price);
    if (onValueChanged){
      onValueChanged(parseFloat(price) * 100);
    }
    this.setState({formattedPrice});
  }

  clearFormattedPriceValue = () => {
    const {disabled} = this.props;
    if (disabled == true){
      return;
    }
    this.setState({formattedPrice: undefined});
  }

  onValueChanged = (t) => {
    this.setState({price: t});
  }

  render(){
    const {formattedPrice} = this.state;
    const {style = {}, ...rest} = this.props;
    return <Input
      keyboardType='phone-pad'
      {...rest}
      value={formattedPrice}
      style={[style, styles.amountInput]}
      onFocus={this.clearFormattedPriceValue}
      onBlur={this.setFormattedPriceValue}
      onChangeText={this.onValueChanged}
    />;
  }
}

const styles = {
  amountInput: {
    fontWeight: 'bold',
    fontSize: 20,
    paddingTop: 10,
    borderBottomWidth: 0,
  }
};
