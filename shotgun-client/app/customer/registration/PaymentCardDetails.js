import React, {Component} from 'react';
import { Form, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import PaymentService from 'common/services/PaymentService';
import {merge} from 'lodash';
import uuidv4 from 'uuid/v4';
import {addOrUpdateCustomer, loadCustomerRegistrationServices} from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import {connect} from 'react-redux';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);
    this.onCardDetailsChange.bind(this);

    this.state = {
      valid: false,
      paymentCard: {
        number: undefined,
        expiry: undefined,
        cvc: undefined
      }
    };
  }

  onCardDetailsChange(details){
    if (details.valid == true){
      const {number, expiry, cvc, type} = details.values;
      this.setState({valid: details.valid, paymentCard: {number, expiry, cvc, type}});
    }
  }

  render(){
    const {history, busy, errors, context, dispatch, client} = this.props;
    const {number, expiry, cvc} = this.state.paymentCard;
    const {valid} = this.state;

    const saveCardDetails = async() => {
      const {user} = context.state;
      const expiryTokens = expiry.split('/');
      const cardToken = await PaymentService.createCardToken(number,  expiryTokens[0], expiryTokens[1], cvc);
      const stripeCustomerId = await PaymentService.createCustomer(cardToken, user.email);
      context.setState({user: merge({}, user, {stripeCustomerId, stripeDefaultPaymentSource: cardToken})});

      //TODO - set busy when calling stripe
      //TODO - error handling for stripe card and customer creation
      await createServicesThenRegister();
    };

    const register = async() => {
      const {user, deliveryAddress} = context.state;
      dispatch(addOrUpdateCustomer(user, deliveryAddress, () => history.push('/Root')));
    };

    const createServicesThenRegister = async() => {
      //TODO - dont like that we have to register stuff here
      dispatch(loadCustomerRegistrationServices(client, uuidv4(), register));
    };

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
        <ErrorRegion errors={errors}>
          <Form style={{display: 'flex', flex: 1}}>
            <LiteCreditCardInput autoFocus={true} onChange={(details) => this.onCardDetailsChange(details)}/>
            <Button onPress={() => saveCardDetails()} disabled={!valid || busy}>
              <Text>Next</Text>
            </Button>
          </Form>
        </ErrorRegion>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  errors: getOperationError(state, 'customerDao', 'addOrUpdateCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'addOrUpdateCustomer'}),
  ...initialProps
});

export default connect(mapStateToProps)(PaymentCardDetails);
