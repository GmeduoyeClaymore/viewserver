import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';
import Logger from 'common/Logger';
import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {OrderSummary, PriceSummary, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest} from 'driver/actions/DriverActions';

class DriverOrderRequestDetail extends Component{
  constructor(props) {
    super(props);
  }

  render() {
    const {orderSummary, client, history, dispatch, busy, busyUpdating, errors, delivery, me, busyMessage} = this.props;

    const onAcceptPress = async() => {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push('/Driver/DriverOrders')));
    };

    return busy ? <LoadingScreen text={busyMessage}/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <PriceSummary isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isDriver={true} price={orderSummary.totalPrice}/>
        {me.userId != orderSummary.customerUserId ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.acceptButton} onPress={onAcceptPress}><Text uppercase={false}>Accept this job</Text></SpinnerButton> : null }
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} product={orderSummary.product} client={client} contentType={orderSummary.contentType}/>
      </Content>
    </Container>;
  }
}

const styles = {
  acceptButton: {
    marginTop: 20,
    marginBottom: 10
  }
};

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  if (!orderId){
    Logger.info('Order id must be specified');
    return;
  }
  const orderSummaries = getDaoState(state, ['driver', 'orders'], 'orderRequestDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);
  const {delivery} = (orderSummary || {});
  const errors = getOperationErrors(state, [
    { driverDao: 'acceptOrderRequest'},
    { orderRequestDao: 'resetSubscription'}
  ]);
  const pendingResetSubscription = isAnyOperationPending(state, [{ orderRequestDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    delivery,
    orderId,
    errors,
    me: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'acceptOrderRequest'}, {driverDao: 'updateOrderPrice'}]),
    busyMessage: pendingResetSubscription ? 'Subscribing to order...' : !orderSummary ? 'Waiting for order...' : undefined,
    busy: pendingResetSubscription || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps, true, true
)(DriverOrderRequestDetail);

