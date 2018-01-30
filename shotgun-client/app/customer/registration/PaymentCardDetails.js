import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import {registerCustomer} from 'customer/actions/CustomerActions';
import ErrorRegion from 'common/components/ErrorRegion';
import {connect} from 'react-redux';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import TermsAgreement from 'common/components/TermsAgreement';

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
      } else {
        this.setState({valid: details.valid});
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
      <Content keyboardShouldPersistTaps="always">
        <LiteCreditCardInput autoFocus={true} onChange={(details) => onCardDetailsChange(details)}/>
      </Content>
      <ErrorRegion errors={errors}>
        <Button paddedBottom fullWidth iconRight busy={busy} onPress={register} disabled={!valid}>
          <Text uppercase={false}>Continue</Text>
          <Icon name='arrow-forward'/>
        </Button>
      </ErrorRegion>
      <TermsAgreement/>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  errors: getOperationError(state, 'customerDao', 'registerCustomer'),
  busy: isAnyOperationPending(state, [{ customerDao: 'registerCustomer'}])
});

export default connect(mapStateToProps)(PaymentCardDetails);
