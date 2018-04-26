import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao} from 'common/dao';
import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {OrderSummary, PriceSummary, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest} from 'driver/actions/DriverActions';
import {calculatePriceToBePaid} from 'common/components/checkout/CheckoutUtils';

class DriverOrderRequestDetail extends Component{
  beforeNavigateTo(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'driverOrderSummary'
      }));
    }
  }

  onAcceptPress = async() => {
    const {orderSummary, history, dispatch, ordersRoot} = this.props;
    dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push({pathname: `${ordersRoot}/DriverOrders`, transition: 'left'})));
  };

  render() {
    const {orderSummary = {}, client, history, busy, busyUpdating, errors, user} = this.props;
    const {delivery = {}} = orderSummary;
    const userCreatedThisOrder = user.userId == orderSummary.customerUserId;
    const price = userCreatedThisOrder ? orderSummary.totalPrice : calculatePriceToBePaid(orderSummary.totalPrice, user);

    return busy ? <LoadingScreen text='Waiting for order'/> : <Container>
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
        <PriceSummary isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isDriver={true} price={price}/>
        {!userCreatedThisOrder ? <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.acceptButton} onPress={this.onAcceptPress}><Text uppercase={false}>Accept this job</Text></SpinnerButton> : null }
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
  let orderSummary = findOrderSummaryFromDao(state, orderId, 'orderSummaryDao');
  orderSummary = orderSummary || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');

  return {
    ...initialProps,
    orderId,
    errors: getOperationErrors(state, [{ driverDao: 'acceptOrderRequest'}]),
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'acceptOrderRequest'}, {driverDao: 'updateOrderPrice'}]),
    busy: !orderSummary,
    orderSummary
  };
};

export default connect(mapStateToProps, true, true)(DriverOrderRequestDetail);

