import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, View} from 'native-base';
import {OrderSummary, PriceSummary, RatingSummary, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, findOrderSummaryFromDao} from 'common/dao';
import {startOrderRequest, cancelOrderRequest} from 'driver/actions/DriverActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {calculatePriceToBePaid} from 'common/components/checkout/CheckoutUtils';

class DriverOrderDetail extends Component{
  beforeNavigateTo(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'driverOrderSummary'
      }));
    }
  }

  onStartPress = async() => {
    const {orderSummary = {status: ''}, dispatch} = this.props;
    dispatch(startOrderRequest(orderSummary.orderId, navigateToOrderInProgress));
  };

  navigateToOrderInProgress = () => {
    const {orderSummary = {status: ''}, history, ordersRoot} = this.props;
    history.push({pathname: `${ordersRoot}/DriverOrderInProgress`, transition: 'left'}, {orderId: orderSummary.orderId});
  }

  onCancelPress = async() => {
    const {orderSummary = {status: ''}, history, dispatch, ordersRoot} = this.props;
    dispatch(cancelOrderRequest(orderSummary.orderId, () => history.push({pathname: `${ordersRoot}/DriverOrders`, transition: 'right'})));
  };

  render() {
    const {orderSummary = {status: ''}, client, history, busy, busyUpdating, user} = this.props;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const userCreatedThisOrder = user.userId == orderSummary.customerUserId;
    const price = userCreatedThisOrder ? orderSummary.totalPrice : calculatePriceToBePaid(orderSummary.totalPrice, user);

    return busy ? <LoadingScreen text='Waiting for order'/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack({transition: 'right'})}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={price}/>
        <RatingSummary orderSummary={orderSummary} isDriver={true}/>
        {!isComplete ?
          <View>
            {!userCreatedThisOrder ? <Button fullWidth padded style={styles.startButton} onPress={this.navigateToOrderInProgress}><Text uppercase={false}>Show navigation map</Text></Button> : null}
            {!userCreatedThisOrder ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.onStartPress}><Text uppercase={false}>Start this job</Text></SpinnerButton> : null}
            <SpinnerButton busy={busyUpdating} fullWidth padded cancelButton onPress={this.onCancelPress}><Text uppercase={false}>Cancel this job</Text></SpinnerButton>
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

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');

  return {
    ...initialProps,
    orderId,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'startOrderRequest'}, { driverDao: 'cancelOrderRequest'}]),
    busy: !orderSummary,
    orderSummary
  };
};

export default connect(mapStateToProps)(DriverOrderDetail);

