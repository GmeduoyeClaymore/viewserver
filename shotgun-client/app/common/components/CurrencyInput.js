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
    const amount = (this.inputMask.getRawValue() * 100).toFixed();
    if (onValueChange){
      onValueChange(amount);
    }
    super.setState({amountMask});
  }

  componentWillReceiveProps(props){
    if (this.props.value != props.value && this.inputMask){
      super.setState({amountMask: props.value / 100});
    }
  }

  clear = () => {
    super.setState({amountMask: undefined});
  }

  render() {
    const {amountMask} = this.state;
    const {style = {}, disabled, ...rest} = this.props;
    return <TextInputMask ref={ref => {this.inputMask = ref;}} underlineColorAndroid='transparent' style={{...styles.amountInput, ...style}} type={'money'} placeholder='Enter amount'
      options={{ unit: '£', separator: '.', delimiter: ','}}  onChangeText={this.setAmount} customTextInputProps={rest} editable={!disabled} {...rest} value={amountMask}/>;
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
