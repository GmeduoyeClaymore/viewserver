import React, {Component} from 'react';
import {Input} from 'native-base';
import {debounce} from 'lodash';
import {Keyboard} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';

export const formatPrice = (price) => {
  if (!price || price === 'undefined'){
    return undefined;
  }
  const priceAsFloat = parseFloat(price + '');
  if (isNaN(priceAsFloat)){
    return undefined;
  }
  return `£${(priceAsFloat).toFixed(2)}`;
};


export class CurrencyInput extends Component{
  constructor(props){
    super(props);
    this.state = {
      formattedPrice: undefined
    };
    this.onUserDormantInControl = debounce(this.onUserDormantInControl, 1000);
  }

  onUserDormantInControl = () => {
    this.setFormattedPriceValue();
    Keyboard.dismiss();
  }

  componentDidMount(){
    const {initialPrice} = this.props;
    const formattedPrice = formatPrice(initialPrice / 100);
    this.setState({formattedPrice});
  }

  componentWillReceiveProps(newProps){
    this.setFormattedPriceValueFromProps(newProps);
  }

  setFormattedPriceValueFromProps = (newProps) => {
    const {initialPrice} = newProps;
    if (this.props.initialPrice != initialPrice){
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
    this.setState({formattedPrice: undefined});
  }

  onValueChanged = (t) => {
    this.setState({price: t});
    this.onUserDormantInControl();
  }

  render(){
    const {formattedPrice} = this.state;
    const {style = {}, placeholder, editable = true} = this.props;
    return <Input
      keyboardType='phone-pad'
      placeholder={placeholder}
      editable={editable}
      placeholderTextColor={shotgun.silver}
      value={formattedPrice}
      style={[styles.amountInput, style]}
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
