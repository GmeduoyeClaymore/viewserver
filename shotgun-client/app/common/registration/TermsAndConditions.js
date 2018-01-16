import React from 'react';
import {Text, Header, Left, Body, Container, Button, Icon, Title, Content} from 'native-base';

export default TermsAndConditions  = ({history}) => {
  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Ts & Cs</Title></Body>
    </Header>
    <Content>
      <Text>
        Shotgun terms and conditions here
      </Text>
      <Text>
        Payment processing services for drivers and customers on Shotgun are provided by Stripe and are subject to the Stripe Connected Account Agreement,
        which includes the Stripe Terms of Service (collectively, the “Stripe Services Agreement”). By agreeing to these terms or continuing to operate as
        a driver or customer on Shotgun, you agree to be bound by the Stripe Services Agreement, as the same may be modified by Stripe from time to time.
        As a condition of Shotgun enabling payment processing services through Stripe, you agree to provide Shotgun accurate and complete information about you and your business,
        and you authorize Shotgun to share it and transaction information related to your use of the payment processing services provided by Stripe.
      </Text>
    </Content>
  </Container>;
};
