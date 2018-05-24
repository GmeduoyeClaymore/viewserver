import React, {Component} from 'react';
import {cancelOrder} from 'customer/actions/CustomerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import {SpinnerButton} from 'common/components';
import {Text} from 'native-base';
import {Alert} from 'react-native';

export default class CancelControl extends Component {
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onCancelOrder = () => {
    const {order, dispatch} = this.props;
    const {orderId, orderContentTypeId} = order;

    Alert.alert(
      'Cancel this job?',
      'Are you sure you want to cancel this job? This action cannot be undone.',
      [
        {text: 'No', style: 'cancel'},
        {text: 'Yes', onPress: () =>  dispatch(cancelOrder({orderId, orderContentTypeId}))},
      ],
      { cancelable: false }
    );
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
