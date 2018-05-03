import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row} from 'native-base';
import moment from 'moment';
import {getDaoState} from 'common/dao';
import {OrderStatuses, getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon, OriginDestinationSummary, Currency} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {connect} from 'custom-redux';

class OrderListItem extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  render() {
    const {order, history, next, isLast, isFirst} = this.props;
    const isInProgress = order.orderStatus == OrderStatuses.INPROGRESS;

    return <ListItem style={[styles.order, isInProgress ? styles.orderOnRoute : undefined, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}
      onPress={() => history.push({pathname: next, transition: 'left'}, {orderId: order.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={60}>
            <OriginDestinationSummary order={order}/>
          </Col>
          <Col size={40}>
            <Text style={styles.price} numberOfLines={1}>{order.orderProduct.name}</Text>
            <Currency value={order.amount} style={styles.price}/>
          </Col>
        </Row>
        <Row size={25}>
          <Col size={60}>
            <Row>
              {order.requiredDate ? [<Icon paddedIcon key='icon' name="delivery-time"/>, <Text key='text'>{moment(order.requiredDate).format('Do MMM, h:mma')}</Text>] : null}
            </Row>
          </Col>
          <Col size={40} style={styles.orderStatusRow}>
            <Text note numberOfLines={1} style={styles.orderStatus}>{this.resources.OrderStatusResolver(order)}</Text>
          </Col>
        </Row>
      </Grid>
    </ListItem>;
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('OrderStatusResolver', getProductBasedFriendlyOrderStatusName).
    delivery(getDeliveryFriendlyOrderStatusName).
    rubbish(getRubbishFriendlyOrderStatusName).
  property('supportsFromTime', true).
  property('supportsTillTime', true).
    rubbish(false);
/*eslint-enable */

const styles = {
  order: {
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
  first: {
    marginTop: 15
  },
  locationRow: {
    paddingBottom: 15,
  },
  price: {
    fontSize: 18,
    fontWeight: 'bold',
    padding: 0,
    margin: 0,
    lineHeight: 20,
    alignSelf: 'flex-end'
  },
  orderStatusRow: {
    justifyContent: 'center'
  },
  orderStatus: {
    alignSelf: 'flex-end'
  }
};

const mapStateToProps = (state, initialProps) => {
  const {order = {}} = initialProps;
  const {orderContentTypeId} = order;
  return {
    orderContentTypeId,
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao')
  };
};

const ConnectedOrderRequest = connect(mapStateToProps)(OrderListItem);
export {ConnectedOrderRequest as OrderListItem};
