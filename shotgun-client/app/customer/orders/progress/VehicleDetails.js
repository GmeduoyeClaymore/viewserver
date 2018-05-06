import {Row, Text, Col} from 'native-base';
import React, {Component} from 'react';

export default class VehicleDetails extends Component{
  constructor(props){
    super(props);
  }
  render(){
    const {order} = this.props;
    const {vehicle = {}} = order;
    <Col>
      <Row>
        <Col>
          <Text style={styles.subTitle}>Vehicle</Text>
          <Text style={styles.data}>{vehicle.vehicleMake} {vehicle.vehicleModel}, {vehicle.vehicleColour}</Text>
          <Text style={styles.data}>{vehicle.registrationNumber}</Text>
        </Col>
      </Row>
    </Col>;
  }
}
