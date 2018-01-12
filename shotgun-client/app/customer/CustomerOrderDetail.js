import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending} from 'common/dao';
import LoadingScreen from 'common/components/LoadingScreen';

class CustomerOrderDetail extends Component{
  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(updateSubscriptionAction('orderSummaryDao', {
        userId: undefined,
        orderId,
        isCompleted: undefined,
        reportId: 'customerOrderSummary'
      }));
    }
  }

  render() {
    const {orderSummary = {status: ''}, client, history, busy} = this.props;

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
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

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

export default connect(
  mapStateToProps
)(CustomerOrderDetail);

