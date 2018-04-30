import React, {Component} from 'react';
import {Grid, Row, Text, Spinner} from 'native-base';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {Currency} from 'common/components';

export class PriceSummary extends Component{
  render() {
    const {orderStatus, isPartner, price} = this.props;

    const isComplete = orderStatus == OrderStatuses.COMPLETED;
    const getCustomerHeading = () => isComplete ? 'You were charged' : 'You will be charged';
    const getPartnerHeading = () => isComplete ? 'You were paid' : 'You will be paid';

    return <Grid>
      <Row style={styles.row}><Text style={styles.heading}>{isPartner ? getPartnerHeading() : getCustomerHeading()}</Text></Row>
      <Row style={styles.row}>{!price ? <Spinner/> : <Currency value={price} style={styles.price}/>}</Row>
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
