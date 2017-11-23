import React from 'react';
import { Form, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import yup from 'yup';
import ValidatingInput from '../../common/components/ValidatingInput';
import ValidatingButton from '../../common/components/ValidatingButton';
import {merge} from 'lodash';

export default PaymentCardDetails  = ({context, history, match}) => {
  const {paymentCard = {}} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({paymentCard: merge(paymentCard, {[field]: value})});
  };

  return <Container>
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Payment Card Details</Title></Body>
    </Header>
    <Content>
      <Form style={{display: 'flex', flex: 1}}>
        <ValidatingInput placeholder="Card Number" value={paymentCard.cardNumber} onChangeText={(value) => onChangeText('cardNumber', value)} validationSchema={PaymentCardDetails.validationSchema.cardNumber} max={16}/>
        <ValidatingInput placeholder="Expiry Month - MM" value={paymentCard.expiryMonth} onChangeText={(value) => onChangeText('expiryMonth', value)}  validationSchema={PaymentCardDetails.validationSchema.expiryMonth}/>
        <ValidatingInput placeholder="Expiry Year - YYYY" value={paymentCard.expiryYear} onChangeText={(value) => onChangeText('expiryYear', value)}  validationSchema={PaymentCardDetails.validationSchema.expiryYear}/>
        <ValidatingInput placeholder="CVV" value={paymentCard.cvv} onChangeText={(value) => onChangeText('cvv', value)} validationSchema={PaymentCardDetails.validationSchema.cvv} maxLength={3}/>
        <ValidatingButton  onPress={() => history.push(`${match.path}/RegistrationConfirmation`)}  validationSchema={yup.object(PaymentCardDetails.validationSchema)} model={paymentCard}>
          <Text>Next</Text>
        </ValidatingButton>
      </Form>
    </Content>
  </Container>;
};

PaymentCardDetails.validationSchema = {
  cardNumber: yup.string().required().max(16).min(16),
  expiryMonth: yup.string().required().max(2).min(2),
  expiryYear: yup.string().required().max(4).min(2),
  cvv: yup.string().required().max(3).min(3)
};
