import React, {Component} from 'react';
import {Grid, Row, Text} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';

export default class PriceSummary extends Component{
  constructor(){
    super();
  }

  render() {
    const {orderStatus, isDriver, price} = this.props;

    const isComplete = orderStatus == OrderStatuses.COMPLETED;
    const getCustomerHeading = () => isComplete ? 'You were charged' : 'You will be charged';
    const getDriverHeading = () => isComplete ? 'You were paid' : 'You will be paid';

    return <Grid>
      <Row style={styles.row}><Text style={styles.heading}>{isDriver ? getDriverHeading() : getCustomerHeading()}</Text></Row>
      <Row style={styles.row}><Text style={styles.price}>£{price}</Text></Row>
    </Grid>;
  }
}

const styles = {
  row: {
    justifyContent: 'center'
  },
  heading: {
    fontSize: 16
  },
  price: {
    fontSize: 30,
    lineHeight: 34,
    fontWeight: 'bold'
  }
};
