import React, {Component} from 'react';
import { Form, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import uuidv4 from 'uuid/v4';
import {registerCustomer, loadCustomerRegistrationServices} from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import {connect} from 'react-redux';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);

    this.state = {
      valid: false
    };
  }

  render(){
    const {history, busy, errors, context, dispatch} = this.props;
    const {valid} = this.state;

    const onCardDetailsChange = (details) => {
      if (details.valid == true){
        const {number, expiry, cvc} = details.values;
        const expiryTokens = expiry.split('/');
        this.setState({valid: details.valid, paymentCard: {number, expMonth: expiryTokens[0], expYear: expiryTokens[1], cvc}});
      }
    };

    const register = async() => {
      const {user, deliveryAddress} = context.state;
      dispatch(registerCustomer(user, deliveryAddress, this.state.paymentCard, () => history.push('/Root')));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Payment Details</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}>
          <LiteCreditCardInput autoFocus={true} onChange={(details) => onCardDetailsChange(details)}/>
        </ErrorRegion>
      </Content>
      <Button paddedBottom fullWidth iconRight onPress={register} disabled={!valid || busy}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='arrow-forward'/>
      </Button>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  errors: getOperationError(state, 'customerDao', 'addCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'addCustomer'})
});

export default connect(mapStateToProps)(PaymentCardDetails);
