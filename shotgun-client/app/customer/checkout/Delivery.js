import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {Slider} from 'react-native';
import {ListItem, Radio, Right, Container, Content, Header, Text, Title, Body, Left, Button, Icon} from 'native-base';
import {merge} from 'lodash';

export default class Delivery extends Component{
  static propTypes = {
    history: PropTypes.object
  };
  
  constructor(props) {
    super(props);
  }

  render() {
    const {history, context} = this.props;
    const {delivery} = context.state;
    const destination = delivery.isDeliveryRequired ? 'DeliveryOptions' : 'OrderConfirmation';

    const onChangeText = async (field, value) => {
      context.setState({delivery: merge(delivery, {[field]: value})});
    };

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
        <ListItem>
          <Text>Store Pickup</Text>
          <Right>
            <Radio selected={!delivery.isDeliveryRequired} onPress={() => onChangeText('isDeliveryRequired', false)}/>
          </Right>
        </ListItem>
        <ListItem>
          <Text>Shotgun Delivery</Text>
          <Right>
            <Radio selected={delivery.isDeliveryRequired} onPress={() => onChangeText('isDeliveryRequired', true)}/>
          </Right>
        </ListItem>

        <Text>{`Required within ${delivery.eta} hours`}</Text>
        <Slider minimumValue={1} maximumValue={72} step={1} value={delivery.eta} onValueChange={val => onChangeText('eta', val)}/>
        <Button onPress={() => history.push(`/CustomerLanding/Checkout/${destination}`)}><Text>Next</Text></Button>
      </Content>
    </Container>;
  }
}
