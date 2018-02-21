import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Container, Header, Left, Button, Body, Title, Content, Text, View} from 'native-base';
import {OrderSummary, PriceSummary, RatingSummary, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {startOrderRequest, cancelOrderRequest, stopWatchingPosition} from 'driver/actions/DriverActions';
import shotgun from 'native-base-theme/variables/shotgun';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class DriverOrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentDidMount(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('orderSummaryDao', {
        orderId,
        reportId: 'driverOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary = {status: ''}, client, history, dispatch, busy, busyUpdating} = this.props;
    const isStarted = orderSummary.status == OrderStatuses.PICKEDUP;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;

    const onStartPress = async() => {
      dispatch(startOrderRequest(orderSummary.orderId, navigateToOrderInProgress));
    };

    const navigateToOrderInProgress = () => history.push('/Driver/DriverOrderInProgress', {orderId: orderSummary.orderId});

    const onCancelPress = async() => {
      dispatch(cancelOrderRequest(orderSummary.orderId, () => history.push('/Driver/DriverOrders')));
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
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={orderSummary.totalPrice}/>
        <RatingSummary orderSummary={orderSummary} isDriver={true}/>
        {!isComplete ?
          <View>
            {isStarted ?
              <Button fullWidth padded style={styles.startButton} onPress={navigateToOrderInProgress}><Text uppercase={false}>Show navigation map</Text></Button> :
              <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.startButton} onPress={onStartPress}><Text uppercase={false}>Start this job</Text></SpinnerButton>
            }
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

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    orderId,
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'startOrderRequest'}, { driverDao: 'cancelOrderRequest'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}]) || !orderSummary,
    orderSummary
  };
};

export default connect(
  mapStateToProps
)(DriverOrderDetail);

