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
    const {disabled} = this.props;
    if (disabled){
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

  clearFormattedPriceValue(){
    const {disabled} = this.props;
    if (disabled){
      return;
    }
    this.setState({formattedPrice: undefined});
  }

  onValueChanged(t){
    console.log(t);
    this.setState({price: t});
  }

  render(){
    const {formattedPrice} = this.state;
    const {style = {}, ...rest} = this.props;
    return <TextInput
      keyboardType='phone-pad'
      {...rest}
      value={formattedPrice}
      style={{...style, fontWeight: 'bold', fontSize: 17, paddingTop: 5, paddingBottom: 5}}
      onFocus={this.clearFormattedPriceValue}
      onBlur={this.setFormattedPriceValue}
      onChangeText={this.onValueChanged}
    />;
  }
}

const styles = {
  amountInput: {
    borderBottomWidth: 0,
    paddingLeft: 0,
    fontSize: 18,
    fontWeight: 'bold',
  }
};
