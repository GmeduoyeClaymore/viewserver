import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, View} from 'native-base';
import {OrderSummary, PriceSummary, RatingSummary, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, findOrderSummaryFromDao} from 'common/dao';
import {startOrderRequest, cancelOrderRequest} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class PartnerOrderDetail extends Component{
  beforeNavigateTo(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'partnerOrderResponse'
      }));
    }
  }

  onStartPress = async() => {
    const {order = {status: ''}, dispatch} = this.props;
    dispatch(startOrderRequest(order.orderId, this.navigateToOrderInProgress));
  };

  navigateToOrderInProgress = () => {
    const {order = {status: ''}, history, ordersRoot} = this.props;
    history.push({pathname: `${ordersRoot}/PartnerOrderInProgress`, transition: 'left'}, {orderId: order.orderId});
  }

  onCancelPress = async() => {
    const {order = {status: ''}, history, dispatch, ordersRoot} = this.props;
    dispatch(cancelOrderRequest(order.orderId, () => history.push({pathname: `${ordersRoot}/PartnerOrders`, transition: 'right'})));
  };

  render() {
    const {order = {status: ''}, client, history, busy, busyUpdating, user} = this.props;
    const isComplete = order.orderStatus == OrderStatuses.COMPLETED;
    const userCreatedThisOrder = user.userId == order.customerUserId;

    //TODO - show the amount - the charge percentage if the user did not create this order
   // const amount = userCreatedThisOrder ? order.amount : calculatePriceToBePaid(order.totalPrice, user);

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
        <PriceSummary orderStatus={order.orderStatus} isPartner={true} price={order.amount}/>
        <RatingSummary order={order} isPartner={true}/>
        {!isComplete ?
          <View>
            {!userCreatedThisOrder ? <Button fullWidth padded style={styles.startButton} onPress={this.navigateToOrderInProgress}><Text uppercase={false}>Show navigation map</Text></Button> : null}
            {!userCreatedThisOrder ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={this.onStartPress}><Text uppercase={false}>Start this job</Text></SpinnerButton> : null}
            <SpinnerButton busy={busyUpdating} fullWidth padded cancelButton onPress={this.onCancelPress}><Text uppercase={false}>Cancel this job</Text></SpinnerButton>
          </View> : null
        }
        <OrderSummary order={order} client={client}/>
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
  let order = findOrderSummaryFromDao(state, orderId, 'partnerOrderResponseDao');
  order = order || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');

  return {
    ...initialProps,
    orderId,
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'startOrderRequest'}, { partnerDao: 'cancelOrderRequest'}]),
    busy: !order,
    order
  };
};

export default connect(mapStateToProps)(PartnerOrderDetail);

