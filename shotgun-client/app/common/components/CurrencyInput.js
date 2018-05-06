import React, {Component} from 'react';
import {TextInputMask} from 'react-native-masked-text';

export class CurrencyInput extends Component{
  constructor(props){
    super(props);
    this.state = {
      amountMask: undefined
    };
  }

  setAmount = (amountMask) => {
    const {onValueChange} = this.props;
    const amount = (this.refs.amountInput.getRawValue()).toFixed();
    if (onValueChange){
      onValueChange(amount);
    }
    super.setState({amountMask});
  }

  clear = () => {
    super.setState({amountMask: undefined});
  }

  render() {
    const {amountMask} = this.state;
    const {style = {}, disabled, ...rest} = this.props;
    return <TextInputMask ref={'amountInput'} underlineColorAndroid='transparent' style={{...styles.amountInput, ...style}} type={'money'} placeholder='Enter amount'
      options={{ unit: '£', separator: '.', delimiter: ','}} value={amountMask} onChangeText={this.setAmount} customTextInputProps={rest} editable={!disabled} {...rest}/>;
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
