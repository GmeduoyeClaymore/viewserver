import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Text, View} from 'native-base';
import {SpinnerButton} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {partnerStartJob, partnerCompleteJob} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';


const CAN_START_STATUSES = ['ASSIGNED'];
const CAN_COMPLETE_STATUSES = ['STARTED'];

class FixedPersonellOrderInProgress extends Component{
  constructor(props){
    super(props);
  }

  onJobStart = async() => {
    const {order, dispatch} = this.props;
    dispatch(partnerStartJob(order.orderId, order.orderContentTypeId));
  };

  onJobComplete = async() => {
    const {order, dispatch} = this.props;
    dispatch(partnerCompleteJob(order.orderId, order.orderContentTypeId));
  };

  render() {
    const {order = {}, busyUpdating} = this.props;
    return <View>
      {!!~CAN_START_STATUSES.indexOf(order.negotiatedOrderStatus) ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.onJobStart}><Text uppercase={false}>Start Job</Text></SpinnerButton> : null}
      {!!~CAN_COMPLETE_STATUSES.indexOf(order.negotiatedOrderStatus)  ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={this.onJobComplete}><Text uppercase={false}>Complete Job</Text></SpinnerButton> : null}
    </View>;
  }
}

const styles = {
  startButton: {
    marginTop: shotgun.contentPadding,
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ orderDao: 'startJourney'}, { orderDao: 'completeJourney'}])
  };
};

export default connect(mapStateToProps)(FixedPersonellOrderInProgress);

