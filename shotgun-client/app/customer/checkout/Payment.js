import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Picker} from 'react-native';
import {Container, Content, Header, Title, Body, Left, Button, Icon, Text} from 'native-base';
import {getDaoState} from 'common/dao';
import { withRouter } from 'react-router';

const DEFAULT_PAYMENT_CARDS = [];
class Payment extends Component {
  static propTypes = {
    paymentCards: PropTypes.array
  };

  constructor(props) {
    super(props);
    this.setCard = this.setCard.bind(this);
  }

  componentDidMount(){
    const {paymentCards} = this.props;
    const defaultCard = paymentCards.find(c => c.isDefault) || paymentCards[0];
    if (defaultCard){
      this.setCard(defaultCard.paymentId);
    }
  }

  setCard(paymentId){
    this.props.context.setState({payment: {paymentId}});
  }

  render() {
    const {paymentCards, history, context} = this.props;
    const {paymentId} = context.state.payment;

    return <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Payment</Title></Body>
      </Header>
      <Content>
        <Picker selectedValue={paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
          {paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
        </Picker>
        <Button onPress={() => history.push('/CustomerLanding/Checkout/Delivery')}><Text>Next</Text></Button>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => ({
  paymentCards: getDaoState(state, ['customer', 'paymentCards'], 'paymentCardsDao') || DEFAULT_PAYMENT_CARDS,
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(Payment));
