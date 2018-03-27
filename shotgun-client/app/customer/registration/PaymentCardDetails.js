import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Title} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import {registerCustomer} from 'customer/actions/CustomerActions';
import {ErrorRegion, TermsAgreement, Icon, SpinnerButton} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);

    this.state = {
      valid: false
    };
    this.onCardDetailsChange = this.onCardDetailsChange.bind(this);
  }

  onCardDetailsChange(details){
    if (details.valid == true){
      const {number, expiry, cvc} = details.values;
      const expiryTokens = expiry.split('/');
      this.setState({valid: details.valid, paymentCard: {number, expMonth: expiryTokens[0], expYear: expiryTokens[1], cvc}});
    } else {
      this.setState({valid: details.valid});
    }
  }

  render(){
    const {onCardDetailsChange} = this;
    const {history, busy, errors, user, deliveryAddress, dispatch, valid, paymentCard} = this.props;

    const register = async() => {
      dispatch(registerCustomer(user, deliveryAddress, paymentCard, () => history.push('/Root')));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Payment Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <LiteCreditCardInput autoFocus={true} onChange={(details) => onCardDetailsChange(details)}/>
      </Content>
      <ErrorRegion errors={errors}>
        <SpinnerButton paddedBottom fullWidth iconRight busy={busy} onPress={register} disabled={!valid}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </SpinnerButton>
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

export default withExternalState(mapStateToProps)(PaymentCardDetails);
