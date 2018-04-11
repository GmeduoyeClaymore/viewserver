import {Row, Text, Col} from 'native-base';
import React, {Component} from 'react';

export default class VehicleDetails extends Component{
  constructor(props){
    super(props);
  }
  render(){
    const {orderSummary} = this.props;
    const {delivery} = orderSummary;
    <Col>
      <Row>
        <Col>
          <Text style={styles.subTitle}>Vehicle</Text>
          <Text style={styles.data}>{delivery.vehicleMake} {delivery.vehicleModel}, {delivery.vehicleColour}</Text>
          <Text style={styles.data}>{delivery.registrationNumber}</Text>
        </Col>
      </Row>
    </Col>;
  }
}
