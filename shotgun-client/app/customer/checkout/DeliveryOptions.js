import React, {Component, PropTypes} from 'react';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';

export default class DeliveryOptions extends Component {
  static PropTypes = {
    customerService: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.updateDeliveryAddresses = this.updateDeliveryAddresses.bind(this);
    this.setDeliveryType = this.setDeliveryType.bind(this);
    this.setDeliveryAddress = this.setDeliveryAddress.bind(this);
    this.customerService = this.props.screenProps.customerService;
    this.navigation = this.props.navigation;
    this.state = {
      deliveryAddresses: [],
      order: this.props.navigation.state.params.order,
      delivery: this.props.navigation.state.params.delivery
    };
  }

  componentWillMount(){
    this.deliveryAddressSubscription = this.customerService.deliveryAddressDao.onSnapshotCompleteObservable.subscribe(this.updateDeliveryAddresses);
  }

  componentWillUnmount(){
    if (this.deliveryAddressSubscription){
      this.deliveryAddressSubscription.dispose();
    }
  }

  updateDeliveryAddresses(deliveryAddresses){
    this.setState(Object.assign({}, this.state.order, {deliveryAddresses}));
    //TODO - handle edge case where not default dlievery address is et
    this.setDeliveryAddress((deliveryAddresses.find(a => a.isDefault)).deliveryAddressId);
  }

  setDeliveryType(type){
    this.setState({delivery: Object.assign({}, this.state.delivery, {type})});
  }

  setDeliveryAddress(deliveryAddressId){
    this.setState({delivery: Object.assign({}, this.state.delivery, {deliveryAddressId})});
  }

  render() {
    const {deliveryAddresses} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Delivery Instructions</Text>
      <Text>Where do you want it?</Text>
      <ListItem>
        <Text>Roadside Delivery</Text>
        <Right>
          <Radio selected={this.state.delivery.type == 'ROADSIDE'} onPress={() => this.setDeliveryType('ROADSIDE')}/>
        </Right>
      </ListItem>
      <ListItem>
        <Text>Carry-in Delivery</Text>
        <Right>
          <Radio selected={this.state.delivery.type == 'CARRYIN'} onPress={() => this.setDeliveryType('CARRYIN')}/>
        </Right>
      </ListItem>

      <Text>Delivery Address</Text>
      <Picker selectedValue={this.state.order.deliveryAddressId} onValueChange={(itemValue) => this.setDeliveryAddress(itemValue)}>
        {deliveryAddresses.map(a => <Picker.Item  key={a.deliveryAddressId} label={a.line1} value={a.deliveryAddressId} />)}
      </Picker>

      <ActionButton buttonText="Next" icon={null} action={() =>  this.navigation.navigate('OrderConfirmation', {order: this.state.order, delivery: this.state.delivery})}/>
    </View>;
  }
}
