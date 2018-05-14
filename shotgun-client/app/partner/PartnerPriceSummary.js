import React, {Component} from 'react';
import {Grid, Row, Text, Spinner} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {Currency} from 'common/components';
import {PaymentTypes} from 'common/constants/PaymentTypes';

export default class PartnerPriceSummary extends Component{
  getHeading = () => {
    const {order} = this.props;
    const {orderStatus} = order;

    if (orderStatus == OrderStatuses.PLACED){
      return 'Job advertised for';
    } else if (orderStatus == OrderStatuses.COMPLETED){
      return 'You were paid';
    }
    if (orderStatus == OrderStatuses.CANCELLED){
      return 'Job cancelled';
    }

    return 'You will be paid';
  }

  render() {
    const {order} = this.props;
    const {amount, paymentType} = order;

    const suffix = paymentType === PaymentTypes.DAYRATE ? 'p/d' : undefined;

    return <Grid style={styles.grid}>
      <Row style={styles.row}><Text style={styles.heading}>{this.getHeading()}</Text></Row>
      <Row style={styles.row}>{!amount ? <Spinner/> : <Currency value={amount} style={styles.amount} suffix={suffix}/>}</Row>
    </Grid>;
  }
}

const styles = {
  grid: {
    marginBottom: 15,
    flex: -1
  },
  row: {
    justifyContent: 'center'
  },
  heading: {
    fontSize: 16
  },
  amount: {
    fontSize: 30,
    lineHeight: 34,
    fontWeight: 'bold'
  }
};
