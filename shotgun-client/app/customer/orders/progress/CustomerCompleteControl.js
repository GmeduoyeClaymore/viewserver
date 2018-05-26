import React, { Component } from 'react';
import {Alert} from 'react-native';
import * as ContentTypes from 'common/constants/ContentTypes';
import {customerCompleteAndPay} from 'customer/actions/CustomerActions';
import {Text} from 'native-base';
import {SpinnerButton} from 'common/components';
import {formatPrice} from 'common/components/Currency';

class CustomerCompleteControl extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onCompletePress = async() => {
    const {order, dispatch} = this.props;
    const {amountToPay, assignedPartner} = order;

    Alert.alert(
      'Complete and pay for job?',
      `You are about to complete this job and pay ${formatPrice(amountToPay)} to ${assignedPartner.firstName} ${assignedPartner.lastName}`,
      [
        {text: 'Cancel', style: 'cancel'},
        {text: 'OK', onPress: () => dispatch(customerCompleteAndPay(order.orderId, order.orderContentTypeId))},
      ],
      { cancelable: false }
    );
  };

  render() {
    const {order, busyUpdating} = this.props;
    return  <SpinnerButton busy={busyUpdating} fullWidth padded onPress={this.onCompletePress}><Text uppercase={false}>{this.resources.CompleteCaption(order)}</Text></SpinnerButton>;
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

export default CustomerCompleteControl;
