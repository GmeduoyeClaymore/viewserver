import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Container, Header, Left, Button, Icon, Body, Title, Content} from 'native-base';
import OrderSummary from 'common/components/OrderSummary';
import {updateSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps} from 'common/dao';
import LoadingScreen from 'common/components/LoadingScreen';
import PriceSummary from 'common/components/PriceSummary';
import RatingSummary from 'common/components/RatingSummary';

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
        <PriceSummary orderStatus={orderSummary.status} isDriver={false} price={orderSummary.totalPrice}/>
        <RatingSummary orderStatus={orderSummary.status} isDriver={false} delivery={orderSummary.delivery}/>
        <OrderSummary delivery={orderSummary.delivery} orderItem={orderSummary.orderItem} client={client}/>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
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

