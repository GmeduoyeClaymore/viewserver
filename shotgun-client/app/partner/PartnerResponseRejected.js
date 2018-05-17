import React, { Component } from 'react';
import {View, Spinner, Text} from 'native-base';
import { OrderSummary, LoadingScreen, Icon, ErrorRegion, RatingSummary, Tabs } from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';

export default PartnerResponseRejected = ({order}) => {
  return <View padded style={styles.view}>
    <Icon name='cross' style={styles.declinedIcon}/>
    <Text style={{alignSelf: 'center'}} numberOfLines={1}>{`${order.customer.firstName} ${order.customer.lastName} declined your offer`}</Text>
  </View>;
};

const styles = {
  view: {
    flexDirection: 'row',
    justifyContent: 'center'
  },
  declinedIcon: {
    marginRight: shotgun.contentPadding,
    alignSelf: 'center',
    fontSize: 30,
    padding: 2,
    color: shotgun.brandDanger,
  }
}