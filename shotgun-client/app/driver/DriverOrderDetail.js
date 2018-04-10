import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, View} from 'native-base';
import {OrderSummary, PriceSummary, RatingSummary, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {startOrderRequest, cancelOrderRequest} from 'driver/actions/DriverActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class DriverOrderDetail extends Component{
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
    const {orderSummary = {status: ''}, client, history, dispatch, busy, busyUpdating, me, busyMessage, ordersRoot} = this.props;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    const onStartPress = async() => {
      dispatch(startOrderRequest(orderSummary.orderId, navigateToOrderInProgress));
    };

    const navigateToOrderInProgress = () => history.push({pathname: `${ordersRoot}/DriverOrderInProgress`, transition: 'left'}, {orderId: orderSummary.orderId});

    const onCancelPress = async() => {
      dispatch(cancelOrderRequest(orderSummary.orderId, () => history.push({pathname: `${ordersRoot}/DriverOrders`, transition: 'right'})));
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
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={orderSummary.totalPrice}/>
        <RatingSummary orderSummary={orderSummary} isDriver={true}/>
        {!isComplete ?
          <View>
            {me.userId != orderSummary.customerUserId ? <Button fullWidth padded style={styles.startButton} onPress={navigateToOrderInProgress}><Text uppercase={false}>Show navigation map</Text></Button> : null}
            {me.userId != orderSummary.customerUserId ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={onStartPress}><Text uppercase={false}>Start this job</Text></SpinnerButton> : null}
            <SpinnerButton busy={busyUpdating} fullWidth padded cancelButton onPress={onCancelPress}><Text uppercase={false}>Cancel this job</Text></SpinnerButton>
          </View> : null
        }

        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem}  product={orderSummary.product} contentType={orderSummary.contentType} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  startButton: {
    marginTop: shotgun.contentPadding,
    marginBottom: 15
  }
};

const findOrderSummaryFromDao = (state, orderId, daoName) => {
  const orderSummaries = getDaoState(state, ['orders'], daoName) || [];
  return  orderSummaries.find(o => o.orderId == orderId);
};


const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const pendingResetSubscription = isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    orderId,
    me: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'startOrderRequest'}, { driverDao: 'cancelOrderRequest'}]),
    busy: pendingResetSubscription || !orderSummary,
    busyMessage: pendingResetSubscription ? 'Subscribing to order...' : !orderSummary ? 'Waiting for order...' : undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(DriverOrderDetail);

