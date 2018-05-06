import React, { Component } from 'react';
import * as ContentTypes from 'common/constants/ContentTypes';
import {customerCompleteAndPay} from 'partner/actions/CustomerAactions';

class CompleteControl extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }
  render() {
    const {order} = this.props;
    const {resources} = this;
    const onCompletePress = async() => {
      dispatch(customerCompleteAndPay(order.orderId));
    };
    return  <SpinnerButton busy={busyUpdating} paddedBottom fullWidth onPress={onCompletePress}><Text uppercase={false}>{resources.CompleteCaption}</Text></SpinnerButton>;
  }
}
/*eslint-disable */
resourceDictionary.a
  property('CompleteCaption', () => 'Complete').
    delivery((order) => `Complete Delivery - Pay (£${order.amountToPay})`).
    hire((order) => `Complete Hire - Pay (£${order.amountToPay})`).
    personell((order) => `Complete Job - Pay (£${order.amountToPay})`).
    rubbish((order) => `Complete Collection - Pay (£${order.amountToPay})`);
/*eslint-enable */

export default CompleteControl;
