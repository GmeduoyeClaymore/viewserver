import React, {Component} from 'react';
import PropTypes from 'prop-types';
import * as constants from '../../redux/ActionConstants';
import {connect} from 'react-redux';
import {View, Text, Picker} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';

class DeliveryOptions extends Component {
  constructor(props) {
    super(props);
    this.setDeliveryAddress = this.setDeliveryAddress.bind(this);
  }

  componentWillMount(){
    this.setDeliveryAddress(this.props.customer.deliveryAddresses.find(c => c.isDefault).deliveryAddressId);
  }

  setDeliveryAddress(deliveryAddressId){
    this.props.dispatch({type: constants.UPDATE_DELIVERY, delivery: {deliveryAddressId}});
  }

  render() {
    const {customer, delivery, navigation, dispatch} = this.props;

    const setDeliveryType = (type) => {
      dispatch({type: constants.UPDATE_DELIVERY, delivery: {type}});
    };

    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Delivery Instructions</Text>
      <Text>Where do you want it?</Text>
      <ListItem>
        <Text>Roadside Delivery</Text>
        <Right>
          <Radio selected={delivery.type == 'ROADSIDE'} onPress={() => setDeliveryType('ROADSIDE')}/>
        </Right>
      </ListItem>
      <ListItem>
        <Text>Carry-in Delivery</Text>
        <Right>
          <Radio selected={delivery.type == 'CARRYIN'} onPress={() => setDeliveryType('CARRYIN')}/>
        </Right>
      </ListItem>

      <Text>Delivery Address</Text>
      <Picker selectedValue={delivery.deliveryAddressId} onValueChange={(itemValue) => this.setDeliveryAddress(itemValue)}>
        {customer.deliveryAddresses.map(a => <Picker.Item  key={a.deliveryAddressId} label={a.line1} value={a.deliveryAddressId} />)}
      </Picker>

      <ActionButton buttonText="Next" icon={null} action={() =>  navigation.navigate('OrderConfirmation')}/>
    </View>;
  }
}

DeliveryOptions.PropTypes = {
  status: PropTypes.object,
  order: PropTypes.object,
  delivery: PropTypes.object
};

DeliveryOptions.navigationOptions = {header: null};

const mapStateToProps = ({CheckoutReducer, CustomerReducer}) => ({
  customer: CustomerReducer.customer,
  status: CheckoutReducer.status,
  order: CheckoutReducer.order,
  delivery: CheckoutReducer.delivery
});

export default connect(
  mapStateToProps
)(DeliveryOptions);


