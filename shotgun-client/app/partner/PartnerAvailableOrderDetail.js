import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao} from 'common/dao';
import {Container, Header, Left, Button, Body, Title, Content} from 'native-base';
import {OrderSummary, PriceSummary, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest} from 'partner/actions/PartnerActions';
import {OrderStatuses} from 'common/constants/OrderStatuses';

class PartnerAvailableOrderDetail extends Component{
  beforeNavigateTo(){
    const {dispatch, orderId, order} = this.props;
    if (order == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'partnerOrderDetail'
      }));
    }
  }

  onAcceptPress = async() => {
    const {orderSummary: order, history, dispatch, ordersRoot, bankAccount, parentPath} = this.props;

    if (bankAccount) {
      dispatch(acceptOrderRequest(order.orderId, () => history.push({pathname: `${ordersRoot}/PartnerMyOrders`, transition: 'left'})));
    } else {
      // user has no bank account set up so take them to set it up
      history.push({pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left'}, {next: `${parentPath}/Checkout`});
    }
  };

  render() {
    const {order = {}, client, history, busy, busyUpdating, user} = this.props;
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
        <PriceSummary orderStatus={order.orderStatus} isPartner={!userCreatedThisOrder} price={order.amount}/>
        <OrderSummary order={order} client={client}/>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const orderId = getNavigationProps(initialProps).orderId;
  let order = findOrderSummaryFromDao(state, orderId, 'orderRequestDao');
  order = order || findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');

  return {
    ...initialProps,
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest'}]),
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest'}, {partnerDao: 'updateOrderPrice'}]),
    busy: !order,
    order,
    orderId,
    bankAccount: getDaoState(state, ['bankAccount'], 'paymentDao')
  };
};

export default connect(mapStateToProps, true, true)(PartnerAvailableOrderDetail);

