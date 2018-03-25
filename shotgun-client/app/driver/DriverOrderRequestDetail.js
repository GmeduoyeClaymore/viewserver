import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors} from 'common/dao';

import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {withRouter} from 'react-router';
import {OrderSummary, PriceSummary, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest, updateOrderPrice} from 'driver/actions/DriverActions';

class DriverOrderRequestDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentDidMount(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('orderSummaryDao', {
        orderId,
        reportId: 'driverOrderSummary'
      }, true));
    }
  }

  render() {
    const {orderSummary, client, history, dispatch, busy, busyUpdating, errors, delivery} = this.props;

    const onAcceptPress = async() => {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push('/Driver/DriverOrders')));
    };

    return busy ? <LoadingScreen text="Loading Order"/> : <Container>
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
        <SpinnerButton busy={busyUpdating} fullWidth padded style={styles.acceptButton} onPress={onAcceptPress}><Text uppercase={false}>Accept this job</Text></SpinnerButton>
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
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);
  const {delivery} = (orderSummary || {});
  const errors = getOperationErrors(state, [
    { driverDao: 'acceptOrderRequest'},
    { orderSummaryDao: 'resetSubscription'}
  ]);
  return {
    ...initialProps,
    delivery,
    orderId,
    errors,
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'acceptOrderRequest'}, {driverDao: 'updateOrderPrice'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'resetSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequestDetail));

