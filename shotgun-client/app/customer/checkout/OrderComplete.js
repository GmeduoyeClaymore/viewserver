import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import ActionButton from '../../common/components/ActionButton';

export default class OrderComplete extends Component {
  static PropTypes = {
    customerService: PropTypes.object
  };

  static navigationOptions = {header: null};

  constructor(props) {
    super(props);
    this.orderId = props.navigation.state.params.orderId;
    this.navigation = props.navigation;
  }

  render() {
    return <View style={{flex: 1, flexDirection: 'column'}}>
      <Text>Your Order Has Been Placed</Text>
      <Text>{`Order Id ${this.orderId}`}</Text>
      <ActionButton buttonText="Continue Shopping" icon={null} action={() => this.navigation.navigate('Home')}/>
    </View>;
  }
}
