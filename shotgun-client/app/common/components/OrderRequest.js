import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row, Icon} from 'native-base';
import moment from 'moment';
import { withRouter } from 'react-router';

class OrderRequest extends Component {
  constructor() {
    super();
  }

  render() {
    const {orderSummary, history, next} = this.props;
    const {delivery, contentType} = orderSummary;
    const {origin, destination, noRequiredForOffload} = delivery;

    return <ListItem style={styles.orderRequest} onPress={() => history.push(next, {orderId: orderSummary.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={75}>
            <Row>
              <Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text>
            </Row>
            {contentType.origin ? <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row> : null}
            {delivery.duration ? <Row><Text time>| {delivery.duration} hrs</Text></Row> : null}
            {contentType.destination ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
          </Col>
          <Col size={25} style={styles.priceRow}>
            <Text style={styles.price}>£{(orderSummary.totalPrice / 100).toFixed(2)} <Icon name="arrow-forward"/></Text>
          </Col>
        </Row>
        <Row size={25}>
          {contentType.fromTime ? <Col size={50}><Row><Icon paddedIcon name="time"/><Text>{moment(delivery.from).format('Do MMM, h:mma')}</Text></Row></Col> : null}
          {contentType.tillTime ? <Col size={50}><Row><Icon paddedIcon name="time"/><Text>{moment(delivery.till).format('Do MMM, h:mma')}</Text></Row></Col>  : null}
          <Col size={50}><Row>{noRequiredForOffload > 0 ? [<Icon key='icon' paddedIcon name="man"/>, <Text key='text'>{`${noRequiredForOffload} people required`}</Text>] : null}</Row></Col>
        </Row>
      </Grid>
    </ListItem>;
  }
}

const styles = {
  orderRequest: {
    paddingTop: 27,
    paddingRight: 13,
    paddingBottom: 27,
    paddingLeft: 21
  },
  priceRow: {
    alignItems: 'flex-start',
    justifyContent: 'flex-start'
  },
  locationRow: {
    paddingBottom: 20
  },
  price: {
    fontSize: 18,
    fontWeight: 'bold',
    lineHeight: 18
  },
  estimate: {
    lineHeight: 12,
    paddingRight: 5
  }
};

export default withRouter(OrderRequest);
