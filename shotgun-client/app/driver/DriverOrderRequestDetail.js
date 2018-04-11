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

  beforeNavigateTo(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'driverOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary, client, history, dispatch, busy, busyUpdating, errors, delivery = {}, me, busyMessage, ordersRoot} = this.props;

    const onAcceptPress = async() => {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push({pathname: `${ordersRoot}/DriverOrders`, transition: 'left'})));
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

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  if (!orderId){
    Logger.info('Order id must be specified');
    return;
  }
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
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

