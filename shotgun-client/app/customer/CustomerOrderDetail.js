import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, Grid, Col} from 'native-base';
import {OrderSummary, Icon, LoadingScreen, PriceSummary, RatingSummary, SpinnerButton} from 'common/components';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {cancelOrder, rejectDriver} from 'customer/actions/CustomerActions';
import {Image} from 'react-native';

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
        reportId: 'customerOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary = {status: ''}, client, history, busy, busyUpdating, dispatch} = this.props;
    const {delivery = {}} = orderSummary;
    const isCancelled = orderSummary.status == OrderStatuses.CANCELLED;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const hasDriver = delivery.driverFirstName !== undefined;
    const isOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const showCancelButton = !isComplete && !isCancelled && !hasDriver;
    const showRejectDriverButton = hasDriver && !isComplete && !isOnRoute;

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
            <Icon name='back-arrow' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Order Summary</Title></Body>
      </Header>
      <Content>
        <PriceSummary orderStatus={orderSummary.status} isDriver={false} price={orderSummary.totalPrice}/>
        {showCancelButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> : null}
        {hasDriver && !isComplete ? <Grid style={styles.driverDetailView}>
          <Col style={{alignItems: 'flex-end'}}>
            <Image source={{uri: delivery.driverImageUrl}} resizeMode='contain' style={styles.driverImage}/>
          </Col>
          <Col>
            <Text>{delivery.driverFirstName} {delivery.driverLastName}</Text>
          </Col>
        </Grid> : null}
        {showRejectDriverButton ? <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectDriver}><Text uppercase={false}>Reject Driver</Text></SpinnerButton> : null}
        {isOnRoute ? <Button padded fullWidth style={styles.ctaButton} signOutButton onPress={() => history.push('/Customer/CustomerOrderInProgress', {orderId: orderSummary.orderId})}><Text uppercase={false}>Track Driver</Text></Button> : null}
        <RatingSummary orderStatus={orderSummary.status} isDriver={false} delivery={orderSummary.delivery}/>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client} product={orderSummary.product} contentType={orderSummary.contentType}/>
      </Content>
    </Container>;
  }
}

const styles = {
  ctaButton: {
    marginTop: 10
  },
  driverDetailView: {
    marginTop: 15,
    flex: 1,
    alignContent: 'center',
    alignItems: 'center'
  },
  driverImage: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 40,
    marginRight: 10
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

