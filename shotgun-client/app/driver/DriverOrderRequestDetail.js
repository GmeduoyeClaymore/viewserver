import React, {Component} from 'react';
import {connect} from 'react-redux';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending} from 'common/dao';
import {Container, Header, Left, Button, Icon, Body, Title, Content, Text} from 'native-base';
import {withRouter} from 'react-router';
import OrderSummary from 'common/components/OrderSummary';
import PriceSummary from 'common/components/PriceSummary';
import {acceptOrderRequest} from 'driver/actions/DriverActions';
import LoadingScreen from 'common/components/LoadingScreen';

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
        isCompleted: false,
        reportId: 'driverOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary, client, history, dispatch, busy} = this.props;

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
        <PriceSummary orderStatus={orderSummary.status} isDriver={true} price={12.00}/>
        <Button fullWidth padded style={styles.acceptButton} onPress={onAcceptPress}><Text uppercase={false}>Accept this job</Text></Button>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
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
  const orderId = initialProps.location.state.orderId;
  const orderSummaries = getDaoState(state, ['orders'], 'orderSummaryDao') || [];
  const orderSummary = orderSummaries.find(o => o.orderId == orderId);

  return {
    ...initialProps,
    orderId,
    busy: isAnyOperationPending(state, { orderSummaryDao: 'updateSubscription'}) || orderSummary == undefined,
    orderSummary
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequestDetail));

