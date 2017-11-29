import React from 'react';
import {connect} from 'react-redux';
import {ScrollView} from 'react-native';
import { addOrUpdateCustomer, customerServicesRegistrationAction } from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import uuidv4 from 'uuid/v4';
import {Spinner, Form, Text, Button, Item, Label, Input, Content, Header, Left, Body, Container, Icon, Title} from 'native-base';
import {isAnyOperationPending, getOperationError} from 'common/dao';

const RegistrationConfirmation  = ({context, history,  dispatch, client, errors, busy}) => {
  const {user, paymentCard, deliveryAddress} = context.state;

  const register = async () => {
    dispatch(addOrUpdateCustomer(user, paymentCard, deliveryAddress, () => history.push('/Root')));
  };

  const createServicesThenRegister = async () => {
    //TODO - dont like that we have to register stuff here
    dispatch(customerServicesRegistrationAction(client, uuidv4(), register));
  };

  return <Container>
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Confirm Details</Title></Body>
    </Header>
    <Content>
      <ErrorRegion errors={errors}><ScrollView style={{flex: 1, flexDirection: 'column'}}>
        {busy ? <Spinner/> : null}
        <Form>
          <Text>Personal Details</Text>
          <Item fixedLabel>
            <Label>First Name</Label>
            <Input value={user.firstName} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Last Name</Label>
            <Input value={user.lastName} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Email</Label>
            <Input value={user.email} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Phone Number</Label>
            <Input value={user.contactNo} editable={false}/>
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
      </ScrollView>
      </ErrorRegion>
    </Content>
  </Container>;
};

const mapStateToProps = (state, initialProps) => ({
  errors: getOperationError(state, 'customerDao', 'addOrUpdateCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'addOrUpdateCustomer'}),
  ...initialProps
});

export default connect(mapStateToProps)(RegistrationConfirmation);
