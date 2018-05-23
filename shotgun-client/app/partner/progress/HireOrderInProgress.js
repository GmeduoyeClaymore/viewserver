import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Button, Text, View} from 'native-base';
import {SpinnerButton} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {markItemReady} from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';

class HireOrderInProgress extends Component{
  constructor(props){
    super(props);
  }

  static StateKey='customerCheckout';

  markItemReady = async() => {
    const {order, dispatch} = this.props;
    dispatch(markItemReady(order.orderId));
  };

  dispatchItem = async(isOutbound) => {
    const {client, history, deliveryContentType} = this.props;
    const deliveryOrder = await client.invokeJSONCommand('hireOrderController', isOutbound ? 'generateOutboundDeliveryOrder' : 'generateInboundDeliveryOrder', {orderId});
    const checkoutState = {
      selectedContentType: deliveryContentType,
      selectedCategory: deliveryContentType.productCategory,
      order: deliveryOrder,
    };
    this.setState(checkoutState, () => history.push('/Partner/Landing/Checkout/ProductList'));
  };

  render() {
    const {order = {}, busyUpdating} = this.props;
    
    return <View>
      <Button fullWidth padded style={styles.startButton} onPress={this.onNavigatePress}><Text uppercase={false}>Show navigation</Text></Button>
      <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.markItemReady}><Text uppercase={false}>Item Ready</Text></SpinnerButton>
      {order.hireOrderStatus === 'ITEMREADY' ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={() => this.dispatchItem(true)}><Text uppercase={false}>Dispatch Item</Text></SpinnerButton> : null}
      {order.hireOrderStatus === 'OFFHIRE'  ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={() => this.dispatchItem(false)}><Text uppercase={false}>Collect Item</Text></SpinnerButton> : null}
    </View>;
  }
}

const styles = {
  startButton: {
    marginBottom: 15
  }
};

const mapStateToProps = (state, initialProps) => {
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const deliveryContentType = contentTypes.find(ct => ct.contentTypeId === ContentTypes.DELIVERY);
  return {
    ...initialProps,
    deliveryContentType,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ orderDao: 'startJourney'}, { orderDao: 'completeJourney'}])
  };
};

export default withExternalState(mapStateToProps)(HireOrderInProgress);

