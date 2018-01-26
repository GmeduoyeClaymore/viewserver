import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content, Text} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import LoadingScreen from 'common/components/LoadingScreen';
import PriceSummary from 'common/components/PriceSummary';
import RatingSummary from 'common/components/RatingSummary';
import {cancelOrder, rejectDriver} from 'customer/actions/CustomerActions';
import SpinnerButton from 'common/components/SpinnerButton';

class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    this.subscribeToOrderSummary(this.props);
  }

  componentWillReceiveProps(netProps){
    this.subscribeToOrderSummary(netProps);
  }


  subscribeToOrderSummary(props){
    const {dispatch, orderId, orderSummary, userId} = props;
    if (orderSummary == undefined) {
      dispatch(updateSubscriptionAction('orderSummaryDao', {
        userId,
        orderId,
        isCompleted: '',
        reportId: 'customerOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary = {status: ''}, client, history, busy, busyUpdating, dispatch} = this.props;
    const {delivery} = orderSummary;
    const isCancelled = orderSummary.status == OrderStatuses.CANCELLED;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const hasDriver = delivery.driverFirstName !== undefined;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const showCancelButton = !isComplete && !isCancelled && !hasDriver;

    const onCancelOrder = () => {
      dispatch(cancelOrder(orderSummary.orderId, () => history.push('/Customer/CustomerOrders')));
    };

    const onRejectDriver = () => {
      dispatch(rejectDriver(orderSummary.orderId, () => history.push('/Customer/CustomerOrders')));
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={orderSummary.status} isDriver={false} price={orderSummary.totalPrice}/>
        {showCancelButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> : null}
        {hasDriver ? <Text>Driver: {delivery.driverFirstName} {delivery.driverLastName} DRIVER IMAGE AND RATING HERE</Text> : null}
        {hasDriver && !isOnRoute ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectDriver}><Text uppercase={false}>Reject Driver</Text></SpinnerButton> : null}
        {isOnRoute ? <Button padded fullWidth style={styles.ctaButton} signOutButton onPress={() => history.push('/Customer/CustomerOrderInProgress', {orderId: orderSummary.orderId})}><Text uppercase={false}>Track Driver</Text></Button> : null}
        <RatingSummary orderStatus={orderSummary.status} isDriver={false} delivery={orderSummary.delivery}/>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  ctaButton: {
    marginTop: 10
  }
};

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    orderId,
    busyUpdating: isAnyOperationPending(state, [{customerDao: 'cancelOrder'}, {customerDao: 'rejectDriver'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'updateSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

