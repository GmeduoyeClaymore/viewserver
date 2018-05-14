import React, {Component} from 'react';
import {View, Text,  Item, Label} from 'native-base';
import {ValidatingInput, ValidatingButton, CurrencyInput} from 'common/components';
import {addPaymentStage} from 'customer/actions/CustomerActions';
import yup from 'yup';

export default class AddPaymentStageControl extends Component{
  constructor(props){
    super(props);
    this.state = {
      amount: undefined,
      name: undefined,
      description: undefined
    };
  }

  resetState = () => {
    this.setState({
      amount: undefined,
      name: undefined,
      description: undefined,
      reset: true
    });

    if (this.nameInput){
      this.nameInput.clearTouched();
    }
    if (this.descriptionInput){
      this.descriptionInput.clearTouched();
    }
    if (this.amountInput){
      this.amountInput.clearTouched();
    }
  }

  onAddPaymentStage = () => {
    const {order, dispatch, paymentStageType} = this.props;
    const {orderId, orderContentTypeId} = order;
    const {name, amount, description} = this.state;

    dispatch(addPaymentStage({orderId, orderContentTypeId, amount, name, description, paymentStageType}, this.resetState));
  };

  render(){
    const {paymentStageType, order, busyUpdating} = this.props;
    const validationSchemaFactory = paymentStageType === 'Percentage' ? percentageValidationSchema : fixedPriceValidationSchema;
    const validationSchema = validationSchemaFactory(order.amount);

    return <View style={{height: 300}}>
      <Item stackedLabel style={styles.item} >
        <Label>Name</Label>
        <ValidatingInput ref={rf => {this.nameInput = rf;}} bold style={{padding: 10}} padded value={this.state.name}placeholder="First fix electrics" onChangeText={(name) => this.setState({name})} validationSchema={validationSchema.name} maxLength={30}/>
      </Item>

      <Item stackedLabel style={styles.item} >
        <Label>Description</Label>
        <ValidatingInput ref={rf => {this.descriptionInput = rf;}} bold style={{padding: 10}} padded value={this.state.description} placeholder="Complete all first fix of bathroom" onChangeText={(description) => this.setState({description})} validationSchema={validationSchema.description} maxLength={150}/>
      </Item>

      <Item stackedLabel last style={styles.item} >
        <Label>{paymentStageType === 'Percentage' ?  'Percentage' : 'Amount'}</Label>
        <ValidatingInput control={paymentStageType === 'Percentage' ?  undefined : CurrencyInput} ref={rf => {this.amountInput = rf;}} bold style={{padding: 10}} value={this.state.amount} initialPrice={this.state.amount} placeholder={paymentStageType === 'Percentage' ?  '25' : '£2500.00'} onValueChanged={(amount) => this.setState({amount})} onChangeText={(amount) => this.setState({amount})} validationSchema={validationSchema.amount} maxLength={10}/>
      </Item>

      <ValidatingButton busy={busyUpdating} fullWidth success onPress={this.onAddPaymentStage} validationSchema={yup.object(validationSchema)} model={this.state}>
        <Text uppercase={false}>Add</Text>
      </ValidatingButton>
    </View>;
  }
}

const styles = {
  item: {
    marginLeft: 4,
    marginRight: 10,
    marginBottom: 10}
};

const validationSchemaBase = {
  name: yup.string().required().max(30),
  description: yup.string().required().max(150),
};

const percentageValidationSchema = () => ({
  ...validationSchemaBase,
  amount: yup.number().required().min(0).max(100)
});

const fixedPriceValidationSchema = orderAmount => ({
  ...validationSchemaBase,
  amount: yup.number().required().min(0).max(orderAmount)
});
