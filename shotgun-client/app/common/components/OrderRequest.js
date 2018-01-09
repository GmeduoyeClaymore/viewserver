import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row, Icon} from 'native-base';
import moment from 'moment';
import Products from 'common/constants/Products';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import { withRouter } from 'react-router';

class OrderRequest extends Component {
  constructor() {
    super();
  }

  render() {
    const {orderSummary, history, next} = this.props;
    const {delivery, status} = orderSummary;
    const {origin, destination, noRequiredForOffload} = delivery;
    const isRowDelivery = orderSummary.orderItem.productId == Products.DELIVERY;

    const isComplete = status == OrderStatuses.COMPLETED;

    return <ListItem style={styles.orderRequest} onPress={() => history.push(next, {orderSummary})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={75}>
            <Row>
              <Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text>
            </Row>
            {isRowDelivery ? <Row><Text time>| 3 hrs</Text></Row> : null}
            {isRowDelivery ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
          </Col>
          <Col size={25} style={styles.priceRow}>
            <Text style={styles.price}>£XX.XX <Icon name="arrow-forward"/></Text>
            {!isComplete ? <Text note style={styles.estimate}>estimated</Text> : null}
          </Col>
        </Row>
        <Row size={25}>
          <Col size={50}><Row><Icon paddedIcon name="time"/><Text>{moment(delivery.eta).format('Do MMM, h:mma')}</Text></Row></Col>
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
