import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Button, Text, View} from 'native-base';
import {SpinnerButton} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {logDayStart, logDayComplete} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';

class PartnerMyOrderDetail extends Component{
  constructor(props){
    super(props);
  }

  logDayComplete = async() => {
    const {order, dispatch} = this.props;
    dispatch(logDayComplete(order.orderId));
  };

  logDayStart = async() => {
    const {order, dispatch} = this.props;
    dispatch(logDayStart(order.orderId));
  };

  render() {
    const {order = {}, busyUpdating} = this.props;
    
    return <View>
      <Button fullWidth padded style={styles.startButton} onPress={this.onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button>
      {!order.dayStarted ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.logDayStart}><Text uppercase={false}>Start Day</Text></SpinnerButton> : null}
      {order.dayStarted  ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={this.logDayComplete}><Text uppercase={false}>Complete Day</Text></SpinnerButton> : null}
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

export default connect(mapStateToProps)(PartnerMyOrderDetail);

