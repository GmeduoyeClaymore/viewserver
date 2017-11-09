import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, Text, Slider} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';

export default class Delivery extends Component {
  static PropTypes = {
    customerService: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.setIsDeliveryRequired = this.setIsDeliveryRequired.bind(this);
    this.setEta = this.setEta.bind(this);
    this.customerService = this.props.screenProps.customerService;
    this.navigation = this.props.navigation;
    this.state = {
      paymentCards: [],
      order: this.props.navigation.state.params.order,
      delivery: this.props.navigation.state.params.delivery,
      isDeliveryRequired: true
    };
  }

  setIsDeliveryRequired(isDeliveryRequired){
    this.setState({isDeliveryRequired});
  }

  setEta(eta){
    this.setState({delivery: Object.assign({}, this.state.delivery, {eta})});
  }

  render() {
    const navFunc =  () => {
      if (this.state.isDeliveryRequired) {
        this.navigation.navigate('DeliveryOptions', {order: this.state.order, delivery: this.state.delivery});
      } else {
        this.navigation.navigate('OrderConfirmation', {order: this.state.order, delivery: this.state.delivery});
      }
    };

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Delivery Instructions</Text>
      <ListItem>
        <Text>Store Pickup</Text>
        <Right>
          <Radio selected={!this.state.isDeliveryRequired} onPress={() => this.setIsDeliveryRequired(false)}/>
        </Right>
      </ListItem>
      <ListItem>
        <Text>Shotgun Delivery</Text>
        <Right>
          <Radio selected={this.state.isDeliveryRequired} onPress={() => this.setIsDeliveryRequired(true)}/>
        </Right>
      </ListItem>

      <Text>{`Required within ${this.state.delivery.eta} hours`}</Text>
      <Slider minimumValue={1} maximumValue={72} step={1} value={this.state.delivery.eta} onValueChange={val => this.setEta(val)}/>
      <ActionButton buttonText="Next" icon={null} action={navFunc}/>
    </View>;
  }
}

