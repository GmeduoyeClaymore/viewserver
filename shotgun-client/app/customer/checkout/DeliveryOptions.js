import React, {Component} from 'react';
import {Switch} from 'react-native';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Picker, Slider} from 'react-native';
import {Icon, Button, Container, Form, Label, Item, Header, Text, Title, Body, Left} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import {getPaymentCards} from 'customer/actions/CustomerActions';
import {isAnyOperationPending} from 'common/dao';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.setRequireHelp = this.setRequireHelp.bind(this);
    this.state = {
      requireHelp: false
    };
  }

  async componentDidMount(){
    const {stripeDefaultPaymentSource} = this.props.user;
    const defaultCard = this.props.paymentCards.find(c => c.id == stripeDefaultPaymentSource) || paymentCards[0];
    if (defaultCard){
      this.setCard(defaultCard.id);
    }
  }

  setCard(paymentId){
    this.props.context.setState({payment: {paymentId}});
  }

  setRequireHelp(requireHelp){
    this.setState({requireHelp});
    this.onChangeValue('noRequiredForOffload', 0);
  }

  onChangeValue(field, value) {
    const {context} = this.props;
    const {delivery} = context.state;

    this.props.context.setState({delivery: merge({}, delivery, {[field]: value})});
  }

  render() {
    const {history, context, vehicleTypes, paymentCards = []} = this.props;
    const {delivery, payment} = context.state;
    const {requireHelp} = this.state;

    return <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Delivery Instructions</Title></Body>
      </Header>
      <Form>
        <Text>What size van?</Text>
        <Picker selectedValue={delivery.vehicleTypeId} onValueChange={(itemValue) => this.onChangeValue('vehicleTypeId', itemValue)}>
          <Picker.Item label="--Select Vehicle Type--"/>
          {vehicleTypes.map(c => <Picker.Item  key={c.vehicleTypeId} label={c.bodyType} value={c.vehicleTypeId} />)}
        </Picker>

        <Text>{`Required within ${delivery.eta} hours`}</Text>
        <Slider minimumValue={1} maximumValue={72} step={1} value={delivery.eta} onValueChange={val => this.onChangeValue('eta', val)}/>

        <Item fixedLabel>
          <Label>Do you require help with this item</Label>
          <Switch onValueChange={this.setRequireHelp} value={requireHelp}/>
        </Item>
        {requireHelp ? <Item fixedLabel>
          <Label>How many people</Label>
          <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 1)} >
            <Text>1</Text>
            <Icon name='man' style={styles.manIcon}/>
          </Button>
          <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 2)} >
            <Text>2</Text>
            <Icon name='man' style={styles.manIcon}/>
            <Icon name='man' style={styles.manIcon}/>
          </Button>
          <Button transparent onPress={() => this.onChangeValue('noRequiredForOffload', 3)} >
            <Text>3</Text>
            <Icon name='man' style={styles.manIcon}/>
            <Icon name='man' style={styles.manIcon}/>
            <Icon name='man' style={styles.manIcon}/>
          </Button>
        </Item> : null}

        <Text>Pay with card</Text>
        <Picker selectedValue={payment.paymentId} onValueChange={(itemValue) => this.setCard(itemValue)}>
          {paymentCards.map(c => <Picker.Item key={c.paymentId} label={`${c.number}  ${c.expiry}`} value={c.paymentId} />)}
        </Picker>
        <Button onPress={() =>  history.push('/Customer/Checkout/OrderConfirmation')}><Text>Next</Text></Button>
      </Form>
    </Container>;
  }
}

DeliveryOptions.PropTypes = {
  paymentCards: PropTypes.array,
  user: PropTypes.object
};

const styles = {
  manIcon: {
    marginLeft: 0,
    marginRight: 0,
    paddingLeft: 0,
    paddingRight: 0
  }
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  getPaymentCards(user.stripeCustomerId)

  return {
    busy: isAnyOperationPending(state, { paymentDao: 'getCustomerPaymentCards'}),
    paymentCards: getDaoState(state, ['paymentCards'], 'paymentDao'),
    vehicleTypes: getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao'),
    user,
    ...initialProps
  };
};

export default withRouter(connect(
  mapStateToProps
)(DeliveryOptions));


