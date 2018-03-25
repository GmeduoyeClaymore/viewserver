import React, {Component} from 'react';
import {Grid, Row, Text, Spinner} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';

export class PriceSummary extends Component{
  constructor(){
    super();
  }

  render() {
    const {orderStatus, isDriver, price, isFixedPrice} = this.props;

    const isComplete = orderStatus == OrderStatuses.COMPLETED;
    const getCustomerHeading = () => isComplete ? 'You were charged' : `You will be charged ${!isFixedPrice ? '(estimated)' : ''}`;
    const getDriverHeading = () => isComplete ? 'You were paid' : `You will be paid ${!isFixedPrice ? '(estimated)' : ''}`;

    return <Grid>
      <Row style={styles.row}><Text style={styles.heading}>{isDriver ? getDriverHeading() : getCustomerHeading()}</Text></Row>
      <Row style={styles.row}>{!price ? <Spinner/> : <Text style={styles.price}>£{(price / 100).toFixed(2)}</Text>}</Row>
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
