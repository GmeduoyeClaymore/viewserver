import React, { Component } from 'react';
import PropTypes from 'prop-types';

class OrderLifecycleView extends Component {
    static propTypes = {
      order: PropTypes.object.isRequired,
      PlacedControls: PropTypes.array.isRequired,
      InProgressControls: PropTypes.array.isRequired,
      AcceptedControls: PropTypes.array.isRequired,
      CompletedControls: PropTypes.array.isRequired,
      CancelledControls: PropTypes.array.isRequired
    };

    renderChildren(children){
      return children.map(
        (Child, idx) => <Child key={idx} {...this.props}/>
      );
    }

    render() {
      const {order} = this.props;
      const {orderStatus} = order;
      if (orderStatus === 'PLACED'){
        return this.renderChildren(this.props.PlacedControls);
      }
      if (orderStatus === 'ACCEPTED'){
        return this.renderChildren(this.props.AcceptedControls);
      }
      if (orderStatus === 'INPROGRESS'){
        return this.renderChildren(this.props.InProgressControls);
      }
      if (orderStatus === 'COMPLETED'){
        return this.renderChildren(this.props.CompletedControls);
      }
      if (orderStatus === 'CANCELLED'){
        return this.renderChildren(this.props.CancelledControls);
      }
      throw new Error('Unrecognised order status \"' + orderStatus + '"');
    }
}

export default OrderLifecycleView;
