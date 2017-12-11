import React, {Component} from 'react';
import { Form, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import PaymentService from 'common/services/PaymentService';

export default class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);

    this.onCardDetailsChange.bind(this);
    this.saveCardDetails.bind(this);

    this.state = {
      valid: false,
    };
  }

  onCardDetailsChange(details){
    if (details.valid == true){
      const {number, expiry, cvc, type} = details.values;
      this.setState({valid: details.valid, paymentCard: {number, expiry, cvc, type}});
    }
  }

  async saveCardDetails(){
    const {history, context, match} = this.props;
    const {number, expiry, cvc, type} = this.state.paymentCard;
    const expiryTokens = expiry.split('/');

    const token = await PaymentService.createCardToken(number,  expiryTokens[0], expiryTokens[1], cvc);

    console.log('TOKEN=' + token);
    context.setState({paymentCard: {
      token,
      type,
      isDefault: true
    }});

    history.push(`${match.path}/RegistrationConfirmation`);
  }

  render(){
    const {valid} = this.state;

    return <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Payment Card Details</Title></Body>
      </Header>
      <Content>
        <Form style={{display: 'flex', flex: 1}}>
          <LiteCreditCardInput autoFocus={true} onChange={(details) => this.onCardDetailsChange(details)}/>
          <Button onPress={() => this.saveCardDetails()} disabled={!valid}>
            <Text>Next</Text>
          </Button>
        </Form>
      </Content>
    </Container>;
  }
}
