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
    const amount = this.refs.amountInput.getRawValue() * 100;
    if (onValueChange){
      onValueChange(amount);
    }
    super.setState({amountMask});
  }

  render() {
    const {amountMask} = this.state;
    return <TextInputMask ref={'amountInput'} underlineColorAndroid='transparent' style={styles.amountInput} type={'money'} placeholder='Enter amount'
      options={{ unit: '£', separator: '.', delimiter: ','}} value={amountMask} onChangeText={this.setAmount}/>;
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
