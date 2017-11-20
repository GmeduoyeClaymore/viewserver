import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';
import {getDaoState} from 'common/dao';

const DEFAULT_DELIVERY_ADDRESSES = [];

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.setDeliveryAddress = this.setDeliveryAddress.bind(this);
    this.state = {...props.navigation.state.params};
  }

  componentWillMount(){
    const {deliveryAddresses} = this.props;
    const defaultAddress = deliveryAddresses.find(c => c.isDefault) || deliveryAddresses[0];
    if (defaultAddress){
      this.setDeliveryAddress(defaultAddress.deliveryAddressId);
    }
  }

  setDeliveryAddress(deliveryAddressId){
    this.setState({deliveryAddressId});
  }

  setDeliveryType(deliveryType){
    this.setState({deliveryType});
  }

  render() {
    const {deliveryAddresses, navigation} = this.props;
    const {deliveryType = 'ROADSIDE', deliveryAddressId} = this.state;

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Delivery Instructions</Text>
      <Text>Where do you want it?</Text>
      <ListItem>
        <Text>Roadside Delivery</Text>
        <Right>
          <Radio selected={deliveryType == 'ROADSIDE'} onPress={() => setDeliveryType('ROADSIDE')}/>
        </Right>
      </ListItem>
      <ListItem>
        <Text>Carry-in Delivery</Text>
        <Right>
          <Radio selected={deliveryType == 'CARRYIN'} onPress={() => setDeliveryType('CARRYIN')}/>
        </Right>
      </ListItem>

      <Text>Delivery Address</Text>
      <Picker selectedValue={deliveryAddressId} onValueChange={(itemValue) => this.setDeliveryAddress(itemValue)}>
        {deliveryAddresses.map(a => <Picker.Item  key={a.deliveryAddressId} label={a.line1} value={a.deliveryAddressId} />)}
      </Picker>

      <ActionButton buttonText="Next" icon={null} action={() =>  navigation.navigate('OrderConfirmation', this.state)}/>
    </View>;
  }
}

DeliveryOptions.PropTypes = {
  deliveryAddresses: PropTypes.array
};

DeliveryOptions.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => ({
  deliveryAddresses: getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao') || DEFAULT_DELIVERY_ADDRESSES,
  ...initialProps
});

export default connect(
  mapStateToProps
)(DeliveryOptions);


