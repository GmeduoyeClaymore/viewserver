import React, {Component} from 'react';
import {Text, ListItem, Grid, Col, Row} from 'native-base';
import moment from 'moment';
import {getDaoState} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon, OriginDestinationSummary, Currency} from 'common/components';
import {PaymentTypes} from 'common/constants/PaymentTypes';
import {connect} from 'custom-redux';

class OrderListItem extends Component {
  getArmbandStyle = () => {
    const {orderColorResolver, order} = this.props;
    let armBandColor;

    if (orderColorResolver) {
      armBandColor = orderColorResolver(order);
    }

    return armBandColor != undefined ? {...styles.armBands, borderColor: armBandColor}  : {};
  }

  render() {
    const {order, history, next, isLast, isFirst, orderStatusResolver} = this.props;

    return <ListItem style={[styles.order, this.getArmbandStyle(), isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}
      onPress={() => history.push({pathname: next, transition: 'left'}, {orderId: order.orderId})}>
      <Grid>
        <Row size={75} style={styles.locationRow}>
          <Col size={70}>
            <Text style={styles.name} numberOfLines={1}>{order.title}</Text>
            <OriginDestinationSummary order={order}/>
          </Col>
          <Col size={30}>
            <Currency decimals={0} value={order.amount} style={styles.price} suffix={order.paymentType === PaymentTypes.DAYRATE ? 'p/d' : undefined}/>
          </Col>
        </Row>
        <Row size={25}>
          <Col size={60}>
            <Row>
              {order.requiredDate ? [<Icon paddedIcon key='icon' name="delivery-time"/>, <Text key='text'>{moment(order.requiredDate).format('Do MMM, h:mma')}</Text>] : null}
            </Row>
          </Col>
          {orderStatusResolver ? <Col size={40} style={styles.orderStatusRow}>
            <Text note numberOfLines={1} style={styles.orderStatus}>{!orderStatusResolver ? null : orderStatusResolver(order)}</Text>
          </Col> : null}
        </Row>
      </Grid>
    </ListItem>;
  }
}

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
  armBands: {
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
    alignSelf: 'flex-end'
  },
  name: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    alignSelf: 'flex-start'
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
