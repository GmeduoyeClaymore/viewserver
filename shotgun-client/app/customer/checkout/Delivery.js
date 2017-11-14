import React, {Component} from 'react';
import PropTypes from 'prop-types';
import * as constants from '../../redux/ActionConstants';
import {connect} from 'react-redux';
import {View, Text, Slider} from 'react-native';
import ActionButton from '../../common/components/ActionButton';
import {ListItem, Radio, Right} from 'native-base';

class Delivery extends Component {
  constructor(props) {
    super(props);
    this.setIsDeliveryRequired = this.setIsDeliveryRequired.bind(this);
    this.state = {
      isDeliveryRequired: true
    };
  }

  setIsDeliveryRequired(isDeliveryRequired){
    this.setState({isDeliveryRequired});
  }

  render() {
    const {navigation, delivery} = this.props;

    const setEta = (eta) => this.props.dispatch({type: constants.UPDATE_DELIVERY, delivery: {eta}});
    const getDestination =  () => this.state.isDeliveryRequired ? 'DeliveryOptions' : 'OrderConfirmation';

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

      <Text>{`Required within ${delivery.eta} hours`}</Text>
      <Slider minimumValue={1} maximumValue={72} step={1} value={delivery.eta} onValueChange={val => setEta(val)}/>
      <ActionButton buttonText="Next" icon={null} action={() => navigation.navigate(getDestination())}/>
    </View>;
  }
}

Delivery.PropTypes = {
  status: PropTypes.object,
  delivery: PropTypes.object
};

Delivery.navigationOptions = {header: null};

const mapStateToProps = ({CheckoutReducer}) => ({
  status: CheckoutReducer.status,
  delivery: CheckoutReducer.delivery
});

export default connect(
  mapStateToProps
)(Delivery);


