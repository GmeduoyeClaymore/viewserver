import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Title, List, ListItem, View, Button} from 'native-base';
import {LiteCreditCardInput} from 'react-native-credit-card-input';
import {ErrorRegion, SpinnerButton, CardIcon, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getOperationErrors, getDaoState, getNavigationProps} from 'common/dao';
import {deletePaymentCard, addPaymentCard} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import PaymentInfo from 'common/settings/PaymentInfo';

class UpdatePaymentCardDetails extends Component {
  onCardDetailsChange = (details) => {
    if (details.valid == true){
      const {number, expiry, cvc, type} = details.values;
      const expiryTokens = expiry.split('/');
      this.setState({valid: details.valid, newPaymentCard: {number, expMonth: expiryTokens[0], expYear: expiryTokens[1], cvc, brand: type}});
    } else {
      this.setState({valid: details.valid});
    }
  }

  deleteCard = (cardId) => {
    const {dispatch} = this.props;
    dispatch(deletePaymentCard(cardId));
  }

  addCard = async() => {
    const {dispatch, newPaymentCard, next, history} = this.props;
    const _this = this;
    dispatch(addPaymentCard(newPaymentCard, () => {
      if (next){
        history.replace(next, undefined, () => _this.clearInput());
      } else {
        _this.clearInput();
      }
    }));
  };

  clearInput(){
    const {ccInput} = this;
    if (ccInput){
      ccInput.setValues({number: undefined, expiry: undefined, cvc: undefined});
    }
  }

  render(){
    const {history, busy, errors, user, valid} = this.props;

    if (!user){
      return null;
    }

    const {paymentCards = [], chargePercentage} = user;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Payment Cards</Title></Body>
      </Header>
      <Content padded keyboardShouldPersistTaps="always">
        <List>
          {paymentCards.length > 0 && paymentCards.map(c => {
            return <ListItem paddedTopBottom key={c.cardId}>
              <CardIcon style={styles.cardIcon} brand={c.brand} />
              <View>
                <Text>{c.brand} ending {c.last4}</Text>
                <Text note>expiry {c.expMonth}/{c.expYear}</Text>
              </View>
              {!c.isDefault && !busy ? <Icon name="bin" right style={styles.trashIcon} onPress={() => this.deleteCard(c.cardId)}/> : null}
            </ListItem>;
          })}
          <ListItem paddedTopBottom>
            <View style={{flex: 1}}>
              <Text style={styles.informationText}>{paymentCards.length > 0 ? 'Add a new card' : 'Add a credit or debit card so we can take payment for completed jobs'}</Text>
              {!paymentCards || !paymentCards.length || paymentCards.length < 3 ? <LiteCreditCardInput ref={c => {this.ccInput = c;}} onChange={(details) => this.onCardDetailsChange(details)}/> : <Text note>You can only add up to 3 payment cards please delete one before adding another</Text> }
            </View>
          </ListItem>
          {paymentCards.length == 0 ?  <ListItem>
            <PaymentInfo chargePercentage={chargePercentage}></PaymentInfo>
          </ListItem> :
            null}
        </List>
      </Content>
      <ErrorRegion errors={errors}/>
      <SpinnerButton paddedBottomLeftRight fullWidth iconRight busy={busy} onPress={this.addCard} disabled={!valid}>
        <Text uppercase={false}>Add new card</Text>
      </SpinnerButton>
    </Container>;
  }
}

const styles = {
  informationText: {
    marginBottom: shotgun.contentPadding
  },
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
  next: getNavigationProps(initialProps).next,
  user: getDaoState(state, ['user'], 'userDao'),
  errors: getOperationErrors(state, [{paymentDao: 'addPaymentCard'}, {paymentDao: 'deletePaymentCard'}]),
  busy: isAnyOperationPending(state, [{paymentDao: 'addPaymentCard'}, {paymentDao: 'deletePaymentCard'}])
});

export default withExternalState(mapStateToProps)(UpdatePaymentCardDetails);
