import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row} from 'native-base';
import moment from 'moment';
import {getDaoState} from 'common/dao';
import {getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon, OriginDestinationSummary, Currency} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {connect} from 'custom-redux';

class OrderRequest extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  render() {
    const {orderRequest, history, next, isLast, isFirst} = this.props;
    const {orderDetails} = orderRequest;

    const isInProgress = orderRequest.status == 'INPROGRESS';

    return <ListItem style={[styles.orderRequest, isInProgress ? styles.orderOnRoute : undefined, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}
      onPress={() => history.push({pathname: next, transition: 'left'}, {orderId: orderRequest.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={60}>
            <OriginDestinationSummary order={orderDetails}/>
          </Col>
          <Col size={40}>
            <Text style={styles.price}>{orderDetails.Title}</Text>
            <Currency value={orderDetails.amount} style={styles.price}/>
          </Col>
        </Row>
        <Row size={25}>
          <Col size={60}>
            <Row>
              {orderDetails.requiredDate ? [<Icon paddedIcon key='icon' name="delivery-time"/>, <Text key='text'>{moment(orderDetails.requiredDate).format('Do MMM, h:mma')}</Text>] : null}
            </Row>
          </Col>
          <Col size={40} style={styles.orderStatusRow}>
            <Text note style={styles.orderStatus}>{this.resources.OrderStatusResolver(orderRequest)}</Text>
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
  const {orderRequest = {}} = initialProps;
  const {orderContentTypeId} = orderRequest;
  return {
    orderContentTypeId,
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao')
  };
};

const ConnectedOrderRequest = connect(mapStateToProps)(OrderRequest);
export {ConnectedOrderRequest as OrderRequest};
