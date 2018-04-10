import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row} from 'native-base';
import moment from 'moment';
import {OrderStatuses, getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon, OriginDestinationSummary} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {connect} from 'custom-redux';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('OrderStatusResolver', getProductBasedFriendlyOrderStatusName).
    delivery(getDeliveryFriendlyOrderStatusName).
    rubbish(getRubbishFriendlyOrderStatusName);

/*eslint-enable */

class OrderRequest extends Component {
  constructor(props) {
    super(props);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentWillReceiveProps(newProps){
    ContentTypes.resolveResourceFromProps(newProps, resourceDictionary, this);
  }

  render() {
    const {resources} = this;
    const {orderSummary, history, next, isLast, isFirst} = this.props;
    const {delivery, contentType, quantity: noRequiredForOffload} = orderSummary;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;

    return <ListItem style={[styles.orderRequest, isOnRoute ? styles.orderOnRoute : undefined, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]} onPress={() => history.push({pathname: next, transition: 'zoom'}, {orderId: orderSummary.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={70}>
            <OriginDestinationSummary contentType={contentType} delivery={delivery}/>
          </Col>
          <Col size={30} style={styles.priceRow}>
            <Text style={{...styles.price, marginBottom: 5}}>{orderSummary.product.name }</Text>
            <Text style={styles.price}>£{(orderSummary.totalPrice / 100).toFixed(2)} <Icon name="forward-arrow" style={styles.forwardIcon}/></Text>
            <Text note style={styles.orderStatus}>{resources.OrderStatusResolver(orderSummary)}</Text>
          </Col>
        </Row>
        <Row size={25}>
          <Col size={70}>
            {!delivery.isFixedPrice && contentType.fromTime ? <Row style={{paddingRight: 10}}><Icon paddedIcon name="delivery-time"/><Text>{moment(delivery.from).format('Do MMM, h:mma')}</Text></Row> : null}
            {!delivery.isFixedPrice && contentType.tillTime ? <Row><Icon paddedIcon name="delivery-time"/><Text>{moment(delivery.till).format('Do MMM, h:mma')}</Text></Row> : null}
          </Col>
          {noRequiredForOffload > 0 ?
            <Col size={30} style={styles.noRequiredForOffloadCol}><Row>{noRequiredForOffload > 0 ? [<Icon key='icon' paddedIcon name="one-person"/>, <Text key='text' style={{alignSelf: 'flex-start'}}>{`${noRequiredForOffload} ${noRequiredForOffload > 1 ? 'people' : 'person'}  required`}</Text>] : null}</Row></Col> : null
          }
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
  first: {
    marginTop: 15
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
  },
  forwardIcon: {
    fontSize: 14,
  }
};


const mapStateToProps = (state, initialProps) => {
  const {orderSummary = {}} = initialProps;
  const {contentType: selectedContentType} = orderSummary;
  return {
    selectedContentType,
    ...initialProps,
  };
};

const ConnectedOrderRequest = connect(mapStateToProps)(OrderRequest);
export {ConnectedOrderRequest as OrderRequest};
