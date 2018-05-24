import React from 'react';
import PropTypes from 'prop-types';
import OrderLifecycleView from './OrderLifecycleView';
import {NegotiationStatuses} from 'common/constants/NegotiationStatuses';

class PartnerOrderLifecycleView extends OrderLifecycleView {
    static propTypes = {
      order: PropTypes.object.isRequired,
      PlacedControls: PropTypes.array.isRequired,
      InProgressControls: PropTypes.array.isRequired,
      AcceptedControls: PropTypes.array.isRequired,
      CompletedControls: PropTypes.array.isRequired,
      CancelledControls: PropTypes.array.isRequired,
      RejectedControls: PropTypes.array.isRequired,
    };

    render() {
      const {order} = this.props;
      const {responseInfo} = order;
      const {responseStatus} = responseInfo;
      if (responseStatus === NegotiationStatuses.REJECTED){
        return this.renderChildren(this.props.RejectedControls);
      }
      return super.render();
    }
}

export default PartnerOrderLifecycleView;
