import React, {Component} from 'react';
import { Form, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import uuidv4 from 'uuid/v4';
import {addCustomer, loadCustomerRegistrationServices} from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import {connect} from 'react-redux';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);
    this.onCardDetailsChange.bind(this);

    this.state = {
      valid: false
    };
  }

  onCardDetailsChange(details){
    if (details.valid == true){
      const {number, expiry, cvc} = details.values;
      const expiryTokens = expiry.split('/');
      this.setState({valid: details.valid, paymentCard: {number, expMonth: expiryTokens[0], expYear: expiryTokens[1], cvc}});
    }
  }

  render(){
    const {history, busy, errors, context, dispatch, client} = this.props;
    const {valid} = this.state;

    const saveCardDetails = async() => {
      //TODO - set busy when calling stripe
      await createServicesThenRegister();
    };

    const register = async() => {
      const {user, deliveryAddress} = context.state;
      dispatch(addCustomer(user, deliveryAddress, this.state.paymentCard, () => history.push('/Root')));
    };

    const createServicesThenRegister = async() => {
      //TODO - dont like that we have to register stuff here
      dispatch(loadCustomerRegistrationServices(client, uuidv4(), register));
    };

    return <Container>
      <Header>
        <Left>
          <Button>
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
  errors: getOperationError(state, 'customerDao', 'addCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'addCustomer'}),
  ...initialProps
});

export default connect(mapStateToProps)(PaymentCardDetails);
