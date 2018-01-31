import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Title, List, ListItem, View, Button} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import ErrorRegion from 'common/components/ErrorRegion';
import SpinnerButton from 'common/components/SpinnerButton';
import {connect} from 'react-redux';
import {isAnyOperationPending, getOperationErrors, getDaoState} from 'common/dao';
import CardIcon from 'common/components/CardIcon';
import {deletePaymentCard, addPaymentCard} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon} from 'common/components/Icon';

class PaymentCardDetails extends Component {
  constructor(props) {
    super(props);

    this.state = {
      valid: false
    };
  }

  render(){
    const {history, busy, errors, dispatch, user, paymentCards} = this.props;
    const {valid, newPaymentCard} = this.state;

    const onCardDetailsChange = (details) => {
      if (details.valid == true){
        const {number, expiry, cvc} = details.values;
        const expiryTokens = expiry.split('/');
        this.setState({valid: details.valid, newPaymentCard: {number, expMonth: expiryTokens[0], expYear: expiryTokens[1], cvc}});
      } else {
        this.setState({valid: details.valid});
      }
    };

    const deleteCard = (cardId) => {
      dispatch(deletePaymentCard(user.stripeCustomerId, cardId));
    };

    const addCard = async() => {
      dispatch(addPaymentCard(user.stripeCustomerId, newPaymentCard, () => this.ccInput.setValues({ number: undefined,  expiry: undefined, cvc: undefined})));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Payment Cards</Title></Body>
      </Header>
      <Content padded keyboardShouldPersistTaps="always">
        <List>
          {paymentCards.map(c => {
            return <ListItem paddedTopBottom key={c.id}>
              <CardIcon style={styles.cardIcon} brand={c.brand} />
              <View>
                <Text>{c.brand} ending {c.last4}</Text>
                <Text note>expiry {c.expMonth}/{c.expYear}</Text>
              </View>
              {c.id !== user.stripeDefaultSourceId && !busy ? <Icon name="trash" right style={styles.trashIcon} onPress={() => deleteCard(c.id)}/> : null}
            </ListItem>;
          })}
          <ListItem paddedTopBottom>
            <View style={{flex: 1}}>
              <Text>Add a new card</Text>
              {paymentCards.length < 3 ? <LiteCreditCardInput ref={c => {this.ccInput = c;}} autoFocus={true} onChange={(details) => onCardDetailsChange(details)}/> : <Text note>You can only add up to 3 payment cards please delete one before adding another</Text> }
            </View>
          </ListItem>
        </List>
      </Content>
      <ErrorRegion errors={errors}>
        <SpinnerButton paddedBottom fullWidth iconRight busy={busy} onPress={addCard} disabled={!valid}>
          <Text uppercase={false}>Add new card</Text>
        </SpinnerButton>
      </ErrorRegion>
    </Container>;
  }
}

const styles = {
  cardIcon: {
    width: 25
  },
  trashIcon: {
    fontSize: 35,
    color: shotgun.brandDanger
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  paymentCards: getDaoState(state, ['paymentCards'], 'paymentDao'),
  user: getDaoState(state, ['user'], 'userDao'),
  errors: getOperationErrors(state, [{paymentDao: 'addPaymentCard'}, {paymentDao: 'deletePaymentCard'}, {paymentDao: 'getPaymentCards'}]),
  busy: isAnyOperationPending(state, [{paymentDao: 'addPaymentCard'}, {paymentDao: 'deletePaymentCard'}, {paymentDao: 'getPaymentCards'}])
});

export default connect(mapStateToProps)(PaymentCardDetails);
