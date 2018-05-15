import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Button, Text, View} from 'native-base';
import {SpinnerButton} from 'common/components';
import {getDaoState, isAnyOperationPending} from 'common/dao';
import {offHireItem} from 'customer/actions/CustomerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import * as ContentTypes from 'common/constants/ContentTypes';
class CustomerHireOrderInProgress extends Component{
  constructor(props){
    super(props);
  }

  static StateKey='customerCheckout';

  offHireItem = async() => {
    const {order, dispatch} = this.props;
    dispatch(offHireItem(order.orderId));
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
      <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.offHireItem}><Text uppercase={false}>Off Hire Item</Text></SpinnerButton>
      {order.hireOrderStatus === 'ITEMREADY' ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={() => this.dispatchItem(true)}><Text uppercase={false}>Deliver Item</Text></SpinnerButton> : null}
      {order.hireOrderStatus === 'OFFHIRE'  ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.completeButton} onPress={() => this.dispatchItem(false)}><Text uppercase={false}>Dispatch Item</Text></SpinnerButton> : null}
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

export default withExternalState(mapStateToProps)(CustomerHireOrderInProgress);

