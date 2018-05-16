import {Text, View} from 'native-base';
import React, {Component} from 'react';

export class VehicleInfo extends Component{
  constructor(props){
    super(props);
  }
  render(){
    const {order} = this.props;
    const {vehicle} = order;
    if (!vehicle){
      return null;
    }
    return <View style={styles.view}>
      <Text style={styles.registrationNumber}>{vehicle.registrationNumber}</Text>
      <Text> - {vehicle.make} {vehicle.model} {vehicle.colour}</Text>
    </View>;
  }
}

const styles = {
  view: {
    flexDirection: 'row'
  },
  registrationNumber: {
    fontWeight: 'bold',
    fontSize: 16
  }
};
