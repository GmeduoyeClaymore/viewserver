import {Row, Text, Col} from 'native-base';
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
    return <Col>
      <Row>
        <Col style={{marginLeft: 15}}>
          <Text style={styles.subTitle}>Vehicle</Text>
          <Text style={styles.data}>{vehicle.make} {vehicle.model}, {vehicle.model}</Text>
          <Text style={styles.data}>{vehicle.registrationNumber}</Text>
        </Col>
      </Row>
    </Col>;
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
