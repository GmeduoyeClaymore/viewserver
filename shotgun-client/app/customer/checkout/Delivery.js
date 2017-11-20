import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, Text, Slider} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';

export default class Delivery extends Component{
  static propTypes = {
    navigation: PropTypes.object
  };
  
  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.setState = this.setState.bind(this);
    this.state = {
      isDeliveryRequired: true,
      eta: 72,
      paymentId: props.navigation.state.params.paymentId
    };
  }

  render() {
    const {navigation} = this.props;
    const {eta, isDeliveryRequired} = this.state;
    const destination = isDeliveryRequired ? 'DeliveryOptions' : 'OrderConfirmation';

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Delivery Instructions</Text>
      <ListItem>
        <Text>Store Pickup</Text>
        <Right>
          <Radio selected={!isDeliveryRequired} onPress={() => this.setState({isDeliveryRequired: false})}/>
        </Right>
      </ListItem>
      <ListItem>
        <Text>Shotgun Delivery</Text>
        <Right>
          <Radio selected={isDeliveryRequired} onPress={() => this.setState({isDeliveryRequired: true})}/>
        </Right>
      </ListItem>

      <Text>{`Required within ${eta} hours`}</Text>
      <Slider minimumValue={1} maximumValue={72} step={1} value={eta} onValueChange={val => this.setState({eta: val})}/>
      <ActionButton buttonText="Next" icon={null} action={() => navigation.navigate(destination, this.state)}/>
    </View>;
  }
}
