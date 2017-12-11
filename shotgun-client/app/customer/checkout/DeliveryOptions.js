import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Picker} from 'react-native';
import {ListItem, Radio, Right, Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';


class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
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

  onChangeValue(field, value) {
    const {context} = this.props;
    const {delivery} = context.state;

    this.props.context.setState({delivery: merge({}, delivery, {[field]: value})});
  }

  render() {
    const {paymentCards, history, context, vehicleTypes} = this.props;
    const {delivery, payment} = context.state;

    return <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Delivery Instructions</Title></Body>
      </Header>
      <Content>
        <Text>What size van?</Text>
        <Picker selectedValue={delivery.vehicleTypeId} onValueChange={(itemValue) => this.onChangeValue('vehicleTypeId', itemValue)}>
          <Picker.Item label="--Select Vehicle Type--"/>
          {vehicleTypes.map(c => <Picker.Item  key={c.vehicleTypeId} label={c.bodyType} value={c.vehicleTypeId} />)}
        </Picker>

        <Text>Where do you want it?</Text>
        <ListItem>
          <Text>Roadside Delivery</Text>
          <Right>
            <Radio selected={delivery.deliveryType == 'ROADSIDE'} onPress={() => this.onChangeValue('deliveryType', 'ROADSIDE')}/>
          </Right>
        </ListItem>
        <ListItem>
          <Text>Carry-in Delivery</Text>
          <Right>
            <Radio selected={delivery.deliveryType == 'CARRYIN'} onPress={() => this.onChangeValue('deliveryType', 'CARRYIN')}/>
          </Right>
        </ListItem>

        <Picker selectedValue={payment.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
          {paymentCards.map(c => <Picker.Item  key={c.paymentId} label={`${c.cardNumber}  ${c.expiryDate}`} value={c.paymentId} />)}
        </Picker>

        <Button onPress={() =>  history.push('/Customer/Checkout/OrderConfirmation')}><Text>Next</Text></Button>
      </Content>
    </Container>;
  }
}

DeliveryOptions.PropTypes = {
  paymentCards: PropTypes.array
};

const mapStateToProps = (state, initialProps) => ({
  paymentCards: getDaoState(state, ['customer', 'paymentCards'], 'paymentCardsDao'),
  vehicleTypes: getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryOptions));


