import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {withRouter} from 'react-router';
import {OrderSummary, PriceSummary, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest} from 'driver/actions/DriverActions';

class DriverOrderRequestDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(updateSubscriptionAction('orderSummaryDao', {
        userId: undefined,
        orderId,
        isCompleted: '',
        reportId: 'driverOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary, client, history, dispatch, busy, busyUpdating} = this.props;

    const onAcceptPress = async() => {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push('/Driver/DriverOrders')));
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
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={orderSummary.totalPrice}/>
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

  return {
    ...initialProps,
    orderId,
    busyUpdating: isAnyOperationPending(state, [{ driverDao: 'acceptOrderRequest'}]),
    busy: isAnyOperationPending(state, [{ orderSummaryDao: 'updateSubscription'}]) || orderSummary == undefined,
    orderSummary
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequestDetail));

