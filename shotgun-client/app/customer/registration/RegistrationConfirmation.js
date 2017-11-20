import React from 'react';
import {connect} from 'react-redux';
import {ScrollView} from 'react-native';
import { Form, Button, Text, Item, Input, Label} from 'native-base';
import { addOrUpdateCustomer, customerServicesRegistrationAction } from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import uuidv4 from 'uuid/v4';
import PrincipalService from 'common/services/PrincipalService';
import {isAnyOperationPending, getOperationError} from 'common/dao';

const RegistrationConfirmation  = ({navigation, dispatch, screenProps, errors}) => {
  const {context} = screenProps;
  const {customer, paymentCard, deliveryAddress} = context.state;

  const register = async () => {
    dispatch(addOrUpdateCustomer(customer, paymentCard, deliveryAddress, () => navigation.navigate('Home', {})));
  };
  const createServicesThenRegister = async () => {
    dispatch(customerServicesRegistrationAction(screenProps.client, uuidv4(), register));
  };

  return <ErrorRegion errors={errors}><ScrollView style={{flex: 1, flexDirection: 'column'}}>
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

    <Button onPress={createServicesThenRegister}>
        <Text>Create Account</Text>
    </Button>
    </Form>
  </ScrollView></ErrorRegion>;
};

RegistrationConfirmation.navigationOptions = {title: 'Registration Confirmation'};

const mapStateToProps = (state, initialProps) => ({
  errors: getOperationError(state, 'customerDao', 'addOrUpdateCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'addOrUpdateCustomer'}),
  ...initialProps
});

export default connect(mapStateToProps)(RegistrationConfirmation);
