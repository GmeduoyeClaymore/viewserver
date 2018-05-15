import React, { Component } from 'react';
import * as ContentTypes from 'common/constants/ContentTypes';
import {customerCompleteAndPay} from 'customer/actions/CustomerActions';
import {Text} from 'native-base';
import {SpinnerButton} from 'common/components';

class CompleteControl extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onCompletePress = async() => {
    const {order, dispatch} = this.props;
    dispatch(customerCompleteAndPay(order.orderId, order.orderContentTypeId));
  };

  render() {
    const {order, busyUpdating} = this.props;
    return  <SpinnerButton busy={busyUpdating} fullWidth padded paddedTopBottom onPress={this.onCompletePress}><Text uppercase={false}>{this.resources.CompleteCaption(order)}</Text></SpinnerButton>;
  }
}
/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('CompleteCaption', () => 'Complete').
    delivery((order) => 'Complete Delivery' + (order.amountToPay ? ` - (Pay ${format(order.amountToPay)})` : '')).
    hire((order) => 'Complete Hire' + (order.amountToPay ? ` - (Pay ${format(order.amountToPay)})` : '')).
    personell((order) => 'Complete Job' + (order.amountToPay ? ` - (Pay ${format(order.amountToPay)})` : '')).
    rubbish((order) => 'Complete Collection' + (order.amountToPay ? ` - (Pay ${format(order.amountToPay)})` : ''));
/*eslint-enable */

const format = (value) => {
  const currency = '£';
  return  value ? currency + (value / 100).toFixed(2) : '';
};

export default CompleteControl;
