import {Row, Text, View} from 'native-base';
import React, {Component} from 'react';

export default class VehicleDetails extends Component{
  constructor(props){
    super(props);
  }
  render(){
    const {order} = this.props;
    const {vehicle} = order;
    if (!vehicle){
      return null;
    }
    return <View style={{marginLeft: 30, marginTop: 15, flexDirection: 'column'}}>
      <Text style={styles.data}>{vehicle.make} {vehicle.model}, {vehicle.model}</Text>
      <Text style={styles.data}>{vehicle.registrationNumber}</Text>
    </View>;
  }
}

const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30,
    fontSize: 13
  },
  data: {
  }
};
