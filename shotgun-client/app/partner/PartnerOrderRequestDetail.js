import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao} from 'common/dao';
import {Container, Header, Left, Button, Body, Title, Content, Text} from 'native-base';
import {OrderSummary, PriceSummary, ErrorRegion, LoadingScreen, SpinnerButton, Icon} from 'common/components';
import {acceptOrderRequest} from 'partner/actions/PartnerActions';
import {calculatePriceToBePaid} from 'common/components/checkout/CheckoutUtils';

class PartnerOrderRequestDetail extends Component{
  beforeNavigateTo(){
    const {dispatch, orderId, orderSummary} = this.props;
    if (orderSummary == undefined) {
      dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
        orderId,
        reportId: 'partnerOrderDetail'
      }));
    }
  }

  onAcceptPress = async() => {
    const {orderSummary, history, dispatch, ordersRoot, bankAccount, parentPath} = this.props;

    if (bankAccount) {
      dispatch(acceptOrderRequest(orderSummary.orderId, () => history.push({pathname: `${ordersRoot}/PartnerOrders`, transition: 'left'})));
    } else {
      // user has no bank account set up so take them to set it up
      history.push({pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left'}, {next: `${parentPath}/Checkout`});
    }
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
        <PriceSummary isFixedPrice={delivery.isFixedPrice} orderStatus={orderSummary.status} isPartner={true} price={price}/>
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
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest'}]),
    user: getDaoState(state, ['user'], 'userDao'),
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest'}, {partnerDao: 'updateOrderPrice'}]),
    busy: !orderSummary,
    orderSummary,
    bankAccount: getDaoState(state, ['bankAccount'], 'paymentDao')
  };
};

export default connect(mapStateToProps, true, true)(PartnerOrderRequestDetail);

