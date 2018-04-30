import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row} from 'native-base';
import moment from 'moment';
import {getDaoState} from 'common/dao';
import {OrderStatuses, getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon, OriginDestinationSummary, Currency} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {connect} from 'custom-redux';


import {calculatePriceToBePaid} from './checkout/CheckoutUtils';

class OrderRequest extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }


  render() {
    const {orderSummary, history, next, isLast, isFirst, user} = this.props;
    const {delivery, contentType, orderItem} = orderSummary;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const {supportsFromTime, supportsTillTime} = resources;
    const userCreatedThisOrder = user.userId == orderSummary.customerUserId;
    const price = userCreatedThisOrder ? orderSummary.totalPrice : calculatePriceToBePaid(orderSummary.totalPrice, user);

    return <ListItem style={[styles.orderRequest, isOnRoute ? styles.orderOnRoute : undefined, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}
      onPress={() => history.push({pathname: next, transition: 'left'}, {orderId: orderSummary.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={60}>
            <OriginDestinationSummary contentType={contentType} delivery={delivery}/>
          </Col>
          <Col size={40}>
            <Text style={styles.price}>{orderSummary.product.name}</Text>
            <Currency value={price} style={styles.price}/>
          </Col>
        </Row>
        <Row size={25}>
          <Col size={60}>
            <Row>
              {supportsFromTime ? [<Icon paddedIcon key='icon' name="delivery-time"/>, <Text key='text'>{moment(orderItem.startTime).format('Do MMM, h:mma')}</Text>] : null}
              {supportsTillTime ? <Text> for {moment(orderItem.endTime).from(moment(orderItem.startTime), true)}</Text> : null}
            </Row>
          </Col>
          <Col size={40} style={styles.orderStatusRow}>
            <Text note style={styles.orderStatus}>{this.resources.OrderStatusResolver(orderSummary)}</Text>
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
  const {orderSummary = {}} = initialProps;
  const {contentType: selectedContentType} = orderSummary;
  return {
    selectedContentType,
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao')
  };
};

const ConnectedOrderRequest = connect(mapStateToProps)(OrderRequest);
export {ConnectedOrderRequest as OrderRequest};