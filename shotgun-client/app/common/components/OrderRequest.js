import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row, Icon} from 'native-base';
import moment from 'moment';
import { withRouter } from 'react-router';
import {getFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';

class OrderRequest extends Component {
  constructor() {
    super();
  }

  render() {
    const {orderSummary, history, next, isLast} = this.props;
    const {delivery, contentType} = orderSummary;
    const {origin, destination, noRequiredForOffload} = delivery;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;

    return <ListItem style={[styles.orderRequest, isOnRoute ? styles.orderOnRoute : undefined, isLast ? styles.last : undefined]} onPress={() => history.push(next, {orderId: orderSummary.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={70}>
            <Row>
              <Icon name="pin" paddedIcon originPin/><Text style={styles.origin}>{origin.line1}, {origin.postCode}</Text>
            </Row>
            {contentType.origin ? <Row><Icon name="pin" paddedIcon originPin/><Text>{origin.line1}, {origin.postCode}</Text></Row> : null}
            {delivery.duration ? <Row><Text time>| {delivery.duration} hrs</Text></Row> : null}
            {contentType.destination ? <Row><Icon paddedIcon name="pin"/><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
          </Col>
          <Col size={30} style={styles.priceRow}>
            <Text style={styles.price}>£{(orderSummary.totalPrice / 100).toFixed(2)} <Icon name="arrow-forward"/></Text>
            <Text note style={styles.orderStatus}>{getFriendlyOrderStatusName(orderSummary.status)}</Text>
          </Col>
        </Row>
        <Row size={25}>
          {contentType.fromTime ? <Col size={50}><Row><Icon paddedIcon name="time"/><Text>{moment(delivery.from).format('Do MMM, h:mma')}</Text></Row></Col> : null}
          {contentType.tillTime ? <Col size={50}><Row><Icon paddedIcon name="time"/><Text>{moment(delivery.till).format('Do MMM, h:mma')}</Text></Row></Col>  : null}
          <Col size={50} style={styles.noRequiredForOffloadCol}><Row>{noRequiredForOffload > 0 ? [<Icon key='icon' paddedIcon name="man"/>, <Text key='text'>{`${noRequiredForOffload} people required`}</Text>] : null}</Row></Col>
        </Row>
      </Grid>
    </ListItem>;
  }
}

const styles = {
  orderRequest: {
    paddingTop: 10,
    paddingRight: 15,
    paddingBottom: 10,
    paddingLeft: 25,
    marginBottom: 15,
    borderTopWidth: 0.5,
    borderBottomWidth: 0.5,
    borderColor: shotgun.silver
  },
  orderOnRoute: {
    borderColor: shotgun.gold,
    borderWidth: 10,
    paddingRight: 5,
    paddingLeft: 15
  },
  last: {
    marginBottom: 0
  },
  priceRow: {
    alignItems: 'flex-end',
    justifyContent: 'flex-start',
  },
  locationRow: {
    paddingBottom: 20,
  },
  origin: {
    alignSelf: 'flex-start'
  },
  price: {
    fontSize: 18,
    fontWeight: 'bold',
    lineHeight: 18,
    alignSelf: 'flex-end'
  },
  orderStatus: {
    alignSelf: 'flex-end'
  },
  noRequiredForOffloadCol: {
    alignItems: 'flex-end'
  }
};

export default withRouter(OrderRequest);
