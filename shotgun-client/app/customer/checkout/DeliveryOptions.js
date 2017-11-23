import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Picker} from 'react-native';
import {ListItem, Radio, Right, Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';

const DEFAULT_DELIVERY_ADDRESSES = [];

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
  }

  componentDidMount(){
    const {deliveryAddresses} = this.props;
    const defaultAddress = deliveryAddresses.find(c => c.isDefault) || deliveryAddresses[0];
    if (defaultAddress){
      this.onChangeValue('deliveryAddressId', defaultAddress.deliveryAddressId);
    }
  }

  onChangeValue(field, value) {
    const {context} = this.props;
    const {delivery} = context.state;

    const delivery2 = merge(delivery, {[field]: value});
    this.props.context.setState({delivery: delivery2});
  }

  render() {
    const {deliveryAddresses, history, context} = this.props;
    const {delivery} = context.state;

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

        <Text>Delivery Address</Text>
        <Picker selectedValue={delivery.deliveryAddressId} onValueChange={(itemValue) => this.onChangeValue('deliveryAddressId', itemValue)}>
          {deliveryAddresses.map(a => <Picker.Item  key={a.deliveryAddressId} label={a.line1} value={a.deliveryAddressId} />)}
        </Picker>
        <Button onPress={() =>  history.push('/CustomerLanding/Checkout/OrderConfirmation')}><Text>Next</Text></Button>
      </Content>
    </Container>;
  }
}

DeliveryOptions.PropTypes = {
  deliveryAddresses: PropTypes.array
};

const mapStateToProps = (state, initialProps) => ({
  deliveryAddresses: getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao') || DEFAULT_DELIVERY_ADDRESSES,
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DeliveryOptions));


