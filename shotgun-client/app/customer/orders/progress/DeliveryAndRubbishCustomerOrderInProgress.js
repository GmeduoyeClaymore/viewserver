
import React, {Component} from 'react';
import PartnerDetails from 'common/components/UserInfo';

import CallButtons from './CallButtons';

export default class DeliveryAndRubbishCustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }
  render() {
    return [
      <PartnerDetails key="1" {...this.props}/>,
      <VehicleDetails key="2"  {...this.props}/>
    ];
  }
}

