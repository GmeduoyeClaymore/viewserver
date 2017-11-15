import React from 'react';
import {connect} from 'react-redux';
import {ScrollView} from 'react-native';
import { Form, Button, Text, Item, Input, Label} from 'native-base';
import CustomerDao from '../data/CustomerDao';
import PaymentCardsDao from '../data/PaymentCardsDao';
import DeliveryAddressDao from '../data/DeliveryAddressDao';

const RegistrationConfirmation  = ({navigation, dispatch, screenProps}) => {
  //TODO - don't like this needs reworking
  const customerDao =  new CustomerDao(screenProps.client, dispatch);
  const paymentCardsDao =  new PaymentCardsDao(screenProps.client, 0, dispatch);
  const deliveryAddressDao =  new DeliveryAddressDao(screenProps.client, 0, dispatch);
  //TODO - need to do this as we need to subscribe before we can send table edits currently
  customerDao.subscribe(0);

  const {context} = screenProps;
  const {customer, paymentCard, deliveryAddress} = context.state;

  const register = async () => {
    const newCustomer = await customerDao.addOrUpdateCustomer(customer);
    await paymentCardsDao.addOrUpdatePaymentCard(newCustomer.customerId, paymentCard);
    await deliveryAddressDao.addOrUpdateDeliveryAddress(newCustomer.customerId, deliveryAddress);
    //navigation.navigate('Home', {});
  };

  return <ScrollView style={{flex: 1, flexDirection: 'column'}}>
    <Form>
    <Text>Personal Details</Text>
    <Item fixedLabel>
      <Label>First Name</Label>
      <Input value={customer.firstName} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Last Name</Label>
      <Input value={customer.lastName} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Email</Label>
      <Input value={customer.email} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Phone Number</Label>
      <Input value={customer.contactNo} editable={false}/>
    </Item>

    <Text>Address Details</Text>
    <Item fixedLabel>
      <Label>Line 1</Label>
      <Input value={deliveryAddress.line1} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Line 2</Label>
      <Input value={deliveryAddress.line2} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>City</Label>
      <Input value={deliveryAddress.city} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Country</Label>
      <Input value={deliveryAddress.country} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Postcode</Label>
      <Input value={deliveryAddress.postcode} editable={false}/>
    </Item>

    <Text>Payment Details</Text>
    <Item fixedLabel>
      <Label>Card Number</Label>
      <Input value={paymentCard.cardNumber} editable={false}/>
    </Item>
    <Item fixedLabel>
      <Label>Expiry</Label>
      <Input value={`${paymentCard.expiryMonth}/${paymentCard.expiryYear}`} editable={false}/>
    </Item>

    <Button onPress={register}>
        <Text>Create Account</Text>
    </Button>
    </Form>
  </ScrollView>;
};

RegistrationConfirmation.navigationOptions = {title: 'Registration Confirmation'};

export default connect(
)(RegistrationConfirmation);
