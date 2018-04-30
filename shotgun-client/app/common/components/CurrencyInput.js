import React, {Component} from 'react';
import { TextInput } from 'react-native';

export const formatPrice = (price) => {
  if (!price || price === 'undefined'){
    return undefined;
  }
  return `£${(parseFloat(price + '')).toFixed(2)}`;
};

export class CurrencyInput extends Component{
  constructor(props){
    super(props);

    this.setFormattedPriceValue = this.setFormattedPriceValue.bind(this);
    this.clearFormattedPriceValue = this.clearFormattedPriceValue.bind(this);
    this.setFormattedPriceValueFromProps = this.setFormattedPriceValueFromProps.bind(this);
    this.onValueChanged = this.onValueChanged.bind(this);
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
  setFormattedPriceValueFromProps(newProps){
    const {initialPrice} = newProps;
    const formattedPrice = formatPrice(initialPrice / 100);
    this.setState({formattedPrice});
  }

  setFormattedPriceValue(){
    const { onValueChanged } = this.props;
    const {price } = this.state;
    const formattedPrice = formatPrice(price);
    if (onValueChanged){
      onValueChanged(parseFloat(price) * 100);
    }
    this.setState({formattedPrice});
  }

  clearFormattedPriceValue(){
    this.setState({formattedPrice: undefined});
  }

  onValueChanged(t){
    console.log(t);
    this.setState({price: t});
  }

  render(){
    const {formattedPrice} = this.state;
    return <TextInput
      keyboardType='phone-pad'
      {...this.props}
      value={formattedPrice}
      onFocus={this.clearFormattedPriceValue}
      onBlur={this.setFormattedPriceValue}
      onChangeText={this.onValueChanged}
    />;
  }
}
