import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {resetSubscriptionAction, getDaoState, isAnyOperationPending, getNavigationProps, getOperationErrors, findOrderSummaryFromDao, getAnyOperationError} from 'common/dao';
import {Text, Container, Header, Left, Button, Body, Title, Content} from 'native-base';
import {OrderSummary, PriceSummary, LoadingScreen, Icon, ErrorRegion, RatingSummary} from 'common/components';
import OrderLifecycleView from 'common/components/orders/OrderLifecycleView';
import {respondToOrder} from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import PartnerNegotiationPanel from './PartnerNegotiationPanel';
import PartnerPaymentStagesPanel from './PartnerPaymentStagesPanel';
import DayRatePersonellOrderInProgress from './progress/DayRatePersonellOrderInProgress';
import PartnerJourneyOrderInProgress from './progress/PartnerJourneyOrderInProgress';
import HireOrderInProgress from './progress/HireOrderInProgress';

class PartnerOrderDetail extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  beforeNavigateTo(){
    const {dispatch, orderId, order, responseParams} = this.props;
    dispatch(resetSubscriptionAction('singleOrderSummaryDao', {
      orderId,
      ...OrderSummaryDao.PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS
    }));
    if (order != undefined) {
      if (responseParams){
        const {order, history, dispatch, bankAccount, path} = this.props;
        const {orderId,  orderContentTypeId} = order;
        const {negotiationDate, negotiationAmount} = responseParams;
        if (bankAccount) {
          dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount,  () => history.push({pathname: path, state: {orderId}})));
        }
      }
    }
  }

  onOrderRespond = async({negotiationAmount, negotiationDate}) => {
    const {order, history, dispatch, bankAccount, parentPath, path} = this.props;
    const {orderId, orderContentTypeId, customer} = order;
    if (bankAccount) {
      dispatch(respondToOrder(orderId, orderContentTypeId, negotiationDate, negotiationAmount,  () => history.push({pathname: path, state: {orderId}})));
    } else {
      const next = {pathname: path, state: {orderId, responseParams: {negotiationDate, negotiationAmount} }, transition: 'left'};
      // user has no bank account set up so take them to set it up
      history.push({pathname: `${parentPath}/Settings/UpdateBankAccountDetails`, transition: 'left', state: {next}});
    }
  }

  render() {
    const {order = {}, client, history, busy, orderId, busyUpdating, dispatch, errors} = this.props;
    const {onOrderRespond, resources} = this;
    const {InProgressControls} = resources;
    return busy || !order ? <LoadingScreen text={ !busy && !order ? 'Order "' + orderId + '" cannot be found' : 'Loading Order...'}/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack({transition: 'right'})}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle(order)}</Title></Body>
      </Header>
      <Content>
        <ErrorRegion errors={errors}/>
        <PriceSummary orderStatus={order.orderStatus} isPartner={true} price={order.amount}/>
        <OrderLifecycleView {...{...this.props, onOrderRespond}}
          PlacedControls={[PartnerNegotiationPanel]}
          InProgressControls={[InProgressControls]}
          AcceptedControls={[InProgressControls]}
          CompletedControls={[RatingSummary, OrderSummary]}
          CancelledControls={[OrderSummary]}
        />
        <OrderSummary order={order} client={client}/>
      </Content>
    </Container>;
  }
}

const styles = {
  suggestText: {
    width: '100%',
    alignSelf: 'stretch',
    justifyContent: 'center',
    textAlign: 'center',
    padding: 10
  },
  acceptButton: {
    marginTop: 20,
    marginBottom: 10
  }
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('PageTitle', () => 'Order Summary').
  delivery((order) => `${order.orderProduct.name} Delivery`).
  hire((order) => `${order.orderProduct.name}  Hire`).
  personell((order) => `${order.orderProduct.name} Job`).
  rubbish((order) => `${order.orderProduct.name} Rubbish Collection`).
property('PlacedControls', [PartnerNegotiationPanel, OrderSummary]).
property('InProgressControls').
  personell(order => order.paymentType === 'DAYRATE' ? [DayRatePersonellOrderInProgress, PartnerPaymentStagesPanel] : [PartnerPaymentStagesPanel]).
  hire(() => [HireOrderInProgress]).
  delivery(() => [PartnerJourneyOrderInProgress]).
  rubbish(() => [PartnerJourneyOrderInProgress]);
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const {orderId, responseParams} = getNavigationProps(initialProps);
  const order = findOrderSummaryFromDao(state, orderId, 'singleOrderSummaryDao');
  const user = getDaoState(state, ['user'], 'userDao');
  const {bankAccount} = user;
  return {
    ...initialProps,
    errors: getOperationErrors(state, [{ partnerDao: 'acceptOrderRequest'}]) + '\n' + getAnyOperationError(state, 'orderDao') + '\n' + getAnyOperationError(state, 'singleOrderSummaryDao'),
    user,
    busyUpdating: isAnyOperationPending(state, [{ partnerDao: 'acceptOrderRequest'}, {partnerDao: 'updateOrderPrice'}]),
    busy: !order,
    order,
    orderId,
    responseParams,
    bankAccount
  };
};

export default withExternalState(mapStateToProps)(PartnerOrderDetail);

