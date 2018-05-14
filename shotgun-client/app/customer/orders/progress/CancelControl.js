import React, {Component} from 'react';
import {cancelOrder} from 'customer/actions/CustomerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import {SpinnerButton} from 'common/components';
import {Text} from 'native-base';

export default class CancelControl extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onCancelOrder = () => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;
    dispatch(cancelOrder({orderId, orderContentTypeId}));
  };

  render() {
    const {busyUpdating} = this.props;
    return <SpinnerButton busy={busyUpdating} padded paddedTopBottom fullWidth danger onPress={this.onCancelOrder}><Text uppercase={false}>{this.resources.CancelCaption}</Text></SpinnerButton>;
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('CancelCaption', 'Cancel').
delivery('Cancel Delivery').
hire('Cancel Hire').
personell('Cancel Job').
rubbish('Cancel Pickup');
/*eslint-enable */
